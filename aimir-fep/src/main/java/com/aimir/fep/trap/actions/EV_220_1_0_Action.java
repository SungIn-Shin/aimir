package com.aimir.fep.trap.actions;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.aimir.constants.CommonConstants.TargetClass;
import com.aimir.dao.device.MeterDao;
import com.aimir.dao.device.ModemDao;
import com.aimir.dao.system.ContractDao;
import com.aimir.dao.system.DeviceModelDao;
import com.aimir.dao.system.OperatorDao;
import com.aimir.dao.system.PrepaymentLogDao;
import com.aimir.fep.trap.common.EV_Action;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.EventUtil;
import com.aimir.fep.util.Hex;
import com.aimir.model.device.EventAlertAttr;
import com.aimir.model.device.EventAlertLog;
import com.aimir.model.device.Meter;
import com.aimir.model.device.ZRU;
import com.aimir.model.system.Contract;
import com.aimir.model.system.DeviceModel;
import com.aimir.model.system.Operator;
import com.aimir.model.system.PrepaymentLog;
import com.aimir.model.system.Supplier;
import com.aimir.notification.FMPTrap;
import com.aimir.util.DateTimeUtil;

/**
 * @name OID: 220.1.0 (eventSTSInformation)
 * @descr DCU가 수집하여 업로드하는 Zigbee STS(Suni) Information.
 * @requirement EventClass "STS Alarm" 필요. ('Alert', 'SaveAndMonitor', 'Major')
 * @author sjhan
 *
 */
@Component
public class EV_220_1_0_Action implements EV_Action {

	private static Log log = LogFactory.getLog(EV_220_1_0_Action.class);
	
	@Resource(name="transactionManager")
	JpaTransactionManager txmanager;
	
	@Autowired
    ModemDao modemDao;
    
    @Autowired
    MeterDao meterDao;
    
    @Autowired
    ContractDao contractDao;
    
    @Autowired
    PrepaymentLogDao pLogDao;
    
    @Autowired
    DeviceModelDao deviceModelDao;
    
    @Autowired
    OperatorDao operatorDao;
    
    @Autowired
    EventUtil eventUtil;
    
    public static int FIXED_LENGTH=30; //sensor(8)+stsnumber(11)+balance(11)
    public static int SENSOR_ID_LENGTH=8;
    public static int STSNUMBER_LENGTH=11;
    public static int BALANCE_LENGTH=11;
	
	/**
	 * @param trap - FMP Trap(MCU Event)
     * @param event - Event Alert Log Data
	 */
	@Override
	public void execute(FMPTrap trap, EventAlertLog event) throws Exception {
		
		/**
		 * 체크할 사항
		 * 1) 번호불일치 (모듈변경), 규격에 맞지 않는 번호, 모듈제거는 알수없음(모듈이없으면, dcu에서 수집x)
		 * 2) 과차감 여부, 음수 저장시 relay off 변경?
		 */
		
		//개발 완료되면 log단계를 debug로 수정할 것 (ok)
		log.info("EV_220_1_0_Action : EventName[eventSTSInformation] "+" EventCode[" + trap.getCode()+"] MCU["+trap.getMcuId()+"]");
		TransactionStatus txstatus = null;
		
		String commDate = trap.getTimeStamp();
		
		for (EventAlertAttr ea : event.getEventAlertAttrs()) {
            log.debug("attr[" + ea.getAttrName() + "] type[" + ea.getAttrType() + "] value[" + ea.getValue()+"]");
        }
		
		// stsInfoCount(1.9, INT) - number of STREAM
		int stsInfoCount = Integer.parseInt(event.getEventAttrValue("intEntry"));
		if(stsInfoCount < 1) {
			log.info("STS Info in eventSTSInformation is empty.");
		}

		// STREAM 처리
		String stsInfoStream = event.getEventAttrValue("streamEntry.hex");
		int[] infoSizeArr = new int[stsInfoCount];
		String[] sensorIdArr = new String[stsInfoCount];
		String[] meterIdArr = new String[stsInfoCount];
		String[] stsNumberArr = new String[stsInfoCount];
		String[] balanceArr = new String[stsInfoCount];
		
		// 고정길이
		byte[] stsInfoByte = hexStringToByteArray(stsInfoStream);
		byte[] sensorIdByte = new byte[SENSOR_ID_LENGTH];
		byte[] stsNumberByte = new byte[STSNUMBER_LENGTH];
		byte[] balanceByte = new byte[BALANCE_LENGTH];
		
		// 분리 (length가 이상하면 아무것도 처리하지 않는다)
		int pos=0;
		for(int j=0; j<stsInfoCount; j++) {
			
			byte[] temp = new byte[1];
			System.arraycopy(stsInfoByte, pos, temp, 0, 1);
			int itemSize = DataUtil.getIntToBytes(temp);
			
			if(itemSize <= FIXED_LENGTH) {
				//내부스트림이 최소길이보다 짧은 경우(미터ID 최소길이 1로 고려)
				log.error("EV_220_1_0_Action : STS Info STREAM is empty!");
				return;
			}
			infoSizeArr[j] = itemSize;
			pos=pos+1;
			log.debug("for["+j+"], pos["+pos+"], itemSize["+itemSize+"]");
			
			System.arraycopy(stsInfoByte, pos, sensorIdByte, 0, SENSOR_ID_LENGTH);
			sensorIdArr[j] = Hex.decode(sensorIdByte);
			pos=pos+SENSOR_ID_LENGTH;
			log.debug("for["+j+"], pos["+pos+"], sensorIdArr["+sensorIdArr[j]+"]");
			
			int meterIdLeng = itemSize-FIXED_LENGTH-1; //total - fixedLength - itemSizeLength
			byte[] meterId = new byte[meterIdLeng];
			System.arraycopy(stsInfoByte, pos, meterId, 0, meterIdLeng);
			meterIdArr[j] = DataUtil.getString(meterId);
			pos=pos+meterIdLeng;
			log.debug("for["+j+"], pos["+pos+"], meterIdArr["+meterIdArr[j]+"]");
			
			System.arraycopy(stsInfoByte, pos, stsNumberByte, 0, STSNUMBER_LENGTH);
			stsNumberArr[j] = DataUtil.getString(stsNumberByte);
			pos=pos+STSNUMBER_LENGTH;
			log.debug("for["+j+"], pos["+pos+"], stsNumberArr["+stsNumberArr[j]+"]");
			
			System.arraycopy(stsInfoByte, pos, balanceByte, 0, BALANCE_LENGTH);
			balanceArr[j] = Hex.decode(balanceByte);
			pos=pos+BALANCE_LENGTH;
			log.debug("for["+j+"], pos["+pos+"], balanceArr["+balanceArr[j]+"]");
			
		}
		
		for(int i=0; i<stsInfoCount; i++) {
			//int infoSize = infoSizeArr[i]; //1+8+11+11+미터시리얼대략14 (Stream분리할때 사용)
			String modemId = sensorIdArr[i].trim();  // 00 0D 6F 00 0B 4D 71 89
			String meterId = meterIdArr[i].trim();
			String stsNumber = stsNumberArr[i].trim();
			
			Map<String,Object> balanceInfo = null;
            String balanceDate = null;
            Double credit = null;
            if(balanceArr[i] != null && !"".equals(balanceArr[i])) {
            	balanceInfo = convertBalance(balanceArr[i]);
            	balanceDate = (String)balanceInfo.get("balanceDate");
                credit = (Double)balanceInfo.get("credit");
            }
            
			// transaction
            txstatus = txmanager.getTransaction(null);
            
			if(stsNumber != null && stsNumber.length() != 11) {
				// stsNumber가 11자리가 아니면, 경고 이벤트 생성 (and 데이터 저장x)
				log.info("METER["+ meterId + "]: stsNumber [" + stsNumber + "] is Wrong Number.");

				eventUtil.sendEvent("STS Alarm",
	                    TargetClass.MCU,
	                    trap.getMcuId(),
	                    new String[][] {{"message", "Invalid format: STS Number["+stsNumber+"]" }});
				
				//transaction 완료
				txmanager.commit(txstatus);
				continue;
			}
			
			
			ZRU modem = (ZRU)modemDao.get(modemId);			
			if(modem == null) {
				//필요한 install 정보가 없으므로, 장비를 새로 등록할 수 없음.
				
				eventUtil.sendEvent("STS Alarm",
	                    TargetClass.MCU,
	                    trap.getMcuId(),
	                    new String[][] {{"message", "Unregistered Device: Modem EUI["+modemId+"]" }});
				
				//transaction 완료
				txmanager.commit(txstatus);
				continue;
			}
			
			Meter meter = null;
			meter = meterDao.get(meterId);
			if(meter == null) {
				//필요한 install 정보가 없으므로, 장비를 새로 등록할 수 없음.
				
				eventUtil.sendEvent("STS Alarm",
	                    TargetClass.MCU,
	                    trap.getMcuId(),
	                    new String[][] {{"message", "Unregistered Device: Meter Serial["+meterId+"]" }});
				
			}else {
				// 미터의 모뎀과 입력받은 모뎀이 다르면 갱신해준다..
				if (meter.getModem() == null || !modem.getDeviceSerial().equals(meter.getModem().getDeviceSerial())) {
                    meter.setModem(modem);
                }
				
				// STS Number가 변경되면, 미터를 갱신하고 별도 이벤트를 생성해준다.
				String meterIhd = meter.getIhdId()==null?"null":meter.getIhdId(); 
				if(stsNumber != null && !stsNumber.equals(meterIhd)) {
                	log.info("STSNumber Change [" + meterIhd + "] => [" + stsNumber +"]");
                	meter.setIhdId(stsNumber);
                	
                	if("null".equals(meterIhd)) {
        				eventUtil.sendEvent("STS Alarm",
        	                    TargetClass.EnergyMeter,
        	                    meter.getMdsId(),
        	                    new String[][] {{"message", "New STSModule : meter["+meterId+"] & sts["+stsNumber+"]" }});
        				
                	}else {
        				eventUtil.sendEvent("STS Alarm",
        	                    TargetClass.EnergyMeter,
        	                    meter.getMdsId(),
        	                    new String[][] {{"message", "STSModule Change:["+meterIhd+"] => ["+stsNumber+"]" }});
        				
                	}
                	
                }
				
				//stsNumber가 존재하면 미터 모델명을 변경.
                if(stsNumber != null && !"OmniPower STS".equals(meter.getModel().getName())) {
                	log.debug("Model Change [" + meter.getModel().getName() + "] => OmniPower STS Model");
                	DeviceModel stsModel = deviceModelDao.findByCondition("name", "OmniPower STS");
                	meter.setModel(stsModel);
                	log.debug("[HSW] HHH setModel:"+stsModel.getName());
                }
                
                //Device 정보 업데이트
                Supplier supplier = meter.getSupplier();
                modem.setLastLinkTime(DateTimeUtil.getDST(supplier.getTimezone().getName(), commDate));
                modemDao.update(modem);
                meterDao.update(meter);
                
                //잔액 저장 및 prepaymentLog 생성
                if("OmniPower STS".equals(meter.getModel().getName()) && stsNumber != null 
                		&& meter.getContract() != null && credit != null) {
                	//
                	Contract contract = meter.getContract();
                	Double preContractCredit = contract.getCurrentCredit() == null ? 0d : contract.getCurrentCredit();
                	log.debug("serverCredit["+contract.getCurrentCredit()+"] => STS Credit[" + credit +"]");
                	if(!preContractCredit.equals(credit)) {
                		log.info("save prepaymentLog: contract[" + contract.getContractNumber() + "]");
                		
                		contract.setCurrentCredit(credit);
                		contract.setIsSts(true);
                		contractDao.update(contract);
                		
                		PrepaymentLog pLog = new PrepaymentLog();
                		pLog.setId(Long.parseLong(Integer.toString(contract.getId())
                                + Long.toString(System.currentTimeMillis())));
                    	pLog.setContract(contract);
                    	pLog.setCustomer(meter.getCustomer());
                    	Operator operator = operatorDao.getOperatorByLoginId("admin");
                    	pLog.setOperator(operator);
                    	pLog.setBalance(credit);
                    	if(balanceDate != null) {
                    		pLog.setLastTokenDate(balanceDate+"00");
                    	}
                    	pLog.setLocation(contract.getLocation());
                        pLogDao.add(pLog);
                	}
                	
                }
                
			} //~(meter==null) else
			
			//transaction 완료
			txmanager.commit(txstatus);
			
		} //~for
		
		
		
	}
	
	
	// Convert balance stream to dateTime&credit
	private Map<String,Object> convertBalance(String evtBalance) {
    	Map<String,Object> map = new HashMap<String,Object>();
    	int pos = 0;
        byte[] b = new byte[6];
        System.arraycopy(Hex.encode(evtBalance), pos, b, 0, b.length);
        pos += b.length;
        String balanceDate = String.format("%4d%02d%02d%02d%02d",
                DataUtil.getIntTo2Byte(new byte[]{b[0], b[1]}),
                DataUtil.getIntToByte(b[2]),
                DataUtil.getIntToByte(b[3]),
                DataUtil.getIntToByte(b[4]),
                DataUtil.getIntToByte(b[5]));
        map.put("balanceDate", balanceDate);
        log.debug("#-balanceDate [" + balanceDate + "]");
        
        b = new byte[4];
        System.arraycopy(Hex.encode(evtBalance), pos, b, 0, b.length);
        pos += b.length;
        log.debug("#-long ["+Hex.decode(b)+ "]");
        long lcredit = 0l;
        if(b[0] < 0) {
        	lcredit = (((((~b[0]) & 0xff) << 8) + (((~b[1]) & 0xff) << 8) + (((~b[2]) & 0xff) << 8) + ((~b[3]) & 0xff)) + 1)*-1;
        } else {
        	lcredit = DataUtil.getIntToBytes(b);
        }

        b = new byte[1];
        System.arraycopy(Hex.encode(evtBalance), pos, b, 0, b.length);
        log.debug("#-decimal point ["+Hex.decode(b)+ "]");
        int dec = DataUtil.getIntToBytes(b);
        
        Double credit = (double)lcredit / Math.pow(10, dec);
        map.put("credit", credit);
        log.debug("#-converted Credit[" + credit + "]");
        
        return map;
    }
	
	// Convert Stream to byte array
	public byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	// Parse 
	public String getHexString(String str) {
		//ex) 32 30 31 37 39 37 35 31 00 00 00 00 00 00 00 00 00 00 00 00 00 --> 20179751
		//ex) 39 36 30 30 30 30 30 35 37 39 31 --> 96000005791 
		byte[] bar = hexStringToByteArray(str);
		String result = new String(bar,StandardCharsets.UTF_8);
		
		return result;
	}
	
	
	public String getHexStringTime(String str) {
		//ex) 07 E2 03 0E 09 09 00 --> 2018 03 14 09 09 00
		if(str.length() == 12) {
			str = str.concat("00");
		}
		byte[] bar = hexStringToByteArray(str);
		String result = DataUtil.getTimeStamp(bar);
		
		return result;
	}

}
