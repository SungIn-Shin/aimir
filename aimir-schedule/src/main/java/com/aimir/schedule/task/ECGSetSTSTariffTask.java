package com.aimir.schedule.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import com.aimir.constants.CommonConstants.McuType;
import com.aimir.constants.CommonConstants.ModemType;
import com.aimir.constants.CommonConstants.OperatorType;
import com.aimir.constants.CommonConstants.TR_OPTION;
import com.aimir.constants.CommonConstants.TR_STATE;
import com.aimir.dao.device.AsyncCommandLogDao;
import com.aimir.dao.device.AsyncCommandParamDao;
import com.aimir.dao.device.MeterDao;
import com.aimir.dao.system.ContractDao;
import com.aimir.dao.system.StsContractDao;
import com.aimir.dao.system.TariffEMDao;
import com.aimir.dao.system.TariffTypeDao;
import com.aimir.fep.util.DataUtil;
import com.aimir.model.device.AsyncCommandLog;
import com.aimir.model.device.AsyncCommandParam;
import com.aimir.model.device.Meter;
import com.aimir.model.device.Modem;
import com.aimir.model.system.Code;
import com.aimir.model.system.Contract;
import com.aimir.model.system.StsContract;
import com.aimir.model.system.TariffEM;
import com.aimir.model.system.TariffType;
import com.aimir.schedule.command.CmdOperationUtil;
import com.aimir.util.Condition;
import com.aimir.util.Condition.Restriction;
import com.aimir.util.DateTimeUtil;

/**
 * 운영중 상시 설치되는 STS 미터를 대상으로 
 * 현재 Tariff 및 변경 예정 Tariff를 전송.
 * @CurrentTariff 현재 사용해야하는 요금제.
 * @FutureTariff 앞으로 적용될 Tariff.
 * Tariff 전송은 각 계약별 Thread 처리.
 * @author sjhan
 *
 */
@Service
public class ECGSetSTSTariffTask {
	protected static Log log = LogFactory.getLog(ECGSetSTSTariffTask.class);
	private static final String SERVICE_TYPE_EM = "Electricity";
	
	@Resource(name="transactionManager")
    HibernateTransactionManager txmanager;
	
	@Autowired
    ContractDao contractDao;
	
	private boolean isNowRunning = false;
	
	public static void main(String[] args) {
		// STS용 공통 spring
		ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"spring-setStsCommand.xml"}); 
        DataUtil.setApplicationContext(ctx);
            	
    	// 1. contract 조회 (id, meter_id, tariff_index) -> is_sts, prepay, emergency
    	// 2. 각 row별로 thread 시작
        // 3. stscontract 조회 -> validtarget.. 없으면 신규 stscontract 생성
    	// 4. current_tariff
    	// 5. init_token이 null이면 발급.. (schedule-exec/command.prop에 TSM정보있음)
        
        // 별도의 입력 파라미터 필요없음..
        // 옵션.. 발급전용 혹은 전송전용..?
        
        ECGSetSTSTariffTask task = ctx.getBean(ECGSetSTSTariffTask.class);
        task.execute();
        System.exit(0);        
        
	}
	
	// 계약 조회
	public List<Contract> getContractInfos(String serviceType) {
		log.info("### Get Contract Ids.");
		TransactionStatus txstatus = null;
		try {
			txstatus = txmanager.getTransaction(null);
            Set<Condition> condition = new HashSet<Condition>();
            
            condition.add(new Condition("serviceTypeCode", new Object[]{"s"}, null, Restriction.ALIAS));
            condition.add(new Condition("s.name", new Object[]{serviceType}, null, Restriction.EQ));
            condition.add(new Condition("creditType", new Object[]{"c"}, null, Restriction.ALIAS));
            condition.add(new Condition("c.code", new Object[]{Code.PREPAYMENT, Code.EMERGENCY_CREDIT}, null, Restriction.IN));
            // OPF-114
            condition.add(new Condition("isSts", new Object[]{true}, null, Restriction.EQ));
            // OPF-88
            condition.add(new Condition("isNetMetering", new Object[]{false}, null, Restriction.EQ));
            List<Contract> contracts = contractDao.findByConditions(condition);
            txmanager.commit(txstatus);
            
            return contracts;
            
		} catch (Exception e) {
            log.error(e, e);
            if (txstatus != null) txmanager.rollback(txstatus);
        }
		
		return null; 
		
	}
	
	// Task 시작
	public void execute() {
		if(isNowRunning){
            log.info("#####  [ECG Set STSTariff] is already running...  #####");
            return;
        }
        isNowRunning = true;
        log.info("#####  START [ECG Set STSTariff]  #####");
        
        // Contract 조회 <contract.id, contract.meterid>
        List<Contract> em_contract = this.getContractInfos(SERVICE_TYPE_EM);
        
        if(em_contract != null && em_contract.size() > 0) {
        	ThreadPoolExecutor tpe = new ThreadPoolExecutor(10, 10, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
        	tpe.allowCoreThreadTimeOut(true); 
        	log.info("#### Contract Size: " + em_contract.size());
        	for(Contract emc : em_contract ) {        		
        		if(emc.getMeterId() != null && emc.getTariffIndexId() != null) {
        			// @contract.id @tariffem.index @meter.id 
        			tpe.execute(new SetSTSTariffThread(emc.getId(), emc.getTariffIndexId(), emc.getMeterId()) );
        		}
        	}
        	
        	// wait
        	try {
        			tpe.shutdown();
        			while (!tpe.isTerminated()) {}
        	}catch (Exception e) {}
        }
        
        log.info("#####  END [ECG Set STSTariff]  #####");
        isNowRunning = false;     
	}
	
	
} //~ECGSetSTSTariffTask


// Tariff 전송 쓰레드
@Transactional(readOnly=false)
class SetSTSTariffThread implements Runnable {
	
	private static Log log = LogFactory.getLog(SetSTSTariffThread.class);
	
	HibernateTransactionManager txmanager = (HibernateTransactionManager)DataUtil.getBean("transactionManager");
	
	Integer contractId;
	Integer tariffIdx;
	Integer meterId;
	String sid;
	
	public SetSTSTariffThread(Integer _contractId, Integer _tariffIdx, Integer _meterId) {
		this.contractId = _contractId;
		this.tariffIdx = _tariffIdx;
		this.meterId = _meterId;
		this.sid = "";
	}
	
	public void setSid(String _sid) {
		this.sid = _sid;
	}
	
	public String getSid() {		
		return this.sid;
	}

	@Override
	public void run() {
		TransactionStatus txstatus1 = null;
		txstatus1 = txmanager.getTransaction(null);
		
		
		// Meter 검증
		MeterDao meterDao = DataUtil.getBean(MeterDao.class);
		Meter stsMeter = meterDao.get(this.meterId);
		
		log.info("Meter Serial [" + stsMeter.getMdsId() + "], Momdem Type [" + stsMeter.getModem().getModemType() + "] Contract[" + this.contractId + "]");
		
		if (stsMeter == null || stsMeter.getIhdId() == null) {
			log.error("Warning: Contract[" + this.contractId + "] -> MeterId[" + this.meterId + "] is not exist or STSNumber is null.");
			return;
		}
		
		String meterMds = stsMeter.getMdsId();
		String stsNumber = stsMeter.getIhdId();
		
		// TariffType 불러오기
		TariffTypeDao tariffTypeDao = DataUtil.getBean(TariffTypeDao.class);
		TariffType tariffType = tariffTypeDao.get(this.tariffIdx);
		
		txmanager.commit(txstatus1);
		
		// stscontract 조회
		StsContract stscon = getStsCon(this.contractId, meterMds, stsNumber);
		
		if(stscon.getValidTarget()!=null && !stscon.getValidTarget()) {
			//validtarget이 false이면 진행하지 않는다.
			log.info("Warning: Contract["+this.contractId+"] -> ValidTarget is false.");
			return;
		}
		
		
		TransactionStatus txstatus6 = null;
		txstatus6 = txmanager.getTransaction(null);
		
		/** [0] 기본 정보 조회 */
		// basic date (현재 Tariff와 대기 Tariff를 정하는 기준일 - 실행일자)
		String writeDate = DateTimeUtil.getDateString(new Date());
		String basicDate = writeDate.substring(0, 8);
		// tariff index
		Integer stsCurrentIdx = stscon.getCurrentTariffIndexId() == null ? -1 : stscon.getCurrentTariffIndexId();
		Integer stsFutureIdx = stscon.getFutureTariffIndexId() == null ? -1 : stscon.getFutureTariffIndexId();
		// tariff date
		String stsCurrentDate = stscon.getCurrentTariff() == null ? "" : stscon.getCurrentTariff();
		String stsFutureDate = stscon.getFutureTariff() == null ? "" : stscon.getFutureTariff();
		// transfer date (전송 성공 일자)
		String currentTransfer = stscon.getCurrentTariffDate() == null ? "" : stscon.getCurrentTariffDate();
		String futureTransfer = stscon.getFutureTariffDate() == null ? "" : stscon.getFutureTariffDate();
		
		/** [1] System Tariff 정보 가져오기(현재날짜, 다음날짜) */
		Map<String, String> tariffDates = getSystemTariffDate(basicDate);
		String sysCDate = tariffDates.get("current");
		String sysFDate = tariffDates.get("future");
		if (sysCDate == "" || "".equals(sysCDate)) {
			// 현재 적용할 Tariff가 System에 없으면 진행하지 않음.
			log.info("Critical: Contract[" + this.contractId + "] No Tariff Info at SYSTEM..");
			return;
		}

		/** [2] FutureTariff와 기준일자 비교 */
		if (stsFutureIdx > -1 && stsFutureIdx == this.tariffIdx) {
			// FutureTariff일자가 BasicDate보다 과거가되면, Current로 취급해야한다.
			// 반복 처리를 없애기 위해, 양쪽 Transfer값이 동일하면 skip함.
			if (futureTransfer.length() == 14 && stsFutureDate.length() == 8 && basicDate.compareTo(stsFutureDate) > -1
					&& futureTransfer.compareTo(currentTransfer) != 0) {
				stsCurrentIdx = stsFutureIdx;
				stsCurrentDate = stsFutureDate;
				currentTransfer = futureTransfer;
				log.info("INFO: Contract[" + this.contractId + "] --> Future tariff copied to current tariff..");

				// Update
				if (stsCurrentIdx > -1)
					// stscon.setCurrentTariffIndexId(stsCurrentIdx);
					stscon.setCurrentTariffType(tariffType);
				if (stsCurrentDate.length() > 1)
					stscon.setCurrentTariff(stsCurrentDate);
				if (currentTransfer.length() > 1)
					stscon.setCurrentTariffDate(currentTransfer);
			}
		}
		
		/** GPRS STS METER */
		if (ModemType.MMIU.toString() == stsMeter.getModem().getModemType().toString()) {
			/** [3] CurrentTariff/FutureTariff 전송 여부 */
			Boolean isCon = false;
			if (currentTransfer.length() != 14 || stsCurrentDate.compareTo(sysCDate) < 0) {
				// 전송조건1) currentTransfer전송일이 null 이거나,
				// 전송조건2) 적용할 대상날짜보다 stsCurrentDate가 과거인 경우.
				List<Map<String, Object>> asyncParamMap = this.getTariffParam(sysFDate);

				if (asyncParamMap != null) {
					isCon = this.sendTariff_MMIU(asyncParamMap, writeDate);

					// 갱신할 정보 세팅
					if (stsCurrentIdx > -1 && stsCurrentIdx != this.tariffIdx) {
						// 전송조건3) TariffType이 변경되는 경우엔, FutureTariff에 내용을 저장함.
						stsFutureIdx = this.tariffIdx;
						stsFutureDate = sysCDate;
						futureTransfer = writeDate;
						log.info("SEND: Contract[" + this.contractId + "] --> TariffType(Future) Change...");
					} else {
						// 위 경우 제외한 나머지는 CurrentTariff임.
						stsCurrentIdx = this.tariffIdx;
						stsCurrentDate = sysCDate;
						currentTransfer = writeDate;
						log.info("SEND: Contract[" + this.contractId + "] --> Current Tariff Setting");
					}
				}
			} else if (sysFDate.length() == 8 && (futureTransfer.length() != 14 || stsFutureDate.compareTo(sysFDate) != 0)) {
				// 전송조건1) futureTransfer(다음 tariff 전송일)이 null이거나,
				// 전송조건2) future date가 system상 다음 적용일자와 일치하지 않을 경우.
				// 전송조건3) 단, sysFDate가 null이면, 전송해야할 다음 Tariff가 없는것이므로 그냥 넘어간다.
				List<Map<String, Object>> asyncParamMap = this.getTariffParam(sysFDate);

				if (asyncParamMap != null) {
					isCon = this.sendTariff_MMIU(asyncParamMap, writeDate);

					// 갱신할 정보 세팅
					stsFutureIdx = this.tariffIdx;
					stsFutureDate = sysFDate;
					futureTransfer = writeDate;

					log.info("SEND: Contract[" + this.contractId + "] --> Future Tariff Setting");
				}

			} else {
				// 모두 유효하면, 할거없음.
				log.info("DONE: Contract[" + this.contractId + "] --> Tariff Set was already done.");
			}

			/** [4] 결과 확인 및 stsCon 정보 갱신 */
			// tariff 전송후 tcp 결과 확인해서 상태가 0으로 변경되어 있다면, 정보 갱신.
			if (isCon) {
				try {
					// AsyncCommand 생성 후, 접속 및 처리시간 감안하여 30초 대기
					log.info("ASYNC: Contract[" + this.contractId + "] Wait for connection...");
					Thread.sleep(30000);

					Integer lastStatus = null;
					AsyncCommandLogDao asyncLogDao = DataUtil.getBean(AsyncCommandLogDao.class);
					lastStatus = asyncLogDao.getCmdStatus(this.getSid(), "cmdSetTariff");
					log.info("Info: Contract[" + this.contractId + "] --> AsyncCommand Status [" + lastStatus + "]..");

					// success(0x00), waiting(0x01), running(0x02), terminate(0x04),
					// delete(0x08), unknown(0xff)
					if (lastStatus == 0) {
						// 실행여부 성공시 stsCon정보 갱신
						tariffTypeDao = DataUtil.getBean(TariffTypeDao.class);

						if (stsCurrentIdx > -1) {
							TariffType tariffTypeC = tariffTypeDao.get(stsCurrentIdx);
							stscon.setCurrentTariffType(tariffTypeC);
						}
						if (stsCurrentDate.length() > 1)
							stscon.setCurrentTariff(stsCurrentDate);
						if (currentTransfer.length() > 1)
							stscon.setCurrentTariffDate(currentTransfer);
						if (stsFutureIdx > -1) {
							TariffType tariffTypeF = tariffTypeDao.get(stsFutureIdx);
							stscon.setFutureTariffType(tariffTypeF);
						}
						if (stsFutureDate.length() > 1)
							stscon.setFutureTariff(stsFutureDate);
						if (futureTransfer.length() > 1)
							stscon.setFutureTariffDate(futureTransfer);

					}

				} catch (Exception e) {
					log.info("Exception: Contract[" + this.contractId + "]");
					log.error(e, e);
					if (!txstatus6.isCompleted())
						txmanager.rollback(txstatus6);
				}
			}
		} else { /** ZigBee STS METER */
			Map<String, Object> map = new HashMap<String, Object>();
			Meter meter = meterDao.get(meterId);
			String mdsId = meter.getMdsId();
			
			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			String cmd = "cmdSetTariff";
			cmd = "ZigBeeSTS_" + cmd;
			
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());
			
			/** [3] CurrentTariff/FutureTariff 전송 여부 */
			if (currentTransfer.length() != 14 || stsCurrentDate.compareTo(sysCDate) < 0) {
				// 전송조건1) currentTransfer전송일이 null 이거나,
				// 전송조건2) 적용할 대상날짜보다 stsCurrentDate가 과거인 경우.
				
				// sysCDate
				List<Map<String, Object>> returnList = this.getTariffParam(sysFDate);
				
				datas.add("yyyymmdd," + returnList.get(0).get("string"));
				datas.add("condLimit1," + returnList.get(1).get("string"));
				datas.add("condLimit2," + returnList.get(2).get("string"));
				
				datas.add("cons," + returnList.get(3).get("int").toString().replace(",", "/"));
				datas.add("fixedRate," + returnList.get(4).get("double").toString().replace(",", "/"));
				datas.add("varRate," + returnList.get(5).get("double").toString().replace(",", "/"));
				datas.add("condRate1," + returnList.get(6).get("double").toString().replace(",", "/"));
				datas.add("condRate2," + returnList.get(7).get("double").toString().replace(",", "/"));
				
				// datas: [cmd,ZigBeeSTS_cmdSetTariff, meterId,20179751, modemType,ZigBee, yyyymmdd,20180401, condLimit1,0, condLimit2,0, cons,0/100/300/600, fixedRate,10.5529/10.5529/10.5529/10.5529, varRate,0.8359/0.8359/0.8895/1.4035, condRate1,0.0/0.0/0.0/0.0, condRate2,0.0/0.0/0.0/0.0]
				log.info("datas: " + datas );
				
				// 갱신할 정보 세팅
				if (stsCurrentIdx > -1 && stsCurrentIdx != this.tariffIdx) {
					// 전송조건3) TariffType이 변경되는 경우엔, FutureTariff에 내용을 저장함.
					stsFutureIdx = this.tariffIdx;
					stsFutureDate = sysCDate;
					futureTransfer = writeDate;
					log.info("Contract[" + this.contractId + "] --> TariffType(Future) Change...");
				} else {
					// 위 경우 제외한 나머지는 CurrentTariff임.
					stsCurrentIdx = this.tariffIdx;
					stsCurrentDate = sysCDate;
					currentTransfer = writeDate;
					log.info("Contract[" + this.contractId + "] --> Current Tariff Setting");
				}

				map = this.executeZigBeeSTSByPass(meter.getModem().getMcu().getSysID(), meter.getModem().getDeviceSerial(), datas);
			} else if (sysFDate.length() == 8 && (futureTransfer.length() != 14 || stsFutureDate.compareTo(sysFDate) != 0)) {
				// 전송조건1) futureTransfer(다음 tariff 전송일)이 null이거나,
				// 전송조건2) future date가 system상 다음 적용일자와 일치하지 않을 경우.
				// 전송조건3) 단, sysFDate가 null이면, 전송해야할 다음 Tariff가 없는것이므로 그냥 넘어간다.
				
				// sysFDate
				List<Map<String, Object>> returnList = this.getTariffParam(sysFDate);
				
				datas.add("yyyymmdd," + returnList.get(0).get("string"));
				datas.add("condLimit1," + returnList.get(1).get("string"));
				datas.add("condLimit2," + returnList.get(2).get("string"));
				
				datas.add("cons," + returnList.get(3).get("int").toString().replace(",", "/"));
				datas.add("fixedRate," + returnList.get(4).get("double").toString().replace(",", "/"));
				datas.add("varRate," + returnList.get(5).get("double").toString().replace(",", "/"));
				datas.add("condRate1," + returnList.get(6).get("double").toString().replace(",", "/"));
				datas.add("condRate2," + returnList.get(7).get("double").toString().replace(",", "/"));
				
				// datas: [cmd,ZigBeeSTS_cmdSetTariff, meterId,20179751, modemType,ZigBee, yyyymmdd,20180401, condLimit1,0, condLimit2,0, cons,0/100/300/600, fixedRate,10.5529/10.5529/10.5529/10.5529, varRate,0.8359/0.8359/0.8895/1.4035, condRate1,0.0/0.0/0.0/0.0, condRate2,0.0/0.0/0.0/0.0]
				log.info("datas: " + datas );
				
				// 갱신할 정보 세팅
				stsFutureIdx = this.tariffIdx;
				stsFutureDate = sysFDate;
				futureTransfer = writeDate;
				log.info("Contract[" + this.contractId + "] --> Future Tariff Setting");
				
				map = this.executeZigBeeSTSByPass(meter.getModem().getMcu().getSysID(), meter.getModem().getDeviceSerial(), datas);
			} else { // 모두 유효하면, 할거없음.
				log.info("DONE: Contract[" + this.contractId + "] --> Tariff Set was already done.");
				return;
			}
			
			String result = map.get("result").toString();

			if (result.equals("fail")) {
				log.info("Meter Number [" + mdsId + "] is FAIL - " + map.get("reason").toString());
			} else if (result.equals("success")) {
				log.info("Meter Number [" + mdsId + "] is SUCCESS");
				tariffTypeDao = DataUtil.getBean(TariffTypeDao.class);

				if (stsCurrentIdx > -1) {
					TariffType tariffTypeC = tariffTypeDao.get(stsCurrentIdx);
					stscon.setCurrentTariffType(tariffTypeC);
				}

				if (stsCurrentDate.length() > 1) {
					stscon.setCurrentTariff(stsCurrentDate);
				}

				if (currentTransfer.length() > 1) {
					stscon.setCurrentTariffDate(currentTransfer);
				}

				if (stsFutureIdx > -1) {
					TariffType tariffTypeF = tariffTypeDao.get(stsFutureIdx);
					stscon.setFutureTariffType(tariffTypeF);
				}

				if (stsFutureDate.length() > 1) {
					stscon.setFutureTariff(stsFutureDate);
				}

				if (futureTransfer.length() > 1) {
					stscon.setFutureTariffDate(futureTransfer);
				}
			}
		}
		
		/** 변경 사항 저장 및 종료 */
		StsContractDao stsContractDao = DataUtil.getBean(StsContractDao.class);
		stsContractDao.saveOrUpdate(stscon);
		txstatus6.flush();
		txmanager.commit(txstatus6);
		log.info("##Contract[" + this.contractId + "] -> STS Tariff thread finished with no error");
		return;
	}
	
	// stscontract 조회, 생성, 갱신
	private StsContract getStsCon(Integer conid, String meterMds, String stsNumber) {
		TransactionStatus txstatus3 = null;
		StsContract stscon = null;

		try {
			log.info("INFO: Contract[" + conid + "] Get StsContract row..");
			txstatus3 = txmanager.getTransaction(null);
			txmanager.setDefaultTimeout(1800);
			StsContractDao stsContractDao = DataUtil.getBean(StsContractDao.class);
			stscon = stsContractDao.getRowByContractId(conid);

			if (stscon == null) {
				// null인경우 해당 contract_id로 생성해줌.
				stscon = new StsContract();
				ContractDao contractDao = DataUtil.getBean(ContractDao.class);
				Contract con = contractDao.get(this.contractId);
				stscon.setContract(con);
				stscon.setContractNumber(con.getContractNumber());
				stscon.setMdevId(meterMds);
				stscon.setStsNumber(stsNumber);
				stscon.setStsContractDate(DateTimeUtil.getDateString(new Date()));
				stsContractDao.add(stscon);
				txstatus3.flush();
				log.info("ADD: StsContract[" + conid + "] add..");
			} else {
				if (stscon.getMdevId() == null || !meterMds.equals(stscon.getMdevId()) || stscon.getStsNumber() == null
						|| !stsNumber.equals(stscon.getStsNumber())) {

					stscon.setMdevId(meterMds);
					stscon.setStsNumber(stsNumber);
					stsContractDao.saveOrUpdate(stscon);
					txstatus3.flush();
					log.info("UPDATE: Update stsCon Mdev[" + stscon.getMdevId() + "] -> Mdev[" + meterMds + "]..");
				}
			}
			
			txmanager.commit(txstatus3);

		} catch (Exception e) {
			log.info("Exception: Contract[" + this.contractId + "]");
			log.error(e, e);
			if (!txstatus3.isCompleted())
				txmanager.rollback(txstatus3);
		}

		return stscon;
	}
	
	// Tariff 일자 조회
	private Map<String, String> getSystemTariffDate(String _basicDate) {

		TransactionStatus txstatus2 = null;
		Map<String,String> tariffDates = new HashMap<String,String>();
		try {
			txstatus2 = txmanager.getTransaction(null);
			TariffEMDao tariffEmDao = DataUtil.getBean(TariffEMDao.class);
			
			// 1) System - Current Tariff Date
			String sctd = tariffEmDao.getCurrentAppliedTariffDate(_basicDate, this.tariffIdx, null);
			if(sctd!=null) {
				tariffDates.put("current", sctd);
			}else {
				tariffDates.put("current", "");
			}
			
			// 2) System - Future Tariff Date
			String sftd = tariffEmDao.getNextAppliedTariffDate(_basicDate, this.tariffIdx, null);
			if(sftd!=null) {
				tariffDates.put("future", sftd);
			}else {
				tariffDates.put("future", "");
			}
			
			txmanager.commit(txstatus2);
			
		}catch(Exception e) {
			log.info("Exception: Contract["+this.contractId+"] at getSystemTariffDate");
			log.error(e,e);
			if(!txstatus2.isCompleted())
				txmanager.rollback(txstatus2);
		}
		
		return tariffDates;
		
	}
	
	// 대상 일자에 해당하는 Tariff를 AsyncParam에 넣을수 있게 만들어 반환
//	private List<Map<String,Object>> getTariffParam(String targetDate, String modemType){
	private List<Map<String,Object>> getTariffParam(String targetDate){
		// tariff 가져오고, async param으로 만들어서, asynclog 생성, tcp전송..
		List<Map<String, Object>> paramList = new ArrayList<Map<String,Object>>();
		TransactionStatus txstatus4 = null;

		try {
			txstatus4 = txmanager.getTransaction(null);
			TariffTypeDao tariffTypeDao = DataUtil.getBean(TariffTypeDao.class);
			TariffEMDao tariffEmDao = DataUtil.getBean(TariffEMDao.class);
			
			// 1) Tariff 목록 가져오기
			TariffType tariffType = tariffTypeDao.get(this.tariffIdx);
			Map<String, Object> tariffParam = new HashMap<String, Object>();
	        tariffParam.put("tariffIndex", tariffType);
	        tariffParam.put("searchDate", targetDate);
	        List<TariffEM> listTariff = tariffEmDao.getApplyedTariff(tariffParam);
	        
	        if(listTariff.size() < 1) {
	        	log.info("Warning: Contract["+this.contractId+"] There are no Tariff Data for Type Index["+this.tariffIdx+"].");
	        	return null;
	        }
			
	        // 2) 정렬
	        Collections.sort(listTariff, new Comparator<TariffEM>() {

	            @Override
	            public int compare(TariffEM t1, TariffEM t2) {
	                return t1.getSupplySizeMin() < t2.getSupplySizeMin()? -1:1;
	            }
	        });
	        
	        // 3) STS 미터에 내릴수 있도록 tariff 요소 계산
	        //returnList: TariffEM의 각 cons(supply_size_min)별 항목
	        List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
	        String tariffName = tariffType.getName();
	        if("Residential".equals(tariffName)) {
	        	log.info("Contract["+this.contractId+"]: Residential Type of Tariff");
				for (int i = 0; i < listTariff.size(); i++) {
					TariffEM tariffSTSrow = listTariff.get(i);
					Map<String,Object> rowMap = new HashMap<String,Object>();
					Double d = tariffSTSrow.getSupplySizeMin();
					rowMap.put("cons", d.intValue());
					rowMap.put("fixedRate", tariffSTSrow.getServiceCharge());
					Double publicLevy = (tariffSTSrow.getActiveEnergyCharge() * tariffSTSrow.getTransmissionNetworkCharge());
					Double govLevy = (tariffSTSrow.getActiveEnergyCharge() * tariffSTSrow.getDistributionNetworkCharge());
					Double varRate = (tariffSTSrow.getActiveEnergyCharge() + publicLevy + govLevy);
					rowMap.put("varRate", Double.parseDouble( String.format( "%.4f" , varRate ) ));
					rowMap.put("condRate1", tariffSTSrow.getAdminCharge() == null ? 0d : -1*Double.parseDouble( String.format( "%.4f" , tariffSTSrow.getAdminCharge() ) ));
					rowMap.put("condRate2", tariffSTSrow.getMaxDemand() == null ? 0d : -1*Double.parseDouble( String.format( "%.4f" , tariffSTSrow.getMaxDemand() ) ));
					returnList.add(rowMap);
				}
				
			} else if("Non Residential".equals(tariffName)) {
				log.info("Contract["+this.contractId+"]: Non Residential Type of Tariff");
				for (int i = 0; i < listTariff.size(); i++) {
					TariffEM tariffSTSrow = listTariff.get(i);
					Map<String,Object> rowMap = new HashMap<String,Object>();
					
					Double d = tariffSTSrow.getSupplySizeMin();
					rowMap.put("cons", d.intValue());
					rowMap.put("fixedRate", tariffSTSrow.getServiceCharge());
					Double publicLevy = (tariffSTSrow.getActiveEnergyCharge() * tariffSTSrow.getTransmissionNetworkCharge());
					Double govLevy = (tariffSTSrow.getActiveEnergyCharge() * tariffSTSrow.getDistributionNetworkCharge());
					Double varRate = (tariffSTSrow.getActiveEnergyCharge() + publicLevy + govLevy) * (tariffSTSrow.getEnergyDemandCharge() + 1);
					rowMap.put("varRate", Double.parseDouble( String.format( "%.4f" , varRate ) ));
					rowMap.put("condRate1", tariffSTSrow.getAdminCharge() == null ? 0d : -1*Double.parseDouble( String.format( "%.4f" , tariffSTSrow.getAdminCharge())));
					rowMap.put("condRate2", tariffSTSrow.getRateRebalancingLevy() == null ? 0d : -1*Double.parseDouble( String.format( "%.4f" , tariffSTSrow.getRateRebalancingLevy())));
					returnList.add(rowMap);					
				}
			} else {
				log.info("Warning: Contract["+this.contractId+"] Not Supported TariffType [" + tariffName + "]");
				return null;
			}
	        
//			if (modemType == ModemType.ZigBee.toString()) {
//				return returnList;
//	        }
	        
	        // 4) AsyncCommand Param에 저장할 파라미터 생성
	        String cons = "";
	        String fixedRate = "";
	        String varRate = "";
	        String condRate1 = "";
	        String condRate2 = "";        
			int tariffSize = returnList.size();
			for (int i = 0; i < tariffSize; i++) {
	            Map<String, Object> map = returnList.get(i);
	            if(i == 0) {
	                cons = map.get("cons").toString();
	                fixedRate = map.get("fixedRate").toString();
	                varRate = map.get("varRate").toString();
	                condRate1 = map.get("condRate1").toString();
	                condRate2 = map.get("condRate2").toString();
	            } else {
	                cons += ","+map.get("cons").toString();
	                fixedRate += ","+map.get("fixedRate").toString();
	                varRate += ","+map.get("varRate").toString();
	                condRate1 += ","+map.get("condRate1").toString();
	                condRate2 += ","+map.get("condRate2").toString();
	            }
			}
			
			// 5) Param 입력 (컬럼 Num 1~8까지)
			paramList = new ArrayList<Map<String,Object>>();
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("string",targetDate);
			paramList.add(paramMap);
			
	        paramMap = new HashMap<String,Object>();
	        //condlimit1은 명령전송시 CommandAction_GG에서 프로퍼티 값을 사용. 0은 default값. 
	        paramMap.put("string","0"); 
	        paramList.add(paramMap);
	        
	        paramMap = new HashMap<String,Object>();
	        //condlimit2은 명령전송시 CommandAction_GG에서 프로퍼티 값을 사용. 0은 default값.
	        paramMap.put("string","0");
	        paramList.add(paramMap);
	        
	        paramMap = new HashMap<String,Object>();
	        paramMap.put("int", cons);
	        paramList.add(paramMap);
	        
	        paramMap = new HashMap<String,Object>();
	        paramMap.put("double", fixedRate);
	        paramList.add(paramMap);
	        
	        paramMap = new HashMap<String,Object>();
	        paramMap.put("double", varRate);
	        paramList.add(paramMap);
	        
	        paramMap = new HashMap<String,Object>();
	        paramMap.put("double", condRate1);
	        paramList.add(paramMap);
	        
	        paramMap = new HashMap<String,Object>();
	        paramMap.put("double", condRate2);
	        paramList.add(paramMap);
	        
	        txmanager.commit(txstatus4);
		} catch(Exception e) {
			log.info("Exception: Contract["+this.contractId+"] at GetTariffParam.");
			log.error(e,e);
			if(!txstatus4.isCompleted())
				txmanager.rollback(txstatus4);
			return null;
		}
		 
	
		return paramList;
	}

	
	/** MMIU Tariff 전송 */
	private boolean sendTariff_MMIU(List<Map<String, Object>> paramList, String writeDate) {
		TransactionStatus txstatus5 = null;
		boolean isConnect = false;

		try {
			txstatus5 = txmanager.getTransaction(null);

			// 주어진 meterId를 통해 모뎀을 찾아 IP주소 체크
			MeterDao meterDao = DataUtil.getBean(MeterDao.class);
			Meter meter = meterDao.get(this.meterId);
			Modem modem = meter.getModem();

			if (modem == null) {
				// 모뎀이 Null이면 전송할 대상이 없으므로 취소
				log.info("Warning: Contract[" + this.contractId + "] Null Modem of meterid[" + this.meterId
						+ "], SKIP.");
			} else {
				String ipAddr = modem.getIpAddr() == null ? "" : modem.getIpAddr();
				String sid = modem.getDeviceSerial();

				// AsyncCommand Log,Param 저장
				this.saveAsyncCommandList(sid, "cmdSetTariff", paramList, writeDate);
				this.setSid(sid);
				// TODO 전체 구문 포함하기(분리해서 에러나면..)

				txmanager.commit(txstatus5);

				// TCP Connection (ip가 있으면 진행, 아니면 실패임)
				if (ipAddr.length() > 6) {
					// Minimum length of IP address is 7.
					isConnect = cmdTCPTrigger("cmdSetSTSToken", ipAddr);

					log.info("Contract[" + this.contractId + "]: TCP Connection Result [" + sid + "] : " + isConnect);
				} else {
					log.info("Contract[" + this.contractId + "]: Modem has No IP Address [" + sid + "] ");
				}
			}

		} catch (Exception e) {
			log.info("Exception: Contract[" + this.contractId + "] at SendTariff.");
			log.error(e, e);
			if (txstatus5 != null && !txstatus5.isCompleted())
				txmanager.rollback(txstatus5);
		} // ~ try-catch
		

		// TCP 연결 성공/실패 결과 반환
		return isConnect;
	}
	
	
	// AsyncCommand Log,Param 저장		
	private void saveAsyncCommandList(String deviceSerial, String cmd, List<Map<String, Object>> paramList, String currentTime) {
		AsyncCommandLogDao asyncLogDao = DataUtil.getBean(AsyncCommandLogDao.class);
		AsyncCommandParamDao asyncParamDao = DataUtil.getBean(AsyncCommandParamDao.class);
		
		AsyncCommandLog asyncCommandLog = new AsyncCommandLog();
        long trId = System.currentTimeMillis();
        asyncCommandLog.setTrId(trId);
        asyncCommandLog.setMcuId(deviceSerial);
        asyncCommandLog.setDeviceType(McuType.MMIU.name());
        asyncCommandLog.setDeviceId(deviceSerial);
        asyncCommandLog.setCommand(cmd);
        asyncCommandLog.setTrOption(TR_OPTION.ASYNC_OPT_RETURN_DATA_SAVE.getCode());
        asyncCommandLog.setState(TR_STATE.Waiting.getCode());
        asyncCommandLog.setOperator(OperatorType.OPERATOR.name());
        asyncCommandLog.setCreateTime(currentTime);
        asyncCommandLog.setRequestTime(currentTime);
        asyncCommandLog.setLastTime(null);
        asyncLogDao.add(asyncCommandLog);
        
        Integer num = 0;
        if (paramList != null && paramList.size() > 0) {
            //parameter가 존재할 경우.
            Integer maxNum = asyncParamDao.getMaxNum(deviceSerial, trId);
            if (maxNum != null)
                num = maxNum + 1;

            for (int i = 0; i < paramList.size(); i++) {
                Map<String,Object> param = paramList.get(i);
                Iterator<String> iter = param.keySet().iterator();
                while (iter.hasNext()) {
                    String key = iter.next();

                    AsyncCommandParam asyncCommandParam = new AsyncCommandParam();
                    asyncCommandParam.setMcuId((String)deviceSerial);
                    asyncCommandParam.setNum(num);
                    asyncCommandParam.setParamType((String)key);
                    asyncCommandParam.setParamValue((String) param.get(key));
                    asyncCommandParam.setTrId(trId);

                    asyncParamDao.add(asyncCommandParam);
                    num += 1;
                }
            }
        }
	
	}
	
	// TCP Connection
	private boolean cmdTCPTrigger(String cmd, String ipAddr) {
		//log.info("[cmdTCPTrigger] Source Command: " + cmd + " IP: " + ipAddr);
		CmdOperationUtil cmdOperationUtil = DataUtil.getBean(CmdOperationUtil.class);
		boolean isConnect= false;
		try {			
			isConnect = cmdOperationUtil.cmdTCPTrigger(cmd, ipAddr);
			if(!isConnect) {
				// 1회 재시도..
				isConnect = cmdOperationUtil.cmdTCPTrigger(cmd, ipAddr);
			}
			
		} catch (Exception e) {
			log.error(e, e);
		}
		return isConnect;
	}
	
	private Map<String, Object> executeZigBeeSTSByPass(String mcuSysId, String deviceSerial, List<java.lang.String> datas) {
		CmdOperationUtil cmdOperationUtil = DataUtil.getBean(CmdOperationUtil.class);
		Map<String, Object> resultMap = createZigbeeTunnel(mcuSysId, deviceSerial);
		Map<String, Object> map = new HashMap<String, Object>();

		// Create Tunnel & Use ByPass (S)
		try {
			if ((Boolean) resultMap.get("result") == true) {
				String portNumber = resultMap.get("rtnStr").toString();
				map = cmdOperationUtil.cmdConnectByPass(mcuSysId, Integer.valueOf(portNumber), datas);
				log.info("returned map: " + map);
			} else {
				map.put("result", "fail");
				map.put("reason", resultMap.get("rtnStr").toString());
			}
		} catch (Exception e) {
			deleteZigbeeTunnel(mcuSysId, deviceSerial);

			map.put("result", "fail");
			map.put("reason", "Data Connect Error[" + e.getMessage() + "]");

			return map;
		}
		// Create Tunnel & Use ByPass (E)

		// Delete Tunnel (S)
		try {
			deleteZigbeeTunnel(mcuSysId, deviceSerial);
		} catch (Exception e) {
			map.put("result", "fail");
			map.put("reason", "Tunnel Delete Error [" + e.getMessage() + "]");

			return map;
		}
		// Delete Tunnel (E)

		return map;
	}

	private Map<String, Object> createZigbeeTunnel(String mcuSysId, String modemEuiId) {
		CmdOperationUtil cmdOperationUtil = DataUtil.getBean(CmdOperationUtil.class);
		final int MSG_TIMEOUT = 60;
		final int TUNNEL_TIMEOUT = 100;

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", false);

		int portNumber = 0;
		String rtnStr = "Create Zigbee Tunnel - FAIL";

		try {
			Map resultTable = cmdOperationUtil.cmdCreateTunnel2(mcuSysId, modemEuiId, MSG_TIMEOUT, TUNNEL_TIMEOUT);

			if (resultTable != null && resultTable.size() > 0) {
				Iterator<String> keys = resultTable.keySet().iterator();

				String keyVal = null;
				while (keys.hasNext()) {
					keyVal = (String) keys.next();
					portNumber = Integer.parseInt(resultTable.get(keyVal).toString());
					break;
				}

				if (portNumber == 0) {
					resultMap.put("rtnStr", rtnStr);
					return resultMap;
				}
			}
		} catch (Exception ex) {
			resultMap.put("rtnStr", "Create Zigbee Tunnel - Exception[" + ex.getMessage() + "]");
			return resultMap;
		}

		log.info("Create Zigbee Tunnel mcuSysId [" + mcuSysId + "], modemEuiId [" + modemEuiId + "], port ["
				+ portNumber + "]");

		resultMap.put("result", true);
		resultMap.put("rtnStr", String.valueOf(portNumber));
		return resultMap;
	}

	public Boolean deleteZigbeeTunnel(String mcuSysId, String modemEuiId) {
		CmdOperationUtil cmdOperationUtil = DataUtil.getBean(CmdOperationUtil.class);
		
		try {
			cmdOperationUtil.cmdDeleteTunnel2(mcuSysId, modemEuiId);
			return true;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		}
	}

}
