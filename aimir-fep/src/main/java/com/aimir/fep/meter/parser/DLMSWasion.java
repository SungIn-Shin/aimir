package com.aimir.fep.meter.parser;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aimir.constants.CommonConstants.TargetClass;
import com.aimir.fep.command.conf.DLMSMeta.CONTROL_STATE;
import com.aimir.fep.command.conf.DLMSMeta.LOAD_CONTROL_STATUS;
import com.aimir.fep.meter.data.BillingData;
import com.aimir.fep.meter.data.EventLogData;
import com.aimir.fep.meter.data.Instrument;
import com.aimir.fep.meter.data.LPData;
import com.aimir.fep.meter.data.STSLog;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSECGTable;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSECGTable.TOKEN_HISTORY_OBIS;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSSCALAR;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.DEVICE_STATUS_ALARM;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.DLMS_CLASS_ATTR;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.ENERGY_LOAD_PROFILE;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.EVENT_LOG;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.FUNCTION_STATUS_ALARM;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.MEASUREMENT_STATUS_ALARM;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.OBIS;
import com.aimir.fep.meter.parser.DLMSECGTable.LPComparator;
import com.aimir.fep.protocol.fmp.datatype.OCTET;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.EventUtil;
import com.aimir.fep.util.Hex;
import com.aimir.fep.util.Util;
import com.aimir.model.device.EnergyMeter;
import com.aimir.model.device.Modem;
import com.aimir.model.system.Supplier;
import com.aimir.util.DateTimeUtil;
import com.aimir.util.TimeLocaleUtil;
import com.aimir.util.TimeUtil;

public class DLMSWasion extends MeterDataParser implements java.io.Serializable {

	private static final long serialVersionUID = 5198091223934578776L;

	private static Log log = LogFactory.getLog(DLMSWasion.class);

	LPData[] lpData = null;

	Map<String, Object> data = new LinkedHashMap<String, Object>(16, 0.75f, false);
	LinkedHashMap<String, Map<String, Object>> result = new LinkedHashMap<String, Map<String, Object>>();
	List<LinkedHashMap<String, Object>> tokenChargeData = new ArrayList<LinkedHashMap<String, Object>>();
	
	String meterID = "";
	String modemID = "";
	String fwVersion = "";
	String otaFwVersion = "";
	String meterModel = "";
	String logicalNumber = "";
	String manufactureSerial = "";
	String servicePointSerial = "";
	Long ct_num = 0L;
	Long vt_num = 0L;
	Double ct_den = null;
	Double vt_den = null;
	Long trans_num = 0L;
	String phaseType = null;
	byte[] relayStatus = null; //Internal Disconnect/reconnect
	byte paymentMode = 0x00;
	Double totalCredit = null;

	int meterActiveConstant = 1;
	int meterReActiveConstant = 1;

	double activePulseConstant = 1;
	double reActivePulseConstant = 1;
	
	public BillingData getCurrBill() {
		return currBill;
	}

	public List<BillingData> getCurrBillLog() {
		return currBillLog;
	}

	public BillingData getPreviousMonthBill() {
		return previousMonthBill;
	}

	public List<BillingData> getDailyBill() {
		return dailyBill;
	}

	public void setDailyBill(List<BillingData> dailyBill) {
		this.dailyBill = dailyBill;
	}

	public List<BillingData> getMonthlyBill() {
		return monthlyBill;
	}

	public void setMonthlyBill(List<BillingData> monthlyBill) {
		this.monthlyBill = monthlyBill;
	}
	
	public Instrument[] getPowerQuality() {
		return instruments;
	}

	public void setPowerQuality(Instrument[] instruments) {
		this.instruments = instruments;
	}	

	public LinkedHashMap<String, Map<String, Object>> getResult() {
		return result;
	}

	public void setResult(LinkedHashMap<String, Map<String, Object>> result) {
		this.result = result;
	}

	private List<EventLogData> meterAlarmLog = null;
	private List<EventLogData> rolloverAlarmLog = null;
	private List<BillingData> currBillLog = null;
	private BillingData currBill = null;
	private BillingData previousMonthBill = null;
	private List<BillingData> dailyBill = null;
	private List<BillingData> monthlyBill = null;
	private Instrument[] instruments = null;
	private List<STSLog> stsLogs = null;

	Long ctRatio = 1L;

	int lpInterval = 60;

	Double meteringValue = 0d;
	Double ct = null;
	Double vt = null;

	private final int CUMULATIVE_ACTIVE_ENERY = 0;
	private final int CONSUMPTION_ACTIVE_ENERGY = 10;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public LinkedHashMap<String, Map<String, Object>> getData() {

		Map<String, Object> resultSubData = null;
		String key = null;

		DecimalFormat decimalf = null;
		SimpleDateFormat datef14 = null;

		if (meter != null && meter.getSupplier() != null) {
			Supplier supplier = meter.getSupplier();
			if (supplier != null) {
				String lang = supplier.getLang().getCode_2letter();
				String country = supplier.getCountry().getCode_2letter();
				decimalf = TimeLocaleUtil.getDecimalFormat(supplier);
				datef14 = new SimpleDateFormat(TimeLocaleUtil.getDateFormat(14, lang, country));
			}
		} else {
			//locail 정보가 없을때는 기본 포멧을 사용한다.
			decimalf = new DecimalFormat();
			datef14 = new SimpleDateFormat();
		}

		String clock = meterTime;
		try {
			clock = datef14.format(DateTimeUtil.getDateFromYYYYMMDDHHMMSS(meterTime));
		} catch (Exception e) {
			e.printStackTrace();
		}
		data.put("Meter Clock", clock);
		
		for (Iterator i = result.keySet().iterator(); i.hasNext();) {
			key = (String) i.next();
			resultSubData = result.get(key);

			if (resultSubData != null) {
				String idx = "";
				if (key.lastIndexOf("-") != -1) {
					idx = key.substring(key.lastIndexOf("-") + 1);
					key = key.substring(0, key.lastIndexOf("-"));
				}
				String subkey = null;
				Object subvalue = null;
				for (Iterator subi = resultSubData.keySet().iterator(); subi.hasNext();) {
					subkey = (String) subi.next();
					
					if (!subkey.contains(DLMSVARIABLE.UNDEFINED)) {
						subvalue = resultSubData.get(subkey);
						if (subvalue instanceof String) {
							if (((String) subvalue).contains(":date=")) {
								try {
									data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), datef14.format(DateTimeUtil.getDateFromYYYYMMDDHHMMSS(((String) subvalue).substring(6) + "00")));
								} catch (Exception e) {
									data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), subvalue);
								}
							} else if (subkey.contains("Date") && !((String) subvalue).contains(":date=") && ((String) subvalue).length() == 12) {
								try {
									data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), datef14.format(DateTimeUtil.getDateFromYYYYMMDDHHMMSS(subvalue + "00")));
								} catch (Exception e) {
									data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), subvalue);
								}
							} else {
								data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), subvalue);
							}

						} else if (subvalue instanceof Number) {
							
							if (subvalue instanceof Long && !OBIS.getObis(key).getName().endsWith("Number") && !OBIS.getObis(key).getName().endsWith("Den") && !OBIS.getObis(key).getName().endsWith("num)") 
									&& !subkey.equals("LpInterval") 
									&& !subkey.equals("PowerLoadProfileInterval")) {
								
								if(key.equals(OBIS.POWER_FACTOR_L1.getCode()) || key.equals(OBIS.POWER_FACTOR_L2.getCode())
										|| key.equals(OBIS.POWER_FACTOR_L3.getCode()) || key.equals(OBIS.TOTAL_POWER_FACTOR.getCode())) {
									// power factor							
									data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), decimalf.format(new Double(((Long) subvalue) * 0.0001)));
								} else if(key.equals(OBIS.SUPPLY_FREQUENCY_L1.getCode()) || key.equals(OBIS.SUPPLY_FREQUENCY_L2.getCode())
										|| key.equals(OBIS.SUPPLY_FREQUENCY_L3.getCode()) || key.equals(OBIS.TOTAL_SUPPLY_FREQUENCY.getCode())) {
									// frequency
									data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), decimalf.format(new Double(((Long) subvalue) * 0.01)));
								} else if(key.equals(OBIS.CT.getCode()) || key.equals(OBIS.CT_DEN.getCode())
										|| key.equals(OBIS.VT.getCode()) || key.equals(OBIS.VT_DEN.getCode())) {
									// ct, vt
									data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), decimalf.format(new Double(((Long) subvalue))));	
								} else {
									data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), decimalf.format(new Double(((Long) subvalue) * 0.001)));
								}
							} else {
								data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), decimalf.format(subvalue));
							}

						} else if (subvalue instanceof OCTET) {
							data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), ((OCTET) subvalue).toHexString());
						} else if (subvalue instanceof LOAD_CONTROL_STATUS) {
							data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), ((LOAD_CONTROL_STATUS) subvalue).name());
						} else if (subvalue instanceof CONTROL_STATE) {
							data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), ((CONTROL_STATE) subvalue).name());
						} else
							data.put(getKey(OBIS.getObis(key).getName(), idx, subkey), subvalue);
					}
				}
			}
		}

		return (LinkedHashMap) data;
	}

	private String getKey(String mainKey, String idx, String subKey) {

		if (mainKey.equals(subKey) && "".equals(idx)) {
			return mainKey + idx;
		} else {
			return mainKey + idx + " : " + subKey;
		}
	}

	@Override
	public void parse(byte[] data) throws Exception {
		// 로그 확인 편하도록....
		log.info("    ");
		log.info("    ");
		log.info("    ");
		log.info("############################## OBIS Parsing Start!! ################################################");
		log.info("##### Meter[" + meter.getMdsId() + "] DLMS parse:" + Hex.decode(data));

		String obisCode = "";
		int clazz = 0;
		int attr = 0;
		int pos = 0;
		int len = 0;
		// DLMS Header OBIS(6), CLASS(1), ATTR(1), LENGTH(2)
		// DLMS Tag Tag(1), DATA or LEN/DATA (*)
		byte[] OBIS = new byte[6];
		byte[] CLAZZ = new byte[2];
		byte[] ATTR = new byte[1];
		byte[] LEN = new byte[2];
		byte[] TAGDATA = null;

		DLMSECGTable dlms = null;

		if (meter != null) {
			log.info("DLMS Meter Name : [" + meter.getModel().getName() + "] , MeterId [" + meter.getMdsId() + "]");
		}

		while (pos < data.length) {
//			log.debug("-----------------------------------------------------------------------");
			dlms = new DLMSECGTable();
			System.arraycopy(data, pos, OBIS, 0, OBIS.length);
			pos += OBIS.length;
			obisCode = Hex.decode(OBIS);
			dlms.setObis(obisCode);

			System.arraycopy(data, pos, CLAZZ, 0, CLAZZ.length);
			pos += CLAZZ.length;
			clazz = DataUtil.getIntTo2Byte(CLAZZ);
			dlms.setClazz(clazz);

			System.arraycopy(data, pos, ATTR, 0, ATTR.length);
			pos += ATTR.length;
			attr = DataUtil.getIntToBytes(ATTR);
			dlms.setAttr(attr);

			System.arraycopy(data, pos, LEN, 0, LEN.length);
			pos += LEN.length;
			len = DataUtil.getIntTo2Byte(LEN);
			dlms.setLength(len);

			TAGDATA = new byte[len];
			if (pos + TAGDATA.length <= data.length) {
				System.arraycopy(data, pos, TAGDATA, 0, TAGDATA.length);
				pos += TAGDATA.length;
			} else {
				System.arraycopy(data, pos, TAGDATA, 0, data.length - pos);
				pos += data.length - pos;
			}

			log.debug("OBIS[" + obisCode + "] CLASS[" + clazz + "] ATTR[" + attr + "] LENGTH[" + len + "] TAGDATA=[" + Hex.decode(TAGDATA) + "]");

			dlms.setMeter(meter);
			dlms.parseDlmsTag(TAGDATA);
			Map<String, Object> dlmsData = dlms.getData();
			
			if(dlmsData == null)
				continue;
			
			if (dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.ENERGY_LOAD_PROFILE 
					|| dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.POWER_LOAD_PROFILE
					|| dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.DAILY_LOAD_PROFILE 
					|| dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.MONTHLY_BILLING 
					|| dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.STANDARD_EVENT 
					|| dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.RELAY_EVENT
					|| dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.FRAUDDETECTIONLOGEVENT 
					|| dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.MEASUREMENT_EVENT
					|| dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.ENERGY_OVERFLOW_EVENTLOG
					|| dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.TOKEN_CREDIT_HISTORY) {

				for (int cnt = 0;; cnt++) {
					obisCode = dlms.getDlmsHeader().getObis().getCode() + "-" + cnt;
					if (!result.containsKey(obisCode)) {
						result.put(obisCode, dlmsData);
						break;
					}
				}
			} else if (dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.METER_TIME && dlms.getDlmsHeader().getAttr() != DLMS_CLASS_ATTR.CLOCK_ATTR02) {
				result.put(obisCode + "-" + dlms.getDlmsHeader().getAttr().getAttr(), dlmsData);
			} else if ((dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.TOTAL_MAX_DEMAND_IMPORT
					 || dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.TOTAL_MAX_DEMAND_IMPORT_T1
					 || dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.TOTAL_MAX_DEMAND_IMPORT_T2
					 || dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.TOTAL_MAX_DEMAND_IMPORT_T3) 
					&& dlms.getDlmsHeader().getAttr() == DLMS_CLASS_ATTR.REGISTER_ATTR05) {
				result.put(obisCode + "-" + "DateTime", dlmsData);				
			} else if(dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.METER_MODEL){
				meterModel = dlmsData.get("Meter Model").toString();
				result.put(obisCode, dlmsData);
			} else if(dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.PAYMENT_MODE_SETTING) {
				result.put(obisCode, dlmsData);
			} else if(dlms.getDlmsHeader().getObis() == DLMSVARIABLE.OBIS.REMAINING_CREDIT) {
				result.put(obisCode, dlmsData);
			} else if (dlmsData != null && !dlmsData.isEmpty()) {
				result.put(obisCode, dlmsData);
			} 
		}
		
		EnergyMeter meter = (EnergyMeter) this.getMeter();

//		this.ct = 1.0;
//		if (meter != null && meter.getCt() != null && meter.getCt() > 0) {
//			ct = meter.getCt();
//		}
		if (meter != null && meter.getLpInterval() != null && meter.getLpInterval() > 0) {
			lpInterval = meter.getLpInterval();
		}
				
		setMeterInfo();
		setLPData();
		setDailyBillingData();
		setMonthlyBillingData();
		setAlarmLog();
		setPowerQualityData();
		setOverflowEventlogData();
		setTokenHistory();
		setSTSLog();
	}

	private void setSTSLog() {
		log.debug("##### STS LOG START #####");
		
		try {
			Map<String, Object> map = null;
			
			if(meterTime != null) {
				String hhmmss = meterTime.substring(8);
				String setDate = meterTime.substring(0, 8); // :date= 제거
				String setTime = hhmmss.substring(0, 6);
				
				map = (Map<String, Object>) result.get(OBIS.PAYMENT_MODE_SETTING.getCode());
				log.debug("## STS_PAYMENT_MODE(Function STS Message) Eevnt count = " + (map == null ? 0 : map.size()));
				if (map != null) {
					log.debug("#######################################");
					log.debug("#######################################");
					log.debug("#######################################");
					log.debug("#######################################");
				}
			}
			
		}catch(Exception e) {
			log.error(e, e);
		}
		
		log.debug("##### STS LOG END #####");
	}
	
	private void setAlarmLog() {

		byte[] meterStatus = null;
		String meterStatusBitString = "";

		byte[] measurementStatus = null;
		String measurementStatusBitString = "";

		byte[] deviceStatus = null;
		String deviceStatusBitString = "";

		meterAlarmLog = new ArrayList<EventLogData>();

		log.debug("##### ALARM LOG START #####");
		try {
			Map<String, Object> map = null;

			/*
			 * 기준시간 설정 Alaram은 시간값이 없기때문에 미터시간을 기준으로 저장한다
			 */
			EventLogData e = null;
			
			if(meterTime != null) {
			
				String hhmmss = meterTime.substring(8);
				String setDate = meterTime.substring(0, 8); // :date= 제거
				String setTime = hhmmss.substring(0, 6);
	
				/*
				 * 0.0.96.10.5.255 METER_STATUS : Function Alarm Message 처리
				 * MeterEvent Log 로 저장 하지만 알람정보 자체가 시간값이 없기때문에 미터 시간 기준으로 저장한다.
				 */
				map = (Map<String, Object>) result.get(OBIS.METER_STATUS.getCode());
				log.debug("## METER_STATUS(Function Alarm Message) Eevnt count = " + (map == null ? 0 : map.size()));
				if (map != null) {
					log.debug("[METER_STATUS] => " + map.toString());
					Object obj = map.get(OBIS.METER_STATUS.getName());
					if (obj != null) {
						//meterStatus = ((OCTET)obj).getValue(); // 2byte
						meterStatus = DataUtil.readByteString(obj.toString()); // 2byte HEX code
						meterStatusBitString = DataUtil.byteArrayToBinaryString(meterStatus);
						log.debug("[METER_STATUS:BitString] => " + meterStatusBitString);
	
						// Function Alarm 의 상태값을 비교해서 저장하기 위해 METER_CAUTION 컬럼을 사용한다.  
						EnergyMeter meter = (EnergyMeter) this.getMeter();
						if (meter.getMeterCaution() != null && meter.getMeterCaution().length() != 16) { // 2byte
							meter.setMeterCaution(null);
						}
	
						// METER_STATUS 값이 없거나 변경됬을때만 이벤트로 등록한다.
						if (meter.getMeterCaution() == null || !meter.getMeterCaution().equals(meterStatusBitString)) {
							int prevState = 0;
							int curState = 0;
	
							for (int value = 0; value < meterStatusBitString.length(); value++) {
								if (meter.getMeterCaution() == null) {
									prevState = 0;
								} else {
									prevState = Integer.parseInt(meter.getMeterCaution().substring(value, value + 1));
								}
	
								curState = Integer.parseInt(meterStatusBitString.substring(value, value + 1));
								if (prevState == 0 && curState == 1) {
									e = new EventLogData();
									e.setDate(setDate); // :date= 제거
									e.setTime(setTime);
									e.setFlag(EVENT_LOG.FunctionAlarm.getFlag());
									e.setKind("STE");
									e.setMsg(EVENT_LOG.FunctionAlarm.getMsg());
	
									switch (value) {
									case 0: // L1 relay status
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.L1RelayStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L1RelayStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L1RelayStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.L1RelayStatus.getMsg() + "] " + e.toString());
										break;
									case 1: // L2 relay status
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.L2RelayStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L2RelayStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L2RelayStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.L2RelayStatus.getMsg() + "] " + e.toString());
										break;
									case 2: // L3 relay status
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.L3RelayStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L3RelayStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L3RelayStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.L3RelayStatus.getMsg() + "] " + e.toString());
										break;
									case 3: // External relay status
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.ExternalRelayStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.ExternalRelayStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.ExternalRelayStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.ExternalRelayStatus.getMsg() + "] " + e.toString());
										break;
									case 4: // L1 relay error
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.L1RelayError.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L1RelayError.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L1RelayError.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.L1RelayError.getMsg() + "] " + e.toString());
										break;
									case 5: // L2 relay error
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.L2RelayError.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L2RelayError.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L2RelayError.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.L2RelayError.getMsg() + "] " + e.toString());
										break;
									case 6: // L3 relay error
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.L3RelayError.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L3RelayError.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L3RelayError.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.L3RelayError.getMsg() + "] " + e.toString());
										break;
									case 7: // External relay error
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.ExternalRelayError.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.ExternalRelayError.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.ExternalRelayError.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.ExternalRelayError.getMsg() + "] " + e.toString());
										break;
									case 8: // Open terminal cover
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.OpenTerminalCover.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.OpenTerminalCover.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.OpenTerminalCover.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.OpenTerminalCover.getMsg() + "] " + e.toString());
										break;
									case 9: // Open terminal cover in power off 
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.OpenTerminalCoverInPowerOff.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.OpenTerminalCoverInPowerOff.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.OpenTerminalCoverInPowerOff.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.OpenTerminalCoverInPowerOff.getMsg() + "] " + e.toString());
										break;
									case 10: // Open top cover
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.OpenTopCover.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.OpenTopCover.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.OpenTopCover.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.OpenTopCover.getMsg() + "] " + e.toString());
										break;
									case 11: // Open top cover in power off
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.OpenTopCoverInPowerOff.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.OpenTopCoverInPowerOff.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.OpenTopCoverInPowerOff.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.OpenTopCoverInPowerOff.getMsg() + "] " + e.toString());
										break;
									case 12: // Magnetic detection 1
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.MagneticDetection1.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.MagneticDetection1.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.MagneticDetection1.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.MagneticDetection1.getMsg() + "] " + e.toString());
										break;
									case 13: // Magnetic detection 2
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.MagneticDetection2.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.MagneticDetection2.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.MagneticDetection2.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.MagneticDetection2.getMsg() + "] " + e.toString());
										break;
									case 14: // program
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.Program.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.Program.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.Program.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.Program.getMsg() + "] " + e.toString());
										break;
									case 15: // Factory status
										e.setAppend("[ON]" + FUNCTION_STATUS_ALARM.FactoryStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.FactoryStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.FactoryStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][ON][" + FUNCTION_STATUS_ALARM.FactoryStatus.getMsg() + "] " + e.toString());
										break;
									default:
										break;
									}
									meterAlarmLog.add(e);
								} else if (prevState == 1 && curState == 0) {
									e = new EventLogData();
									e.setDate(setDate); // :date= 제거
									e.setTime(setTime);
									e.setFlag(EVENT_LOG.FunctionAlarm.getFlag());
									e.setKind("STE");
									e.setMsg(EVENT_LOG.FunctionAlarm.getMsg());
	
									switch (value) {
									case 0: // L1 relay status
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.L1RelayStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L1RelayStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L1RelayStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.L1RelayStatus.getMsg() + "] " + e.toString());
										break;
									case 1: // L2 relay status
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.L2RelayStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L2RelayStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L2RelayStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.L2RelayStatus.getMsg() + "] " + e.toString());
										break;
									case 2: // L3 relay status
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.L3RelayStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L3RelayStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L3RelayStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.L3RelayStatus.getMsg() + "] " + e.toString());
										break;
									case 3: // External relay status
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.ExternalRelayStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.ExternalRelayStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.ExternalRelayStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.ExternalRelayStatus.getMsg() + "] " + e.toString());
										break;
									case 4: // L1 relay error
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.L1RelayError.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L1RelayError.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L1RelayError.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.L1RelayError.getMsg() + "] " + e.toString());
										break;
									case 5: // L2 relay error
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.L2RelayError.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L2RelayError.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L2RelayError.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.L2RelayError.getMsg() + "] " + e.toString());
										break;
									case 6: // L3 relay error
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.L3RelayError.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.L3RelayError.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.L3RelayError.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.L3RelayError.getMsg() + "] " + e.toString());
										break;
									case 7: // External relay error
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.ExternalRelayError.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.ExternalRelayError.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.ExternalRelayError.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.ExternalRelayError.getMsg() + "] " + e.toString());
										break;
									case 8: // Open terminal cover
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.OpenTerminalCover.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.OpenTerminalCover.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.OpenTerminalCover.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.OpenTerminalCover.getMsg() + "] " + e.toString());
										break;
									case 9: // Open terminal cover in power off 
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.OpenTerminalCoverInPowerOff.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.OpenTerminalCoverInPowerOff.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.OpenTerminalCoverInPowerOff.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.OpenTerminalCoverInPowerOff.getMsg() + "] " + e.toString());
										break;
									case 10: // Open top cover
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.OpenTopCover.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.OpenTopCover.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.OpenTopCover.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.OpenTopCover.getMsg() + "] " + e.toString());
										break;
									case 11: // Open top cover in power off
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.OpenTopCoverInPowerOff.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.OpenTopCoverInPowerOff.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.OpenTopCoverInPowerOff.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.OpenTopCoverInPowerOff.getMsg() + "] " + e.toString());
										break;
									case 12: // Magnetic detection 1
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.MagneticDetection1.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.MagneticDetection1.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.MagneticDetection1.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.MagneticDetection1.getMsg() + "] " + e.toString());
										break;
									case 13: // Magnetic detection 2
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.MagneticDetection2.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.MagneticDetection2.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.MagneticDetection2.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.MagneticDetection2.getMsg() + "] " + e.toString());
										break;
									case 14: // program
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.Program.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.Program.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.Program.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.Program.getMsg() + "] " + e.toString());
										break;
									case 15: // Factory status
										e.setAppend("[OFF]" + FUNCTION_STATUS_ALARM.FactoryStatus.getMsg());
										e.setFlag(FUNCTION_STATUS_ALARM.FactoryStatus.getFlag());
										e.setMsg(FUNCTION_STATUS_ALARM.FactoryStatus.getMsg());
										log.debug("[METER_STATUS - Function Alarm][OFF][" + FUNCTION_STATUS_ALARM.FactoryStatus.getMsg() + "] " + e.toString());
										break;
									default:
										break;
									}
	
									meterAlarmLog.add(e);
								}
							}
	
							meter.setMeterCaution(meterStatusBitString);
						}
					}
	
					result.remove(OBIS.METER_STATUS.getCode());
				}
	
				// 기존코드 나중에 필요없으면 삭제할것.              
				//            map = (Map<String, Object>)result.get(OBIS.METER_STATUS.getCode());
				//            if (map != null) {
				//	        	Object obj = map.get(OBIS.METER_STATUS.getName());
				//	        	if (obj != null) meterStatus = ((OCTET)obj).getValue();	        	
				//	        	meterStatusStr = DLMSTable.getOBIS_FUNCTION_STATUS((OCTET)obj);
				//	        	log.debug("METER_STATUS[" + Hex.decode(meterStatus) + "], OBIS_FUNCTION_STATUS["+ meterStatusStr + "]");	        	
				//	        	data.put(OBIS.METER_STATUS.getName(), meterStatusStr);
				//	        	result.remove(OBIS.METER_STATUS.getCode());
				//            }
	
				/*
				 * 0.0.96.10.6.255 DRIVE_STATUS : Drive(Device) Alarm Message 처리
				 * MeterEvent Log 로 저장 하지만 알람정보 자체가 시간값이 없기때문에 미터 시간 기준으로 저장한다.
				 */
				map = (Map<String, Object>) result.get(OBIS.DRIVE_STATUS.getCode());
				log.debug("## DRIVE_STATUS(Drive(Device) Alarm Message) Eevnt count = " + (map == null ? 0 : map.size()));
				if (map != null) {
					log.debug("[DRIVE_STATUS] => " + map.toString());
					Object obj = map.get(OBIS.DRIVE_STATUS.getName());
					if (obj != null) {
						//deviceStatus = ((OCTET)obj).getValue();  // 1byte
						deviceStatus = DataUtil.readByteString(obj.toString()); // 1byte HEX code
						deviceStatusBitString = DataUtil.byteArrayToBinaryString(deviceStatus);
						log.debug("[DRIVE_STATUS:BitString] => " + deviceStatusBitString);
	
						// Drive(Device) Alarm 의 상태값을 비교해서 저장하기 위해 METER_ERROR 컬럼을 사용한다. 
						EnergyMeter meter = (EnergyMeter) this.getMeter();
						if (meter.getMeterError() != null && meter.getMeterError().length() != 8) { // 1byte
							meter.setMeterError(null);
						}
	
						//DRIVE_STATUS 값이 없거나 변경됬을때만 이벤트로 등록한다.
						//meter.setMeterError("0010000000000100");
						if (meter.getMeterError() == null || !deviceStatusBitString.equals(meter.getMeterError())) {
							int prevState = 0;
							int curState = 0;
	
							for (int flag = 0; flag < deviceStatusBitString.length(); flag++) {
								if (meter.getMeterError() == null) {
									prevState = 0;
								} else {
									prevState = Integer.parseInt(meter.getMeterError().substring(flag, flag + 1));
								}
	
								curState = Integer.parseInt(deviceStatusBitString.substring(flag, flag + 1));
								if (prevState == 0 && curState == 1) {
									e = new EventLogData();
									e.setDate(setDate); // :date= 제거
									e.setTime(setTime);
									e.setFlag(EVENT_LOG.DeviceAlarm.getFlag());
									e.setKind("STE");
									e.setMsg(EVENT_LOG.DeviceAlarm.getMsg());
	
									switch (flag) {
									case 0: // EPROM error
										e.setAppend("[ON]" + DEVICE_STATUS_ALARM.EPROMError.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.EPROMError.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.EPROMError.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][ON][" + DEVICE_STATUS_ALARM.EPROMError.getMsg() + "] " + e.toString());
										break;
									case 1: // Clock error
										e.setAppend("[ON]" + DEVICE_STATUS_ALARM.ClockError.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.ClockError.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.ClockError.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][ON][" + DEVICE_STATUS_ALARM.ClockError.getMsg() + "] " + e.toString());
										break;
									case 2: // Battery error
										e.setAppend("[ON]" + DEVICE_STATUS_ALARM.BatteryError.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.BatteryError.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.BatteryError.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][ON][" + DEVICE_STATUS_ALARM.BatteryError.getMsg() + "] " + e.toString());
										break;
									case 3: // Read card errors
										e.setAppend("[ON]" + DEVICE_STATUS_ALARM.ReadCardError.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.ReadCardError.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.ReadCardError.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][ON][" + DEVICE_STATUS_ALARM.ReadCardError.getMsg() + "] " + e.toString());
										break;
									case 4: // Data abnormal
										e.setAppend("[ON]" + DEVICE_STATUS_ALARM.DataAbnormal.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.DataAbnormal.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.DataAbnormal.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][ON][" + DEVICE_STATUS_ALARM.DataAbnormal.getMsg() + "] " + e.toString());
										break;
									case 5: // External Battery Status
										e.setAppend("[ON]" + DEVICE_STATUS_ALARM.ExternalBatteryStatus.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.ExternalBatteryStatus.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.ExternalBatteryStatus.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][ON][" + DEVICE_STATUS_ALARM.ExternalBatteryStatus.getMsg() + "] " + e.toString());
										break;
									case 6: // High low level input
										e.setAppend("[ON]" + DEVICE_STATUS_ALARM.HighLowLevelInput.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.HighLowLevelInput.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.HighLowLevelInput.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][ON][" + DEVICE_STATUS_ALARM.HighLowLevelInput.getMsg() + "] " + e.toString());
										break;
									case 7: // Voltage detect input
										e.setAppend("[ON]" + DEVICE_STATUS_ALARM.VoltageDetectInput.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.VoltageDetectInput.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.VoltageDetectInput.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][ON][" + DEVICE_STATUS_ALARM.VoltageDetectInput.getMsg() + "] " + e.toString());
										break;
									default:
										break;
									}
									meterAlarmLog.add(e);
								} else if (prevState == 1 && curState == 0) {
									e = new EventLogData();
									e.setDate(setDate); // :date= 제거
									e.setTime(setTime);
									e.setFlag(EVENT_LOG.DeviceAlarm.getFlag());
									e.setKind("STE");
									e.setMsg(EVENT_LOG.DeviceAlarm.getMsg());
	
									switch (flag) {
									case 0: // EPROM error
										e.setAppend("[OFF]" + DEVICE_STATUS_ALARM.EPROMError.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.EPROMError.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.EPROMError.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][OFF][" + DEVICE_STATUS_ALARM.EPROMError.getMsg() + "] " + e.toString());
										break;
									case 1: // Clock error
										e.setAppend("[OFF]" + DEVICE_STATUS_ALARM.ClockError.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.ClockError.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.ClockError.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][OFF][" + DEVICE_STATUS_ALARM.ClockError.getMsg() + "] " + e.toString());
										break;
									case 2: // Battery error
										e.setAppend("[OFF]" + DEVICE_STATUS_ALARM.BatteryError.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.BatteryError.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.BatteryError.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][OFF][" + DEVICE_STATUS_ALARM.BatteryError.getMsg() + "] " + e.toString());
										break;
									case 3: // Read card errors
										e.setAppend("[OFF]" + DEVICE_STATUS_ALARM.ReadCardError.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.ReadCardError.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.ReadCardError.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][OFF][" + DEVICE_STATUS_ALARM.ReadCardError.getMsg() + "] " + e.toString());
										break;
									case 4: // Data abnormal
										e.setAppend("[OFF]" + DEVICE_STATUS_ALARM.DataAbnormal.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.DataAbnormal.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.DataAbnormal.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][OFF][" + DEVICE_STATUS_ALARM.DataAbnormal.getMsg() + "] " + e.toString());
										break;
									case 5: // External Battery Status
										e.setAppend("[OFF]" + DEVICE_STATUS_ALARM.ExternalBatteryStatus.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.ExternalBatteryStatus.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.ExternalBatteryStatus.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][OFF][" + DEVICE_STATUS_ALARM.ExternalBatteryStatus.getMsg() + "] " + e.toString());
										break;
									case 6: // High low level input
										e.setAppend("[OFF]" + DEVICE_STATUS_ALARM.HighLowLevelInput.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.HighLowLevelInput.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.HighLowLevelInput.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][OFF][" + DEVICE_STATUS_ALARM.HighLowLevelInput.getMsg() + "] " + e.toString());
										break;
									case 7: // Voltage detect input
										e.setAppend("[OFF]" + DEVICE_STATUS_ALARM.VoltageDetectInput.getMsg());
										e.setFlag(DEVICE_STATUS_ALARM.VoltageDetectInput.getFlag());
										e.setMsg(DEVICE_STATUS_ALARM.VoltageDetectInput.getMsg());
										log.debug("[DRIVE_STATUS - Device Alarm][OFF][" + DEVICE_STATUS_ALARM.VoltageDetectInput.getMsg() + "] " + e.toString());
										break;
									default:
										break;
									}
	
									meterAlarmLog.add(e);
								}
							}
	
							meter.setMeterError(deviceStatusBitString);
						}
					}
	
					result.remove(OBIS.DRIVE_STATUS.getCode());
				}
	
				// 원래 코드 이벤트 잘 저장되면 아래 삭제할것            
				//            map = (Map<String, Object>)result.get(OBIS.DRIVE_STATUS.getCode());
				//            if (map != null) {
				//	        	Object obj = map.get(OBIS.DRIVE_STATUS.getName());
				//	        	if (obj != null) deviceStatus = ((OCTET)obj).getValue();	        	
				//	        	devieStatusStr = DLMSTable.getOBIS_DRIVE_STATUS((OCTET)obj);
				//	        	log.debug("DRIVE_STATUS[" + Hex.decode(deviceStatus) + "], OBIS_DRIVE_STATUS[" + devieStatusStr + "]");
				//	        	data.put(OBIS.DRIVE_STATUS.getName(), devieStatusStr);
				//	        	result.remove(OBIS.DRIVE_STATUS.getCode());
				//            }
				//            for(DEVICE_STATUS_ALARM alarm : DEVICE_STATUS_ALARM.values()){
				//            	if(devieStatusStr.indexOf(alarm.getMsg()) >= 0){
				//    	        	log.debug("OBIS_DRIVE_STATUS[" + devieStatusStr + "] => " + EVENT_LOG.DeviceAlarm.name() + "[alarmTime=" + meterTime + ", value=" + alarm.getMsg()+"]");
				//    	            EventLogData e = new EventLogData();
				//    	            String hhmmss = meterTime.substring(8);
				//    	            e.setDate(meterTime.substring(0,8)); // :date= 제거
				//    	            e.setTime(hhmmss.substring(0, 6));
				//    	            e.setFlag(EVENT_LOG.DeviceAlarm.getFlag());
				//    	            e.setKind("STE");
				//    	            e.setMsg(EVENT_LOG.DeviceAlarm.getMsg());
				//    	            e.setAppend(alarm.getMsg());
				//    		        meterAlarmLog.add(e);
				//            	}            	
				//            }
	
				/*
				 * 0.0.96.10.7.255 MEASUREMENT_STATUS : Measurement Alarm Message 처리
				 * MeterEvent Log 로 저장 하지만 알람정보 자체가 시간값이 없기때문에 미터 시간 기준으로 저장한다.
				 * DR 미터만 이용하고 CT/CP미터는 새로 추가된 1.0.99.1.2.255 Measurement event log 를 이용하도록 한다.
				 */
				// 원래 코드 나중에 잘되면 삭제할것                
				//            map = (Map<String, Object>)result.get(OBIS.MEASUREMENT_STATUS.getCode());
				//            if (map != null) {
				//	        	Object obj = map.get(OBIS.MEASUREMENT_STATUS.getName());
				//	        	if (obj != null) measurementStatus = ((OCTET)obj).getValue();	        	
				//	        	measureStatusStr = DLMSTable.getOBIS_MEASUREMENT_STATUS((OCTET)obj);
				//	        	log.debug("MEASUREMENT_STATUS[" + Hex.decode(measurementStatus) + "], OBIS_MEASUREMENT_STATUS[" + measureStatusStr + "]");
				//	        	data.put(OBIS.MEASUREMENT_STATUS.getName(), measureStatusStr);                            
				//	        	result.remove(OBIS.MEASUREMENT_STATUS.getCode());
				//            }
				//            
				//            String meterName = meter.getModel().getName();
				//            if("LSIQ-3PCT".equals(meterName) || "LSIQ-3PCV".equals(meterName)) {
				//            	/*
				//            	 * Measurement event log(1.0.99.1.2.255) 이용하는 것으로 변경
				//            	 */
				//            }else{
				//                //0000000201860980
				//                for(MEASUREMENT_STATUS_ALARM alarm : MEASUREMENT_STATUS_ALARM.values()){
				//                	if(measureStatusStr.indexOf(alarm.getMsg()) >= 0){
				//        	        	log.debug("OBIS_MEASUREMENT_STATUS[" + measureStatusStr + "]=> " + EVENT_LOG.MeasurementAlarm.name() + "[alarmTime=" + meterTime + ", value=" + alarm.getMsg()+"]");
				//        	            EventLogData e = new EventLogData();
				//        	            String hhmmss = meterTime.substring(8);
				//        	            e.setDate(meterTime.substring(0,8)); // :date= 제거
				//        	            e.setTime(hhmmss.substring(0, 6));
				//        	            e.setFlag(EVENT_LOG.MeasurementAlarm.getFlag());
				//        	            e.setKind("STE");
				//        	            e.setMsg(EVENT_LOG.MeasurementAlarm.getMsg());
				//        	            e.setAppend(alarm.getMsg());
				//        		        meterAlarmLog.add(e);
				//                	}            	
				//                }            	
				//            }
	
				map = (Map<String, Object>) result.get(OBIS.MEASUREMENT_STATUS.getCode());
				log.debug("## MEASUREMENT_STATUS(Measurement Alarm Message) Eevnt count = " + (map == null ? 0 : map.size()));
				if (map != null) {
					log.debug("[MEASUREMENT_STATUS] => " + map.toString());
					Object obj = map.get(OBIS.MEASUREMENT_STATUS.getName());
					if (obj != null) {
						/*
						 * CT/CP 미터의 경우 Measurement event log(1.0.99.1.2.255) 이용하는 것으로 변경  
						 */
						String meterName = meter.getModel().getName();
						if (!"LSIQ-3PCT".equals(meterName) && !"LSIQ-3PCV".equals(meterName)) {
							//measurementStatus = ((OCTET)obj).getValue(); // 8byte
							measurementStatus = DataUtil.readByteString(obj.toString()); // 8byte HEX code
							measurementStatusBitString = DataUtil.byteArrayToBinaryString(measurementStatus);
							measurementStatusBitString = measurementStatusBitString.substring(21, measurementStatusBitString.length()); // length�� 43���� ����. �������� Reserved�̱⶧��
							log.debug("[MEASUREMENT_STATUS:BitString] => " + measurementStatusBitString);
	
							//  Measurement Alarm 의 상태값을 비교해서 저장하기 위해 FRIENDLY_NAME 컬럼을 사용한다. 
							EnergyMeter meter = (EnergyMeter) this.getMeter();
							if (meter.getFriendlyName() != null && meter.getFriendlyName().length() != 43) { // 43bit
								meter.setFriendlyName(null);
							}
	
							// MEASUREMENT_STATUS 값이 없거나 변경됬을때만 이벤트로 등록한다.
							if (meter.getFriendlyName() == null || !meter.getFriendlyName().equals(measurementStatusBitString)) {
								int prevState = 0;
								int curState = 0;
	
								for (int flag = 0; flag < measurementStatusBitString.length(); flag++) {
									if (meter.getFriendlyName() == null) {
										prevState = 0;
									} else {
										prevState = Integer.parseInt(meter.getFriendlyName().substring(flag, flag + 1));
									}
	
									curState = Integer.parseInt(measurementStatusBitString.substring(flag, flag + 1));
									if (prevState == 0 && curState == 1) {
										e = new EventLogData();
										e.setDate(setDate); // :date= 제거
										e.setTime(setTime);
										e.setFlag(EVENT_LOG.MeasurementAlarm.getFlag());
										e.setKind("STE");
										e.setMsg(EVENT_LOG.MeasurementAlarm.getMsg());
	
										switch (flag) {
										case 0: // L1 voltage loss
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1VoltageLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1VoltageLoss.getMsg() + "] " + e.toString());
											break;
										case 1: // L2 voltage loss
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2VoltageLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2VoltageLoss.getMsg() + "] " + e.toString());
											break;
										case 2: // L3 voltage loss
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3VoltageLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3VoltageLoss.getMsg() + "] " + e.toString());
											break;
										case 3: // L1 current loss
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1CurrentLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1CurrentLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1CurrentLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1CurrentLoss.getMsg() + "] " + e.toString());
											break;
										case 4: // L2 current loss
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2CurrentLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2CurrentLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2CurrentLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2CurrentLoss.getMsg() + "] " + e.toString());
											break;
										case 5: // L3 current loss
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3CurrentLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3CurrentLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3CurrentLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3CurrentLoss.getMsg() + "] " + e.toString());
											break;
										case 6: // L1 voltage cut
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1VoltageCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1VoltageCut.getMsg() + "] " + e.toString());
											break;
										case 7: // L2 voltage cut
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2VoltageCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2VoltageCut.getMsg() + "] " + e.toString());
											break;
										case 8: // L3 voltage cut
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3VoltageCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3VoltageCut.getMsg() + "] " + e.toString());
											break;
										case 9: // Voltage reverse phase sequence
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.VoltageReversePhaseSequence.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.VoltageReversePhaseSequence.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.VoltageReversePhaseSequence.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.VoltageReversePhaseSequence.getMsg() + "] " + e.toString());
											break;
										case 10: // Current reverse phase sequence
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.CurrentReversePhaseSequence.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.CurrentReversePhaseSequence.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.CurrentReversePhaseSequence.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.CurrentReversePhaseSequence.getMsg() + "] " + e.toString());
											break;
										case 11: // Voltage asymmetric
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.VoltageAsymmetric.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.VoltageAsymmetric.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.VoltageAsymmetric.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.VoltageAsymmetric.getMsg() + "] " + e.toString());
											break;
										case 12: // Current asymmetric
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.CurrentAsymmetric.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.CurrentAsymmetric.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.CurrentAsymmetric.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.CurrentAsymmetric.getMsg() + "] " + e.toString());
											break;
										case 13: // L1 over current
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1OverCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1OverCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1OverCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1OverCurrent.getMsg() + "] " + e.toString());
											break;
										case 14: // L2 over current
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2OverCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2OverCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2OverCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2OverCurrent.getMsg() + "] " + e.toString());
											break;
										case 15: // L3 over current
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3OverCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3OverCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3OverCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3OverCurrent.getMsg() + "] " + e.toString());
											break;
										case 16: // L1 current cut
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1CurrentCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1CurrentCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1CurrentCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1CurrentCut.getMsg() + "] " + e.toString());
											break;
										case 17: // L2 current cut
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2CurrentCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2CurrentCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2CurrentCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2CurrentCut.getMsg() + "] " + e.toString());
											break;
										case 18: // L3 current cut
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3CurrentCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3CurrentCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3CurrentCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3CurrentCut.getMsg() + "] " + e.toString());
											break;
										case 19: // L1 over voltage
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1OverVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1OverVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1OverVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1OverVoltage.getMsg() + "] " + e.toString());
											break;
										case 20: // L2 over voltage
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2OverVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2OverVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2OverVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2OverVoltage.getMsg() + "] " + e.toString());
											break;
										case 21: // L3 over voltage
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3OverVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3OverVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3OverVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3OverVoltage.getMsg() + "] " + e.toString());
											break;
										case 22: // L1 under voltage
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1UnderVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1UnderVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1UnderVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1UnderVoltage.getMsg() + "] " + e.toString());
											break;
										case 23: // L2 under voltage
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2UnderVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2UnderVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2UnderVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2UnderVoltage.getMsg() + "] " + e.toString());
											break;
										case 24: // L3 under voltage
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3UnderVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3UnderVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3UnderVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3UnderVoltage.getMsg() + "] " + e.toString());
											break;
										case 25: // All phases voltage loss
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.AllPhasesVoltageLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.AllPhasesVoltageLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.AllPhasesVoltageLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.AllPhasesVoltageLoss.getMsg() + "] " + e.toString());
											break;
										case 26: // L1 over load
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1OverLoad.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1OverLoad.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1OverLoad.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1OverLoad.getMsg() + "] " + e.toString());
											break;
										case 27: // L2 over load
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2OverLoad.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2OverLoad.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2OverLoad.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2OverLoad.getMsg() + "] " + e.toString());
											break;
										case 28: // L3 over load
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3OverLoad.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3OverLoad.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3OverLoad.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3OverLoad.getMsg() + "] " + e.toString());
											break;
										case 29: // Total power factor exceeded
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.TotalPowerFactorExceeded.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.TotalPowerFactorExceeded.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.TotalPowerFactorExceeded.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.TotalPowerFactorExceeded.getMsg() + "] " + e.toString());
											break;
										case 30: // L1 voltage super top limit
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1VoltageSuperTopLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageSuperTopLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageSuperTopLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1VoltageSuperTopLimit.getMsg() + "] " + e.toString());
											break;
										case 31: // L2 voltage super top limit
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2VoltageSuperTopLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageSuperTopLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageSuperTopLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2VoltageSuperTopLimit.getMsg() + "] " + e.toString());
											break;
										case 32: // L3 voltage super top limit
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3VoltageSuperTopLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageSuperTopLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageSuperTopLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3VoltageSuperTopLimit.getMsg() + "] " + e.toString());
											break;
										case 33: // L1 voltage qualification
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1VoltageQualification.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageQualification.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageQualification.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1VoltageQualification.getMsg() + "] " + e.toString());
											break;
										case 34: // L2 voltage qualification
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2VoltageQualification.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageQualification.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageQualification.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2VoltageQualification.getMsg() + "] " + e.toString());
											break;
										case 35: // L3 voltage qualification
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3VoltageQualification.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageQualification.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageQualification.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3VoltageQualification.getMsg() + "] " + e.toString());
											break;
										case 36: // L1 voltage super low limit
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1VoltageSuperLowLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageSuperLowLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageSuperLowLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1VoltageSuperLowLimit.getMsg() + "] " + e.toString());
											break;
										case 37: // L2 voltage super low limit
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2VoltageSuperLowLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageSuperLowLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageSuperLowLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2VoltageSuperLowLimit.getMsg() + "] " + e.toString());
											break;
										case 38: // L3 voltage super low limit
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3VoltageSuperLowLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageSuperLowLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageSuperLowLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3VoltageSuperLowLimit.getMsg() + "] " + e.toString());
											break;
										case 39: // Neutral current unbalance
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.NeutralCurrentUnbalance.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.NeutralCurrentUnbalance.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.NeutralCurrentUnbalance.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.NeutralCurrentUnbalance.getMsg() + "] " + e.toString());
											break;
										case 40: // L1 reverse current
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L1ReverseCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1ReverseCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1ReverseCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L1ReverseCurrent.getMsg() + "] " + e.toString());
											break;
										case 41: // L2 reverse current
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L2ReverseCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2ReverseCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2ReverseCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L2ReverseCurrent.getMsg() + "] " + e.toString());
											break;
										case 42: // L3 reverse current
											e.setAppend("[ON]" + MEASUREMENT_STATUS_ALARM.L3ReverseCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3ReverseCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3ReverseCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][ON][" + MEASUREMENT_STATUS_ALARM.L3ReverseCurrent.getMsg() + "] " + e.toString());
											break;
										default:
											break;
										}
										meterAlarmLog.add(e);
									} else if (prevState == 1 && curState == 0) {
										e = new EventLogData();
										e.setDate(setDate); // :date= 제거
										e.setTime(setTime);
										e.setFlag(EVENT_LOG.MeasurementAlarm.getFlag());
										e.setKind("STE");
										e.setMsg(EVENT_LOG.MeasurementAlarm.getMsg());
	
										switch (flag) {
										case 0: // L1 voltage loss
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1VoltageLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1VoltageLoss.getMsg() + "] " + e.toString());
											break;
										case 1: // L2 voltage loss
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2VoltageLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2VoltageLoss.getMsg() + "] " + e.toString());
											break;
										case 2: // L3 voltage loss
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3VoltageLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3VoltageLoss.getMsg() + "] " + e.toString());
											break;
										case 3: // L1 current loss
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1CurrentLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1CurrentLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1CurrentLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1CurrentLoss.getMsg() + "] " + e.toString());
											break;
										case 4: // L2 current loss
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2CurrentLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2CurrentLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2CurrentLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2CurrentLoss.getMsg() + "] " + e.toString());
											break;
										case 5: // L3 current loss
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3CurrentLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3CurrentLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3CurrentLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3CurrentLoss.getMsg() + "] " + e.toString());
											break;
										case 6: // L1 voltage cut
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1VoltageCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1VoltageCut.getMsg() + "] " + e.toString());
											break;
										case 7: // L2 voltage cut
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2VoltageCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2VoltageCut.getMsg() + "] " + e.toString());
											break;
										case 8: // L3 voltage cut
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3VoltageCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3VoltageCut.getMsg() + "] " + e.toString());
											break;
										case 9: // Voltage reverse phase sequence
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.VoltageReversePhaseSequence.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.VoltageReversePhaseSequence.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.VoltageReversePhaseSequence.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.VoltageReversePhaseSequence.getMsg() + "] " + e.toString());
											break;
										case 10: // Current reverse phase sequence
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.CurrentReversePhaseSequence.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.CurrentReversePhaseSequence.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.CurrentReversePhaseSequence.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.CurrentReversePhaseSequence.getMsg() + "] " + e.toString());
											break;
										case 11: // Voltage asymmetric
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.VoltageAsymmetric.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.VoltageAsymmetric.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.VoltageAsymmetric.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.VoltageAsymmetric.getMsg() + "] " + e.toString());
											break;
										case 12: // Current asymmetric
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.CurrentAsymmetric.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.CurrentAsymmetric.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.CurrentAsymmetric.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.CurrentAsymmetric.getMsg() + "] " + e.toString());
											break;
										case 13: // L1 over current
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1OverCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1OverCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1OverCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1OverCurrent.getMsg() + "] " + e.toString());
											break;
										case 14: // L2 over current
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2OverCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2OverCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2OverCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2OverCurrent.getMsg() + "] " + e.toString());
											break;
										case 15: // L3 over current
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3OverCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3OverCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3OverCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3OverCurrent.getMsg() + "] " + e.toString());
											break;
										case 16: // L1 current cut
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1CurrentCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1CurrentCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1CurrentCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1CurrentCut.getMsg() + "] " + e.toString());
											break;
										case 17: // L2 current cut
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2CurrentCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2CurrentCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2CurrentCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2CurrentCut.getMsg() + "] " + e.toString());
											break;
										case 18: // L3 current cut
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3CurrentCut.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3CurrentCut.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3CurrentCut.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3CurrentCut.getMsg() + "] " + e.toString());
											break;
										case 19: // L1 over voltage
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1OverVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1OverVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1OverVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1OverVoltage.getMsg() + "] " + e.toString());
											break;
										case 20: // L2 over voltage
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2OverVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2OverVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2OverVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2OverVoltage.getMsg() + "] " + e.toString());
											break;
										case 21: // L3 over voltage
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3OverVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3OverVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3OverVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3OverVoltage.getMsg() + "] " + e.toString());
											break;
										case 22: // L1 under voltage
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1UnderVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1UnderVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1UnderVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1UnderVoltage.getMsg() + "] " + e.toString());
											break;
										case 23: // L2 under voltage
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2UnderVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2UnderVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2UnderVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2UnderVoltage.getMsg() + "] " + e.toString());
											break;
										case 24: // L3 under voltage
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3UnderVoltage.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3UnderVoltage.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3UnderVoltage.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3UnderVoltage.getMsg() + "] " + e.toString());
											break;
										case 25: // All phases voltage loss
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.AllPhasesVoltageLoss.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.AllPhasesVoltageLoss.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.AllPhasesVoltageLoss.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.AllPhasesVoltageLoss.getMsg() + "] " + e.toString());
											break;
										case 26: // L1 over load
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1OverLoad.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1OverLoad.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1OverLoad.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1OverLoad.getMsg() + "] " + e.toString());
											break;
										case 27: // L2 over load
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2OverLoad.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2OverLoad.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2OverLoad.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2OverLoad.getMsg() + "] " + e.toString());
											break;
										case 28: // L3 over load
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3OverLoad.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3OverLoad.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3OverLoad.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3OverLoad.getMsg() + "] " + e.toString());
											break;
										case 29: // Total power factor exceeded
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.TotalPowerFactorExceeded.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.TotalPowerFactorExceeded.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.TotalPowerFactorExceeded.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.TotalPowerFactorExceeded.getMsg() + "] " + e.toString());
											break;
										case 30: // L1 voltage super top limit
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1VoltageSuperTopLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageSuperTopLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageSuperTopLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1VoltageSuperTopLimit.getMsg() + "] " + e.toString());
											break;
										case 31: // L2 voltage super top limit
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2VoltageSuperTopLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageSuperTopLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageSuperTopLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2VoltageSuperTopLimit.getMsg() + "] " + e.toString());
											break;
										case 32: // L3 voltage super top limit
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3VoltageSuperTopLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageSuperTopLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageSuperTopLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3VoltageSuperTopLimit.getMsg() + "] " + e.toString());
											break;
										case 33: // L1 voltage qualification
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1VoltageQualification.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageQualification.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageQualification.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1VoltageQualification.getMsg() + "] " + e.toString());
											break;
										case 34: // L2 voltage qualification
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2VoltageQualification.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageQualification.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageQualification.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2VoltageQualification.getMsg() + "] " + e.toString());
											break;
										case 35: // L3 voltage qualification
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3VoltageQualification.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageQualification.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageQualification.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3VoltageQualification.getMsg() + "] " + e.toString());
											break;
										case 36: // L1 voltage super low limit
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1VoltageSuperLowLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1VoltageSuperLowLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1VoltageSuperLowLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1VoltageSuperLowLimit.getMsg() + "] " + e.toString());
											break;
										case 37: // L2 voltage super low limit
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2VoltageSuperLowLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2VoltageSuperLowLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2VoltageSuperLowLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2VoltageSuperLowLimit.getMsg() + "] " + e.toString());
											break;
										case 38: // L3 voltage super low limit
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3VoltageSuperLowLimit.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3VoltageSuperLowLimit.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3VoltageSuperLowLimit.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3VoltageSuperLowLimit.getMsg() + "] " + e.toString());
											break;
										case 39: // Neutral current unbalance
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.NeutralCurrentUnbalance.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.NeutralCurrentUnbalance.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.NeutralCurrentUnbalance.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.NeutralCurrentUnbalance.getMsg() + "] " + e.toString());
											break;
										case 40: // L1 reverse current
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L1ReverseCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L1ReverseCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L1ReverseCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L1ReverseCurrent.getMsg() + "] " + e.toString());
											break;
										case 41: // L2 reverse current
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L2ReverseCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L2ReverseCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L2ReverseCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L2ReverseCurrent.getMsg() + "] " + e.toString());
											break;
										case 42: // L3 reverse current
											e.setAppend("[OFF]" + MEASUREMENT_STATUS_ALARM.L3ReverseCurrent.getMsg());
											e.setFlag(MEASUREMENT_STATUS_ALARM.L3ReverseCurrent.getFlag());
											e.setMsg(MEASUREMENT_STATUS_ALARM.L3ReverseCurrent.getMsg());
											log.debug("[MEASUREMENT_STATUS - Measurement Alarm][OFF][" + MEASUREMENT_STATUS_ALARM.L3ReverseCurrent.getMsg() + "] " + e.toString());
											break;
										default:
											break;
										}
	
										meterAlarmLog.add(e);
									}
								}
	
								meter.setFriendlyName(measurementStatusBitString);
							}
						} else {
							/*
							 * CT/CP 미터의 경우 Measurement event log(1.0.99.1.2.255) 이용하는 것으로 변경  
							 */
						}
					}
					result.remove(OBIS.MEASUREMENT_STATUS.getCode());
				}
			} // end if
			log.debug("##### ALARM LOG STOP #####");
			
		} catch (Exception e) {
			log.error(e, e);
		}

	}

	private void setDailyBillingData() {
		log.debug("##### DailyBillingData set START #####");

		dailyBill = new ArrayList<BillingData>();
		Double val = 0.0;
		int cnt = 0;
		Double[] ch = new Double[8];
		Map<String, Object> dailyMap = null;
		Object value = null;
		BillingData day = null;

		for (int i = 0; i < result.size(); i++) {
			if (!result.containsKey(OBIS.DAILY_LOAD_PROFILE.getCode() + "-" + i))
				break;

			dailyMap = (Map<String, Object>) result.get(OBIS.DAILY_LOAD_PROFILE.getCode() + "-" + i);

			if (dailyMap != null && dailyMap.size() > 0) {
				cnt = 0;
				while ((value = dailyMap.get("Channel[1]" + "-" + cnt)) != null) {
					if (value instanceof OCTET)
						val = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
					else if (value instanceof Long)
						val = ((Long) value).doubleValue();
					else if (value instanceof Float)
						val = ((Float) value).doubleValue();
					
					val = getCalcCTVT(val); // ctvt
					
					val *= 0.001;
					val = val / activePulseConstant;
					ch[0] = val;
					if (val > meteringValue) {
						meteringValue = val;
					}

					//String dayTime = (String) dailyMap.get("DateTime" + "-" + cnt++);
					String dayTime = (String) dailyMap.get("DateTime[0]" + "-" + cnt);

					log.debug("0. [Clock] DateTime = " + dayTime + ", Metering Value = " + meteringValue);
					log.debug("1. [Cumulative active energy -import] RAWDATA = " + ch[0].toString());

					value = dailyMap.get("Channel[2]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[1] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[1] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[1] = ((Float) value).doubleValue();
						
						ch[1] = getCalcCTVT(ch[1]); // ctvt
						ch[1] /= activePulseConstant;
						ch[1] *= 0.001;
						log.debug("2. [Cumulative active energy -import rate 1] RAWDATA = " + ch[1].toString());
					}
					value = dailyMap.get("Channel[3]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[2] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[2] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[2] = ((Float) value).doubleValue();
						
						ch[2] = getCalcCTVT(ch[2]); // ctvt
						ch[2] /= activePulseConstant;
						ch[2] *= 0.001;
						log.debug("3. [Cumulative active energy -import rate 2] RAWDATA = " + ch[2].toString());
					}
					value = dailyMap.get("Channel[4]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[3] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[3] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[3] = ((Float) value).doubleValue();
						
						ch[3] = getCalcCTVT(ch[3]); // ctvt
						ch[3] /= activePulseConstant;
						ch[3] *= 0.001;
						log.debug("4. [Cumulative active energy -import rate 3] RAWDATA = " + ch[3].toString());
					}
					value = dailyMap.get("Channel[5]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[4] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[4] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[4] = ((Float) value).doubleValue();
						
						ch[4] = getCalcCTVT(ch[4]); // ctvt
						ch[4] /= reActivePulseConstant;
						ch[4] *= 0.001;
						log.debug("5. [Cumulative reactive energy -import] RAWDATA = " + ch[4].toString());
					}
					value = dailyMap.get("Channel[6]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[5] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[5] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[5] = ((Float) value).doubleValue();
						
						ch[5] = getCalcCTVT(ch[5]); // ctvt
						ch[5] /= reActivePulseConstant;
						ch[5] *= 0.001;
						log.debug("6. [Cumulative reactive energy -import rate 1] RAWDATA = " + ch[5].toString());
					}
					value = dailyMap.get("Channel[7]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[6] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[6] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[6] = ((Float) value).doubleValue();
						
						ch[6] = getCalcCTVT(ch[6]); // ctvt
						ch[6] /= reActivePulseConstant;
						ch[6] *= 0.001;
						log.debug("7. [Cumulative reactive energy -import rate 2] RAWDATA = " + ch[6].toString());
					}
					value = dailyMap.get("Channel[8]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[7] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[7] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[7] = ((Float) value).doubleValue();
						
						ch[7] = getCalcCTVT(ch[7]); // ctvt
						ch[7] /= reActivePulseConstant;
						ch[7] *= 0.001;
						log.debug("8. [Cumulative reactive energy -import rate 3] RAWDATA = " + ch[7].toString());
					}
					/********************************************************/

					day = new BillingData();
					day.setBillingTimestamp(dayTime);
					day.setActiveEnergyImportRateTotal(ch[0]); // Cumulative active energy -import
					day.setActiveEnergyImportRate1(ch[1]); // Cumulative active energy -import rate 1
					day.setActiveEnergyImportRate2(ch[2]); // Cumulative active energy -import rate 2
					day.setActiveEnergyImportRate3(ch[3]); // Cumulative active energy -import rate 3

					day.setReactiveEnergyRateTotal(ch[4]); // Cumulative reactive energy -import 
					day.setReactiveEnergyRate1(ch[5]); // Cumulative reactive energy -import rate 1
					day.setReactiveEnergyRate2(ch[6]); // Cumulative reactive energy -import rate 2
					day.setReactiveEnergyRate3(ch[7]); // Cumulative reactive energy -import rate 3

					dailyBill.add(day);
					
					cnt++;
				}
			}
		}
		log.debug("##### DailyBillingData set STOP #####");
	}

	private void setMonthlyBillingData() {
		log.debug("##### MonthlyBillingData set START #####");

		/* 
		 * 
			Clock
			Cumulative active energy -import
			Cumulative active energy -import rate 1
			Cumulative active energy -import rate 2
			Cumulative active energy -import rate 3 
			Cumulative reactive energy -import
			Cumulative reactive energy -import rate 1
			Cumulative reactive energy -import rate 2
			Cumulative reactive energy -import rate 3 
			Total maximum demand +A
			Total maximum demand +A     (datetime)
			Total maximum demand +A T1
			Total maximum demand +A T1  (datetime)
			Total maximum demand +A T2
			Total maximum demand +A T2  (datetime)
			Total maximum demand +A T3
			Total maximum demand +A T3  (datetime)
		 */

		monthlyBill = new ArrayList<BillingData>();
		Double val = 0.0;
		int cnt = 0;
		Double[] ch = new Double[12];
		Map<String, Object> dailyMap = null;
		Object value = null;
		BillingData month = null;

		for (int i = 0; i < result.size(); i++) {
			if (!result.containsKey(OBIS.MONTHLY_BILLING.getCode() + "-" + i))
				break;

			dailyMap = (Map<String, Object>) result.get(OBIS.MONTHLY_BILLING.getCode() + "-" + i);

			if (dailyMap != null && dailyMap.size() > 0) {
				cnt = 0;
				while ((value = dailyMap.get("Channel[1]" + "-" + cnt)) != null) {
					if (value instanceof OCTET)
						val = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
					else if (value instanceof Long)
						val = ((Long) value).doubleValue();
					else if (value instanceof Float)
						val = ((Float) value).doubleValue();
					
					val = getCalcCTVT(val); // ctvt
					val *= 0.001;
					val = val / activePulseConstant;
					ch[0] = val; // Cumulative active energy -import 

					//String dayTime = (String) dailyMap.get("DateTime" + "-" + cnt++);
					String clockTime = (String) dailyMap.get("DateTime[0]" + "-" + cnt);

					log.debug("0. [Clock] DateTime = " + clockTime);
					log.debug("1. [Cumulative active energy -import] RAWDATA = " + ch[0].toString());

					value = dailyMap.get("Channel[2]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[1] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[1] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[1] = ((Float) value).doubleValue();
						
						ch[1] = getCalcCTVT(ch[1]); // ctvt
						ch[1] /= activePulseConstant;
						ch[1] *= 0.001; // Cumulative active energy -import rate 1
						log.debug("2. [Cumulative active energy -import rate 1] RAWDATA = " + ch[1].toString());
					}
					value = dailyMap.get("Channel[3]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[2] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[2] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[2] = ((Float) value).doubleValue();
						
						ch[2] = getCalcCTVT(ch[2]); // ctvt
						ch[2] /= activePulseConstant;
						ch[2] *= 0.001; // Cumulative active energy -import rate 2
						log.debug("3. [Cumulative active energy -import rate 2] RAWDATA = " + ch[2].toString());
					}
					value = dailyMap.get("Channel[4]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[3] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[3] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[3] = ((Float) value).doubleValue();
						
						ch[3] = getCalcCTVT(ch[3]); // ctvt
						ch[3] /= activePulseConstant;
						ch[3] *= 0.001; // Cumulative active energy -import rate 3
						log.debug("4. [Cumulative active energy -import rate 3] RAWDATA = " + ch[3].toString());
					}
					value = dailyMap.get("Channel[5]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[4] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[4] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[4] = ((Float) value).doubleValue();
						
						ch[4] = getCalcCTVT(ch[4]); // ctvt
						ch[4] /= reActivePulseConstant;
						ch[4] *= 0.001; // Cumulative reactive energy -import
						log.debug("5. [Cumulative reactive energy -import] RAWDATA = " + ch[4].toString());
					}
					value = dailyMap.get("Channel[6]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[5] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[5] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[5] = ((Float) value).doubleValue();
						
						ch[5] = getCalcCTVT(ch[5]); // ctvt
						ch[5] /= reActivePulseConstant;
						ch[5] *= 0.001; // Cumulative reactive energy -import rate 1
						log.debug("6. [Cumulative reactive energy -import rate 1] RAWDATA = " + ch[5].toString());
					}
					value = dailyMap.get("Channel[7]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[6] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[6] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[6] = ((Float) value).doubleValue();
						
						ch[6] = getCalcCTVT(ch[6]); // ctvt
						ch[6] /= reActivePulseConstant;
						ch[6] *= 0.001; // Cumulative reactive energy -import rate 2
						log.debug("7. [Cumulative reactive energy -import rate 2] RAWDATA = " + ch[6].toString());
					}
					value = dailyMap.get("Channel[8]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[7] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[7] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[7] = ((Float) value).doubleValue();
						
						ch[7] = getCalcCTVT(ch[7]); // ctvt
						ch[7] /= reActivePulseConstant;
						ch[7] *= 0.001; // Cumulative reactive energy -import rate 3
						log.debug("8. [Cumulative reactive energy -import rate 2] RAWDATA = " + ch[7].toString());
					}

					value = dailyMap.get("Channel[9]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[8] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[8] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[8] = ((Float) value).doubleValue();
						
						ch[8] = getCalcCTVT(ch[8]); // ctvt
						ch[8] /= reActivePulseConstant;
						ch[8] *= 0.001; // Total maximum demand +A
						log.debug("9. [Total maximum demand +A] RAWDATA = " + ch[8].toString());
					}
					
					
					String totalMaximumDemandATime = String.valueOf(dailyMap.get("DateTime[9]" + "-" + cnt));
					if (totalMaximumDemandATime != null && !totalMaximumDemandATime.equals("")) {
						log.debug("10. [Total maximum demand +A Time] RAWDATA = " + totalMaximumDemandATime);
					}
					
					
//					String totalMaximumDemandATime = String.valueOf(dailyMap.get("Channel[10]" + "-" + cnt));
//					if (totalMaximumDemandATime != null && !totalMaximumDemandATime.equals("")) {
//						log.debug("10. [Total maximum demand +A Time] RAWDATA = " + totalMaximumDemandATime);
//					}
					
					//value = dailyMap.get("Channel[11]" + "-" + cnt);
					value = dailyMap.get("Channel[10]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[9] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[9] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[9] = ((Float) value).doubleValue();
						
						ch[9] = getCalcCTVT(ch[9]); // ctvt
						ch[9] /= reActivePulseConstant;
						ch[9] *= 0.001; // Total maximum demand +A T1
						log.debug("11. [Total maximum demand +A T1] RAWDATA = " + ch[9].toString());
					}
					
					//String totalMaximumDemandAT1Time = String.valueOf(dailyMap.get("Channel[12]" + "-" + cnt));
					String totalMaximumDemandAT1Time = String.valueOf(dailyMap.get("DateTime[10]" + "-" + cnt));
					if (totalMaximumDemandAT1Time != null && !totalMaximumDemandAT1Time.equals("")) {
						log.debug("12. [Total maximum demand +A T1 Time] RAWDATA = " + totalMaximumDemandAT1Time);
					}
					
					//value = dailyMap.get("Channel[13]" + "-" + cnt);
					value = dailyMap.get("Channel[11]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[10] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[10] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[10] = ((Float) value).doubleValue();
						
						ch[10] = getCalcCTVT(ch[10]); // ctvt
						ch[10] /= activePulseConstant;
						ch[10] *= 0.001; // Total maximum demand +A T2
						log.debug("13. [Total maximum demand +A T2] RAWDATA = " + ch[10].toString());
					}
					
					//String totalMaximumDemandAT2Time = String.valueOf(dailyMap.get("Channel[14]" + "-" + cnt));
					String totalMaximumDemandAT2Time = String.valueOf(dailyMap.get("DateTime[11]" + "-" + cnt));
					if (totalMaximumDemandAT2Time != null && !totalMaximumDemandAT2Time.equals("")) {
						log.debug("14. [Total maximum demand +A T2 Time] RAWDATA = " + totalMaximumDemandAT2Time);
					}
					
					//value = dailyMap.get("Channel[15]" + "-" + cnt);
					value = dailyMap.get("Channel[12]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[11] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[11] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[11] = ((Float) value).doubleValue();
						
						ch[11] = getCalcCTVT(ch[11]); // ctvt
						ch[11] /= activePulseConstant;
						ch[11] *= 0.001; // Total maximum demand +A T3
						log.debug("15. [Total maximum demand +A T3] RAWDATA = " + ch[11].toString());
					}
					
					//String totalMaximumDemandAT3Time = String.valueOf(dailyMap.get("Channel[16]" + "-" + cnt));
					String totalMaximumDemandAT3Time = String.valueOf(dailyMap.get("DateTime[12]" + "-" + cnt));
					if (totalMaximumDemandAT3Time != null && !totalMaximumDemandAT3Time.equals("")) {
						log.debug("16. [Total maximum demand +A T3 Time] RAWDATA = " + totalMaximumDemandAT3Time);
					}
					/********************************************************/

					month = new BillingData();
					month.setBillingTimestamp(clockTime);       // Clock
					month.setActiveEnergyRateTotal(ch[0]);    // Cumulative active energy -import
					month.setActiveEnergyRate1(ch[1]);        // Cumulative active energy -import rate 1
					month.setActiveEnergyRate2(ch[2]);        // Cumulative active energy -import rate 2
					month.setActiveEnergyRate3(ch[3]);		  // Cumulative active energy -import rate 3

					month.setReactiveEnergyRateTotal(ch[4]);  // Cumulative reactive energy -import
					month.setReactiveEnergyRate1(ch[5]);      // Cumulative reactive energy -import rate 1
					month.setReactiveEnergyRate2(ch[6]);      // Cumulative reactive energy -import rate 1
					month.setReactiveEnergyRate3(ch[7]);      // Cumulative reactive energy -import rate 1

					month.setActivePowerMaxDemandRateTotal(ch[8]);     // Total maximum demand +A
					month.setActivePowerDemandMaxTimeRateTotal(totalMaximumDemandATime);     // Total maximum demand +A
					month.setMaxDmdkVah1Rate1(ch[9]);		  // Total maximum demand +A T1
					month.setActivePowerDemandMaxTimeRate1(totalMaximumDemandAT1Time);       // Total maximum demand +A T1
					month.setMaxDmdkVah1Rate2(ch[10]);		  // Total maximum demand +A T2
					month.setActivePowerDemandMaxTimeRate2(totalMaximumDemandAT2Time);       // Total maximum demand +A T2
					month.setMaxDmdkVah1Rate3(ch[11]);		  // Total maximum demand +A T3
					month.setActivePowerDemandMaxTimeRate3(totalMaximumDemandAT3Time);       // Total maximum demand +A T3

					monthlyBill.add(month);
					
					cnt++;
				}
			}

		}
		log.debug("##### MonthlyBillingData set STOP #####");
	}
	
	
	
	private void setOverflowEventlogData() {
		log.debug("##### setOverflowEventlogData set START #####");

		/*
			Clock
			Cumulative active energy -export
			Cumulative reactive energy -export
			Cumulative active energy -import
			Cumulative active energy -import rate 1
			Cumulative active energy -import rate 2
			Cumulative active energy -import rate 3
			Cumulative active energy -import rate 4
			Cumulative reactive energy -import
			Cumulative reactive energy -import rate 1
			Cumulative reactive energy -import rate 2
			Cumulative reactive energy -import rate 3
			Cumulative reactive energy -import rate 4 
		*/
		currBillLog = new ArrayList<BillingData>();
		rolloverAlarmLog = new ArrayList<EventLogData>();
		String prevDatetime = "";
		
		Double val = 0.0;
		int cnt = 0;
		Double[] ch = new Double[12];
		Map<String, Object> eventlogMap = null;
		Object value = null;
		for (int i = 0; i < result.size(); i++) {
			if (!result.containsKey(OBIS.ENERGY_OVERFLOW_EVENTLOG.getCode() + "-" + i))
				break;

			eventlogMap = (Map<String, Object>) result.get(OBIS.ENERGY_OVERFLOW_EVENTLOG.getCode() + "-" + i);

			if (eventlogMap != null && eventlogMap.size() > 0) {
				cnt = 0;
				while ((value = eventlogMap.get("Channel[1]" + "-" + cnt)) != null) {
					if (value instanceof OCTET)
						val = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
					else if (value instanceof Long)
						val = ((Long) value).doubleValue();
					else if (value instanceof Float)
						val = ((Float) value).doubleValue();
					
					val = getCalcCTVT(val); // ctvt
					val *= 0.001;
					val = val / activePulseConstant;
					ch[0] = val; // Cumulative active energy -import 

					String dayTime = (String) eventlogMap.get("DateTime" + "-" + cnt);

					log.debug("0. [Clock] DateTime = " + dayTime);
					log.debug("1. [Cumulative active energy -export] RAWDATA = " + ch[0].toString());

					value = eventlogMap.get("Channel[2]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[1] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[1] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[1] = ((Float) value).doubleValue();
						
						ch[1] = getCalcCTVT(ch[1]); // ctvt
						ch[1] /= activePulseConstant;
						ch[1] *= 0.001; // Cumulative reactive energy -export
						log.debug("2. [Cumulative reactive energy -export] RAWDATA = " + ch[1].toString());
					}
					value = eventlogMap.get("Channel[3]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[2] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[2] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[2] = ((Float) value).doubleValue();
						
						ch[2] = getCalcCTVT(ch[2]); // ctvt
						ch[2] /= activePulseConstant;
						ch[2] *= 0.001; // Cumulative active energy -import
						log.debug("3. [Cumulative active energy -import] RAWDATA = " + ch[2].toString());
					}
					value = eventlogMap.get("Channel[4]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[3] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[3] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[3] = ((Float) value).doubleValue();
						
						ch[3] = getCalcCTVT(ch[3]); // ctvt
						ch[3] /= activePulseConstant;
						ch[3] *= 0.001; // Cumulative active energy -import rate 1
						log.debug("4. [Cumulative active energy -import rate 1] RAWDATA = " + ch[3].toString());
					}
					value = eventlogMap.get("Channel[5]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[4] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[4] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[4] = ((Float) value).doubleValue();
						
						ch[4] = getCalcCTVT(ch[4]); // ctvt
						ch[4] /= reActivePulseConstant;
						ch[4] *= 0.001; // Cumulative active energy -import rate 2
						log.debug("5. [Cumulative active energy -import rate 2] RAWDATA = " + ch[4].toString());
					}
					value = eventlogMap.get("Channel[6]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[5] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[5] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[5] = ((Float) value).doubleValue();
						
						ch[5] = getCalcCTVT(ch[5]);  // ctvt
						ch[5] /= reActivePulseConstant;
						ch[5] *= 0.001; // Cumulative active energy -import rate 3
						log.debug("6. [Cumulative active energy -import rate 3] RAWDATA = " + ch[5].toString());
					}
					value = eventlogMap.get("Channel[7]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[6] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[6] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[6] = ((Float) value).doubleValue();
						
						ch[6] = getCalcCTVT(ch[6]); // ctvt
						ch[6] /= reActivePulseConstant;
						ch[6] *= 0.001; // Cumulative active energy -import rate 4
						log.debug("7. [Cumulative active energy -import rate 4] RAWDATA = " + ch[6].toString());
					}
					value = eventlogMap.get("Channel[8]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[7] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[7] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[7] = ((Float) value).doubleValue();
						
						ch[7] = getCalcCTVT(ch[7]); // ctvt
						ch[7] /= reActivePulseConstant;
						ch[7] *= 0.001; // Cumulative reactive energy -import
						log.debug("8. [Cumulative reactive energy -import] RAWDATA = " + ch[7].toString());
					}

					value = eventlogMap.get("Channel[9]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[8] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[8] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[8] = ((Float) value).doubleValue();
						
						ch[8] = getCalcCTVT(ch[8]); // ctvt
						ch[8] /= reActivePulseConstant;
						ch[8] *= 0.001; // Cumulative reactive energy -import rate 1
						log.debug("9. [Cumulative reactive energy -import rate 1] RAWDATA = " + ch[8].toString());
					}
					
					value = eventlogMap.get("Channel[10]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[9] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[9] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[9] = ((Float) value).doubleValue();
						
						ch[9] = getCalcCTVT(ch[9]); // ctvt
						ch[9] /= reActivePulseConstant;
						ch[9] *= 0.001; // Cumulative reactive energy -import rate 2
						log.debug("10. [Cumulative reactive energy -import rate 2] RAWDATA = " + ch[9].toString());
					}
					value = eventlogMap.get("Channel[11]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[10] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[10] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[10] = ((Float) value).doubleValue();
						
						ch[10] = getCalcCTVT(ch[10]); // ctvt
						ch[10] /= activePulseConstant;
						ch[10] *= 0.001; // Cumulative reactive energy -import rate 3
						log.debug("11. [Cumulative reactive energy -import rate 3] RAWDATA = " + ch[10].toString());
					}
					
					value = eventlogMap.get("Channel[12]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							ch[11] = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							ch[11] = ((Long) value).doubleValue();
						else if (value instanceof Float)
							ch[11] = ((Float) value).doubleValue();
						
						ch[11] = getCalcCTVT(ch[11]); // ctvt
						ch[11] /= activePulseConstant;
						ch[11] *= 0.001; // Cumulative reactive energy -import rate 4
						log.debug("12. [Cumulative reactive energy -import rate 4] RAWDATA = " + ch[11].toString());
					}
					
					/********************************************************/
					log.debug("prevDatetime : " + prevDatetime + " dayTime : " + dayTime);
					if(dayTime != null) {
						if(!prevDatetime.equals(dayTime)) {
							String strDaytime = dayTime + "00";
							BillingData bill = new BillingData();
							bill.setBillingTimestamp(strDaytime);       // Clock
							
							bill.setActiveEnergyExportRateTotal(ch[0]);
							bill.setReactiveEnergyLagExportRateTotal(ch[1]);
							
							bill.setActiveEnergyRateTotal(ch[2]);    // Cumulative active energy -import
							bill.setActiveEnergyRate1(ch[3]);        // Cumulative active energy -import rate 1
							bill.setActiveEnergyRate2(ch[4]);        // Cumulative active energy -import rate 2
							bill.setActiveEnergyRate3(ch[5]);		  // Cumulative active energy -import rate 3
							bill.setActiveEnergyRate4(ch[6]);		  // Cumulative active energy -import rate 4
		
							bill.setReactiveEnergyRateTotal(ch[7]);  // Cumulative reactive energy -import
							bill.setReactiveEnergyRate1(ch[8]);      // Cumulative reactive energy -import rate 1
							bill.setReactiveEnergyRate2(ch[9]);      // Cumulative reactive energy -import rate 2
							bill.setReactiveEnergyRate3(ch[10]);      // Cumulative reactive energy -import rate 3
							bill.setReactiveEnergyRate4(ch[11]);      // Cumulative reactive energy -import rate 4
							bill.setOverflowYN(true); // overflow event flag
							
							currBillLog.add(bill);
							
							prevDatetime = dayTime;		
							
							StringBuffer sb = new StringBuffer();
							sb.append("Event Date ["+strDaytime+"]");
							sb.append(" ActiveEnergyExportRateTotal ["+ch[0]+"]");
							sb.append(" ReactiveEnergyLagExportRateTotal ["+ch[1]+"]");
							sb.append(" ActiveEnergyRateTotal ["+ch[2]+"]");
							sb.append(" ActiveEnergyRate1 ["+ch[3]+"]");
							sb.append(" ActiveEnergyRate2 ["+ch[4]+"]");
							sb.append(" ActiveEnergyRate3 ["+ch[5]+"]");
							sb.append(" ActiveEnergyRate4 ["+ch[6]+"]");
							
							sb.append(" ReactiveEnergyRateTotal ["+ch[7]+"]");
							sb.append(" ReactiveEnergyRate1 ["+ch[8]+"]");
							sb.append(" ReactiveEnergyRate2 ["+ch[9]+"]");
							sb.append(" ReactiveEnergyRate3 ["+ch[10]+"]");
							sb.append(" ReactiveEnergyRate4 ["+ch[11]+"]");							
							
							EventLogData e = new EventLogData();
							e.setDate(strDaytime.substring(0, 8)); // :date= 제거
							e.setTime(strDaytime.substring(8));
							e.setFlag(EVENT_LOG.RolloverEventAlarm.getFlag());
							e.setKind("STE");
							e.setMsg(EVENT_LOG.RolloverEventAlarm.getMsg());
							e.setAppend("[RolloverEvent]" + sb.toString());
							rolloverAlarmLog.add(e);
						}
					}
					
					log.debug("SIZE : " + currBillLog.size());
					cnt++;
				} // while
			} // if
		} //for
		log.debug("##### setOverflowEventlogData set STOP #####");
	}	
	
	public List<EventLogData> getRolloverEventAlarm() {
		return rolloverAlarmLog;
	}

	@Override
	public void setFlag(int flag) {

	}

	@Override
	public String toString() {

		return null;
	}

	public LPData[] getLPData() {

		return lpData;
	}

	public double getOBISScalar(int channel) {
		double scalar = 1;

		if (this.meter != null) {
			String meterName = meter.getModel().getName();
			String code = "";

			switch (channel) {
			case 1:
				code = DLMSSCALAR.OBIS.CUMULATIVE_ACTIVEENERGY_IMPORT.getCode();
				break;
			case 2:
				code = DLMSSCALAR.OBIS.CUMULATIVE_ACTIVEENERGY_IMPORT1.getCode();
				break;
			case 3:
				code = DLMSSCALAR.OBIS.CUMULATIVE_ACTIVEENERGY_IMPORT2.getCode();
				break;
			case 4:
				code = DLMSSCALAR.OBIS.CUMULATIVE_ACTIVEENERGY_IMPORT3.getCode();
				break;
			case 5:
				code = DLMSSCALAR.OBIS.CUMULATIVE_REACTIVEENERGY_IMPORT.getCode();
				break;
			default:
				break;
			}

			scalar = DLMSSCALAR.OBISSACLAR.getOBISScalar(code);
		}

		return scalar;
	}

	public void setLPData() {
		log.debug("  ");
		log.debug("##### LPData set START #####");
		try {
			List<LPData> lpDataList = new ArrayList<LPData>();

			Double lp = 0.0;
			Double lpValue = 0.0;
			Object value = null;
			Map<String, Object> lpMap = null;
			int cnt = 0;
			LPData _lpData = null;

			Double[] ch;
			Double chValue = 0.0;
			ArrayList<Double> chList = new ArrayList<Double>();

			for (int i = 0; i < result.size(); i++) {
				
				if (!result.containsKey(OBIS.ENERGY_LOAD_PROFILE.getCode() + "-" + i))
                    break;
				
				if (result.containsKey(OBIS.ENERGY_LOAD_PROFILE.getCode() + "-" + i)) {					
					lpMap = (Map<String, Object>) result.get(OBIS.ENERGY_LOAD_PROFILE.getCode() + "-" + i);
				}			
				
				cnt = 0;
				
				if(lpMap == null){
					break;
				}else if((value = lpMap.get("LpInterval")) != null){
					lpInterval = Integer.parseInt(value.toString());
					log.debug("### LP Interval = " + lpInterval);										
				}
				
				while ((value = lpMap.get("Channel[1]" + "-" + cnt)) != null) {
					chList.clear();

					log.debug("channel[1] VALUE RAWDATA=" + value.toString());
					if (value instanceof OCTET)
						chValue = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
					else if (value instanceof Long)
						chValue = ((Long) value).doubleValue();
					else if (value instanceof Float)
						chValue = ((Float) value).doubleValue();
					
					chValue = getCalcCTVT(chValue); // ctvt
					chValue = (chValue * getOBISScalar(1)) * 0.001; // Cumulative active energy -import
					
					lp = chValue;
					lpValue = chValue;
					chList.add(chValue);

					value = lpMap.get("Channel[2]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							chValue = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							chValue = ((Long) value).doubleValue();
						else if (value instanceof Float)
							chValue = ((Float) value).doubleValue();
						
						chValue = getCalcCTVT(chValue); // ctvt						
						chValue = (chValue * getOBISScalar(2)) * 0.001; // Cumulative active energy -import rate 1

						chList.add(chValue);
					}
					value = lpMap.get("Channel[3]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							chValue = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							chValue = ((Long) value).doubleValue();
						else if (value instanceof Float)
							chValue = ((Float) value).doubleValue();
						
						chValue = getCalcCTVT(chValue); // ctvt
						chValue = (chValue * getOBISScalar(3)) * 0.001; // Cumulative active energy -import rate 2
						
						chList.add(chValue);
					}
					value = lpMap.get("Channel[4]" + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							chValue = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if (value instanceof Long)
							chValue = ((Long) value).doubleValue();
						else if (value instanceof Float)
							chValue = ((Float) value).doubleValue();
						
						chValue = getCalcCTVT(chValue); // ctvt
						chValue = (chValue * getOBISScalar(4)) * 0.001; // Cumulative active energy -import rate 3
						
						chList.add(chValue);
					}
					
					log.debug("Parse Channel cnt:[" + cnt + "] ,Channel Length:[" + chList.size() + "]");
					ch = new Double[chList.size()];
					for (int j = 0; j < chList.size(); j++) {
						ch[j] = chList.get(j);
					}

//					String lpTime = (String) lpMap.get("DateTime" + "-" + cnt);
//					//lpTime = Util.addMinYymmdd(lpTime, -(this.meter.getLpInterval()));//
//					// 미터에서 LP시간이 00시00분부터 00시15분 사이의 데이터는 00시 15분으로 저장하기 때문에
//					//서버에서는 해당 기간의 사용데이터는 00시 00분으로 계산하므로 주기만큼 빼야함.
//
//					String revisionTime = Util.getQuaterYymmddhhmm(lpTime, lpInterval);
//					log.debug("### LP Time editing : " + lpTime + " => " + revisionTime);
//
//					_lpData = new LPData(revisionTime, lp, lpValue);
//					_lpData.setCh(ch);
//					_lpData.setPF(1d);

					
					//_lpData = new LPData((String) lpMap.get("DateTime" + "-" + cnt), lp, lpValue);
					_lpData = new LPData((String) lpMap.get("DateTime[0]" + "-" + cnt), lp, lpValue);
					_lpData.setCh(ch);
					_lpData.setPF(1d);

					// Status 값 저장
					value = lpMap.get(ENERGY_LOAD_PROFILE.Status.getName() + "-" + cnt);
					if (value != null) {
						_lpData.setStatus(String.valueOf(value));
					} else {
						_lpData.setStatus(null);
					}
					
					/*
					 * Modem에서 시간동기화를 할경우 00, 15, 30, 45 단위로 lp가 떨어지지 않고 03, 14.. 이런식으로 생성이되며
					 * 이렇게 생기는 LP는 버리기 위해서 아래 로직을 추가한다.
					 */
					int val = 0;
					if(_lpData.getDatetime() != null){
						int lpTime = Integer.parseInt(_lpData.getDatetime().substring(10, 12));
						val = lpTime % lpInterval;
					}
					
					cnt++;
					
					// [OPF-491]정전됐다 올라온 시간은 LP타임이 정확하지 않으므로 주석 처리
					
//					if (_lpData.getDatetime() == null || (0 != val)) { 
//						log.debug("#### [잘못된 LP_TIME 발생!!!] Interval = {" + lpInterval + "}, LP Time = " + _lpData.getDatetime());
//						log.debug("#### [잘못된 LP_TIME 발생!!!] Interval = {" + lpInterval + "}, LP Time = " + _lpData.getDatetime());
//						log.debug("#### [잘못된 LP_TIME 발생!!!] Interval = {" + lpInterval + "}, LP Time = " + _lpData.getDatetime());
//						continue;
//					}					
					

					if (_lpData.getDatetime() != null) {
						lpDataList.add(_lpData);
						log.debug("LP" + cnt + " ==> " + _lpData.toString());
					}
					
				}
			}

			Collections.sort(lpDataList,LPComparator.TIMESTAMP_ORDER);   
//          lpData = checkEmptyLP(lpDataList);		// DELETE SP-501
            
            lpDataList = checkDupLPAndWrongLPTime(lpDataList);
            lpData = lpDataList.toArray(new LPData[0]);	// INSERT SP-501 (Uncomment)
            log.debug("########################lpData.length:"+lpData.length);
			
		} catch (Exception e) {
			log.error("DLMSLSSmartMeter setLPData Error : " + e);
			e.printStackTrace();
		}
	}
	
	
	private List<LPData> checkDupLPAndWrongLPTime(List<LPData> list) throws Exception
    {
        List<LPData> totalList = list;
        List<LPData> removeList = new ArrayList<LPData>();
        LPData prevLPData = null;
    	
        for(int i = 0; i < list.size(); i++){
        	//SP-783
        	//Time consistency check
        	if (!checkLpDataTime(list.get(i).getDatetime())){        		
        		removeList.add(list.get(i));
                DataUtil.getBean(EventUtil.class).sendEvent("Meter Value Alarm",
                        TargetClass.valueOf(meter.getMeterType().getName()),
                        meter.getMdsId(),
                        new String[][] {{"message", "Wrong Date LP, DateTime[" + list.get(i).getDatetime() + "]"}}
                        );
        	}else{
	        	if(prevLPData!= null && prevLPData.getDatetime() != null && !prevLPData.getDatetime().equals("")){
	        		
	        		if(list.get(i).getDatetime().equals(prevLPData.getDatetime()) 
	        				&& list.get(i).getCh()[0].equals(prevLPData.getCh()[0])){
	        			//log.warn("time equls:" +list.get(i).getDatetime());  
	        			removeList.add(list.get(i));
	        			log.debug("list : " + list.get(i).getDatetime());
	        			log.debug("prevLPData : " + prevLPData.getDatetime());
	                    DataUtil.getBean(EventUtil.class).sendEvent("Meter Value Alarm",
                                TargetClass.valueOf(meter.getMeterType().getName()),
                                meter.getMdsId(),
                                new String[][] {{"message", "Duplicate LP, DateTime[" + list.get(i).getDatetime() + "] LP Val[" + list.get(i).getCh()[0] + "]"}}
                                );
	        		}else if(list.get(i).getDatetime().equals(prevLPData.getDatetime()) 
	        				&& list.get(i).getCh()[0] > prevLPData.getCh()[0]){
	        			log.debug("time equls:" +list.get(i).getDatetime()); 
	        			removeList.add(list.get(i-1));
	                    DataUtil.getBean(EventUtil.class).sendEvent("Meter Value Alarm",
                                TargetClass.valueOf(meter.getMeterType().getName()),
                                meter.getMdsId(),
                                new String[][] {{"message", "Duplicate LP and Diff Value DateTime[" + list.get(i).getDatetime() + "] LP Val[" + list.get(i).getCh()[0]+"/"+prevLPData.getCh()[0] + "]"}}
                                );
	        	    }else if(list.get(i).getDatetime().equals(prevLPData.getDatetime()) 
	        				&& list.get(i).getCh()[0] < prevLPData.getCh()[0]){
	        	    	log.debug("time equls:" +list.get(i).getDatetime()); 
	        			removeList.add(list.get(i));
                        DataUtil.getBean(EventUtil.class).sendEvent("Meter Value Alarm",
                                TargetClass.valueOf(meter.getMeterType().getName()),
                                meter.getMdsId(),
                                new String[][] {{"message", "Duplicate LP and Diff Value DateTime[" + list.get(i).getDatetime() + "] LP Val[" + list.get(i).getCh()[0]+"/"+prevLPData.getCh()[0] + "]"}}
                                );
	        	    }
	        		
	        	}
	        	prevLPData = list.get(i);
	        	
	        	if(list.get(i).getDatetime().startsWith("1994") 
	        			|| list.get(i).getDatetime().startsWith("2000")
	        			|| (list.get(i).getDatetime().startsWith("2057") && !TimeUtil.getCurrentTime().startsWith("205"))){
	        		removeList.add(list.get(i));
                    DataUtil.getBean(EventUtil.class).sendEvent("Meter Value Alarm",
                            TargetClass.valueOf(meter.getMeterType().getName()),
                            meter.getMdsId(),
                            new String[][] {{"message", "Wrong Date LP, DateTime[" + list.get(i).getDatetime() + "]"}}
                            );
	        	}
	        	/*if(meterTime != null && !"".equals(meterTime) 
	        			&& meterTime.length() == 14 
	        			&& list.get(i).getDatetime().compareTo(meterTime.substring(0, 12)) > 0){
	        		removeList.add(list.get(i));
	                try {
	                    EventUtil.sendEvent("Meter Value Alarm",
	                            TargetClass.valueOf(meter.getMeterType().getName()),
	                            meter.getMdsId(),
	                            new String[][] {{"message", "Wrong Date LP, DateTime[" + list.get(i).getDatetime() + "] Meter Time[" + meterTime + "]"}}
	                            );
	                }
	                catch (Exception ignore) {
	                }
	        	}*/      	
	        	
	            Long lpTime = DateTimeUtil.getDateFromYYYYMMDDHHMMSS(list.get(i).getDatetime()+"00").getTime();
	            Long serverTime = new Date().getTime(); ;
	            /*
	        	if(lpTime > serverTime){
	                try {
	                    EventUtil.sendEvent("Meter Value Alarm",
	                            TargetClass.valueOf(meter.getMeterType().getName()),
	                            meter.getMdsId(),
	                            new String[][] {{"message", "Wrong Date LP, DateTime[" + list.get(i).getDatetime() + "] Current Time[" + TimeUtil.getCurrentTime() + "]"}}
	                            );
	                }
	                catch (Exception ignore) {
	                }
	        	}*/
        	}
        }
      
        totalList.removeAll(removeList);
        return totalList;
    }
	
	/**
     * Time consistency check
     * @param chkDate
     * @return
     */
    private boolean checkLpDataTime(String chkDate) {
    	boolean ret = true;
    	String cd = chkDate.substring(8, 12);
    	if ("5255".equals(cd)){
    		ret = false;
    	}else{
        	// Time check
        	String hh = cd.substring(0, 2);
        	String mm = cd.substring(2, 4);
        	if (Integer.parseInt(hh) > 23){
        		ret = false;
        	}else if (Integer.parseInt(mm) > 59){
        		ret = false;
        	}
    	}

    	return ret;
    }

	private LPData[] checkEmptyLP(List<LPData> list) throws Exception
    {
        ArrayList<LPData> emptylist = new ArrayList<LPData>();
        List<LPData> totalList = list;
        
        int channelCount = 4; 
        
        if(list != null && list.size() > 0){
        	channelCount = list.get(0).getCh().length;
        }
        Double[] ch  = new Double[channelCount];
        Double[] v  = new Double[channelCount];
        
        for(int i = 0; i < channelCount; i++){
            ch[i] = new Double(0.0);
            v[i] = new Double(0.0);
        }
        
        String prevTime = "";
        String currentTime = "";
        Double lp = 0.0;
        Double lpValue = 0.0;

        Iterator<LPData> it = totalList.iterator();
        while(it.hasNext()) {
        	
        	LPData prev = (LPData)it.next();
            currentTime = prev.getDatetime();
            lp = prev.getLp();
            lpValue = prev.getLpValue();
            ch = prev.getCh();				// INSERT SP-467

            if(prevTime != null && !prevTime.equals("")){
                String temp = Util.addMinYymmdd(prevTime, lpInterval);
                if(!temp.equals(currentTime))
                {

                    int diffMin = (int) ((Util.getMilliTimes(currentTime+"00")-Util.getMilliTimes(prevTime+"00"))/1000/60) - lpInterval;
                    
                    if(diffMin > 0 && diffMin <= 1440){ //하루이상 차이가 나지 않을때 빈값으로 채운다. 
                        for(int i = 0; i < (diffMin/lpInterval) ; i++){
                        	
                            log.debug("empty lp temp : "+ currentTime+", diff Min="+diffMin);
                            
                            LPData data = new LPData();
                            data.setLp(lp);
                            data.setLpValue(lpValue);
                            data.setV(v);
                            data.setCh(ch);
                            data.setFlag(0);
                            data.setPF(1.0);
                            // UPDATE START SP-467
                            //data.setDatetime(Util.addMinYymmdd(prevTime, lpInterval*(i+1)));
                            data.setDatetime((Util.addMinYymmdd(prevTime, lpInterval*(i+1))).substring(0, 12));
                            // UPDATE END SP-467
                            emptylist.add(data);
                        } 
                    }

                }
            }
            prevTime = currentTime;

        }
        
        Iterator<LPData> it2 = emptylist.iterator();
        while(it2.hasNext()){
            totalList.add((LPData)it2.next());
        }
        
        Collections.sort(totalList,LPComparator.TIMESTAMP_ORDER);  
        
        return totalList.toArray(new LPData[0]);
    }

	private List<LPData> checkDupLP(int lpInterval, List<LPData> list) throws Exception {
		log.debug("Checking... Duplication LP data.");
		List<LPData> totalList = list;
		List<LPData> removeList = new ArrayList<LPData>();
		LPData prevLPData = null;

		for (LPData lpData : totalList) {

			if (prevLPData != null && prevLPData.getDatetime() != null && !prevLPData.getDatetime().equals("")) {
				//if (lpData.getDatetime().equals(prevLPData.getDatetime()) && !lpData.getStatus().contains("Clock adjusted") && !lpData.getStatus().contains("Daylight saving")) {
				if (lpData.getDatetime().equals(prevLPData.getDatetime())) {
					log.debug(" ==>> found equal time lp : " + lpData.getDatetime() + ", LPStatus = " + (lpData == null ? "null" : lpData.getStatus()));
					removeList.add(lpData);
				}
			}
			prevLPData = lpData;
		}

		for (LPData lpData : removeList) {
			totalList.remove(lpData);
		}

		return totalList;
	}

	public void setMeterInfo() {
		try {
			
			EnergyMeter meter = (EnergyMeter) this.getMeter();
			
			Map<String, Object> map = null;
			map = (Map<String, Object>) result.get(OBIS.DEVICE_INFO.getCode());
			if (map != null) {
				Object obj = null;
				obj = map.get(OBIS.DEVICE_INFO.getName());
				if (obj != null)
					meterID = (String) obj;
				log.debug("METER_ID[" + meterID + "]");

				data.put("Meter Serial", meterID);
			}
			map = (Map<String, Object>) result.get(OBIS.MANUFACTURE_SERIAL.getCode());
			if (map != null) {
				Object obj = map.get(OBIS.MANUFACTURE_SERIAL.getName());
				if (obj != null)
					manufactureSerial = (String) obj;
				log.debug("MANUFACTURE_SERIAL[" + manufactureSerial + "]");
			}
			map = (Map<String, Object>) result.get(OBIS.METER_TIME.getCode());
			if (map != null) {
				Object obj = map.get(OBIS.METER_TIME.getName());
				if (obj != null)
					meterTime = (String) obj;
				if (meterTime != null && meterTime.length() != 14) {
					meterTime = meterTime + "00";
				}
				log.debug("METER_TIME[" + meterTime + "]");
			}
			map = (Map<String, Object>) result.get(OBIS.METER_MODEL.getCode());
			if (map != null) {
				Object obj = map.get(OBIS.METER_MODEL.getName());
				if (obj != null)
					meterModel = (String) obj;
				log.debug("METER_MODEL[" + meterModel + "]");
			}
			map = (Map<String, Object>) result.get(OBIS.PHASE_TYPE.getCode());
			if (map != null) {
				Object obj = map.get(OBIS.PHASE_TYPE.getName());
				if (obj != null)
					phaseType = String.valueOf(obj);
				log.debug("PHASE_TYPE[" + phaseType + "]");
			}
			map = (Map<String, Object>) result.get(OBIS.LOGICAL_NUMBER.getCode());
			if (map != null) {
				Object obj = map.get(OBIS.LOGICAL_NUMBER.getName());
				if (obj != null)
					logicalNumber = (String) obj;
				log.debug("LOGICAL_NUMBER[" + logicalNumber + "]");
			}
			map = (Map<String, Object>) result.get(OBIS.FW_VERSION.getCode());/* 없음 */
			if (map != null) {
				Object obj = map.get(OBIS.FW_VERSION.getName());
				if (obj != null)
					fwVersion = (String) obj;
				log.debug("FW_VERSION[" + fwVersion + "]");
			}
			map = (Map<String, Object>) result.get(OBIS.ACTIVE_FIRMWARE_DATE.getCode());/* 없음 */
			if (map != null) {
				Object obj = map.get(OBIS.ACTIVE_FIRMWARE_DATE.getName());
				if (obj != null)
					otaFwVersion = (String) obj;
				log.debug("ACTIVE_FIRMWARE_DATE[" + otaFwVersion + "]");
			}
			/*
			map = (Map<String, Object>)result.get(OBIS.METER_STATUS.getCode());
			if (map != null) {
				Object obj = map.get(OBIS.METER_STATUS.getName());
				if (obj != null) meterStatus = (byte[])obj;
				log.debug("METER_STATUS[" + Hex.decode(meterStatus) + "]");
			}
			*/
			/*
			map = (Map<String, Object>)result.get(OBIS.RELAY_STATUS.getCode());
			if (map != null) {
				Object obj = map.get(OBIS.RELAY_STATUS.getName());
				if (obj != null) relayStatus = (byte[])obj;
				log.debug("RELAY_STATUS[" + Hex.decode(relayStatus) + "]");
			}
			*/
			map = (Map<String, Object>) result.get(OBIS.CT.getCode());
			if (map != null) {
				Object obj = null;
				obj = map.get(OBIS.CT.getName());				
				if (obj != null)
				if (obj instanceof OCTET)
					ct = (double)DataUtil.getLongToBytes(((OCTET)obj).getValue());
            	else if (obj instanceof Long)
            		ct = ((Long)obj).doubleValue();
            	else if (obj instanceof Float)
            		ct = ((Float)obj).doubleValue();
            	
				log.debug("CT[" + ct + "]");
				data.put("CT", ct);
				meter.setCt(ct);
			}
			
			map = (Map<String, Object>) result.get(OBIS.VT.getCode());
			if (map != null) {
				Object obj = null;
				obj = map.get(OBIS.VT.getName());
				if (obj != null)
				if (obj instanceof OCTET)
					vt = (double)DataUtil.getLongToBytes(((OCTET)obj).getValue());
            	else if (obj instanceof Long)
            		vt = ((Long)obj).doubleValue();
            	else if (obj instanceof Float)
            		vt = ((Float)obj).doubleValue();
				
				log.debug("VT[" + vt + "]");
				data.put("VT", vt);
				meter.setVt(vt);
			}
			
			map = (Map<String, Object>) result.get(OBIS.CT_DEN.getCode()); // 분모
			if (map != null) {
				Object obj = null;
				obj = map.get(OBIS.CT_DEN.getName());				
				if (obj != null)
				if (obj instanceof OCTET)
					ct_den = (double)DataUtil.getLongToBytes(((OCTET)obj).getValue());
            	else if (obj instanceof Long)
            		ct_den = ((Long)obj).doubleValue();
            	else if (obj instanceof Float)
            		ct_den = ((Float)obj).doubleValue();
            	
				log.debug("CT_DEN[" + ct_den + "]");
				data.put("CT_DEN", ct_den);
				meter.setCt2(ct_den);
			}
			
			map = (Map<String, Object>) result.get(OBIS.VT_DEN.getCode());
			if (map != null) {
				Object obj = null;
				obj = map.get(OBIS.VT_DEN.getName());
				if (obj != null)
				if (obj instanceof OCTET)
					vt_den = (double)DataUtil.getLongToBytes(((OCTET)obj).getValue());
            	else if (obj instanceof Long)
            		vt_den = ((Long)obj).doubleValue();
            	else if (obj instanceof Float)
            		vt_den = ((Float)obj).doubleValue();
				
				log.debug("VT_DEN[" + vt_den + "]");
				data.put("VT_DEN", vt_den);
				meter.setVt2(vt_den);
			}
			
			
			//(Byte0) Bit0	Prepayment energy
			//(Byte0) Bit1	Prepayment TOU
			//(Byte0) Bit2	Prepayment Step
			//(Byte0) Bit3	Postpaid TOU
			//(Byte0) Bit4	Postpaid Step

			
			map = (Map<String, Object>) result.get(OBIS.PAYMENT_MODE_SETTING.getCode());
			if (map != null) {
				Object obj = null;
				obj = map.get(OBIS.PAYMENT_MODE_SETTING.getName());
				if (obj != null)
				if (obj instanceof OCTET)
					paymentMode = ((OCTET)obj).encode()[0];
            	else if (obj instanceof Integer)
            		paymentMode = ((Integer)obj).byteValue();
				
				log.debug("PAYMENT_MODE_SETTING[" + paymentMode + "]");
				data.put("PAYMENT_MODE_SETTING", paymentMode);
				//TODO set contract payment type
			}

			map = (Map<String, Object>) result.get(OBIS.TOTAL_OWE_CREDIT.getCode());
			if (map != null) {
				Object obj = null;
				obj = map.get(OBIS.TOTAL_OWE_CREDIT.getName());
				if (obj != null)
				if (obj instanceof OCTET)
					totalCredit = (double)DataUtil.getLongToBytes(((OCTET)obj).getValue());
            	else if (obj instanceof Long)
            		totalCredit = ((Long)obj).doubleValue();
            	else if (obj instanceof Float)
            		totalCredit = ((Float)obj).doubleValue();
				
				log.debug("TOTAL_OWE_CREDIT[" + totalCredit + "]");
				data.put("TOTAL_OWE_CREDIT", totalCredit);
				//TODO set contract.current_credit
			}
			
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	/**
	 * setPowerQuality
	 */
	public void setPowerQualityData() {

		Modem modem = meter.getModem();
		String modelName = "NAMR-P20CSR";//modem.getModel().getName();
		String meterModelName = "NRAM-1405DR60";//meter.getModel().getName();
		if(!("NAMR-P20CSR".equals(modelName) || "NAMR-W401LT".equals(modelName))) {
			log.info("# Does not accept Power Qulity Log. meterId[" + meter.getMdsId()+"] modem["+modem.getDeviceSerial()+"] modemnName["+modelName+"]");
			return;
		}
		
		List<Instrument> instrumentlist = new ArrayList<Instrument>();
		//CT  NRAM-3405CT10
		/*
		Clock	0.0.1.0.0.255 
		L1 voltage	1.0.32.7.0.255
		L2 voltage	1.0.52.7.0.255
		L3 voltage	1.0.72.7.0.255
		L1 current	1.0.31.7.0.255
		L2 current	1.0.51.7.0.255
		L3 current	1.0.71.7.0.255
		L1 active power+	1.0.21.7.0.255
		L2 active power+	1.0.41.7.0.255
		L3 active power+	1.0.61.7.0.255
		L1 active power-	1.0.22.7.0.255
		L2 active power- 	1.0.42.7.0.255
		L3 active power- 	1.0.62.7.0.255
		L1 apparent  power+	1.0.29.7.0.255
		L1 apparent  power-	1.0.30.7.0.255
		L2 apparent  power+	1.0.49.7.0.255
		L2 apparent  power-	1.0.50.7.0.255
		L3 apparent  power+	1.0.69.7.0.255
		L3 apparent  power-	1.0.70.7.0.255
		Supply frequency	1.0.14.7.0.255
		L1 supply frequency	1.0.34.7.0.255
		L2 supply frequency	1.0.54.7.0.255
		L3 supply frequency	1.0.74.7.0.255
		*/
		
		/* NRAM-3410DR100
		Clock
		L1 voltage
		L2 voltage
		L3 voltage
		L1 current
		L2 current
		L3 current
		L1 active power+
		L2 active power+
		L3 active power+
		L1 active power-
		L2 active power- 
		L3 active power- 
		L1 apparent  power+
		L1 apparent  power-
		L2 apparent  power+
		L2 apparent  power-
		L3 apparent  power+
		L3 apparent  power-
		*/

		/* NRAM-1405DR60
		Clock
		L1 voltage
		L1 current
		L1 active power+
		L1 active power-
		L1 apparent  power+
		L1 apparent  power-
		*/

		log.debug("##### PQ(Power load profile)Data set START #####");
		try {
			int cnt = 0;
			Map<String, Object> lpMap = null;
			Object value = null;
			Instrument pq = null;
			Double chValue = 0.0;
			ArrayList<Double> chList = new ArrayList<Double>();
			final int MAX_PQ_CHANNEL = 22;
			
			for (int i = 0; i < result.size(); i++) {
				if (!result.containsKey(OBIS.POWER_LOAD_PROFILE.getCode() + "-" + i))
                    break;
				
				if (result.containsKey(OBIS.POWER_LOAD_PROFILE.getCode() + "-" + i)) {
					lpMap = (Map<String, Object>) result.get(OBIS.POWER_LOAD_PROFILE.getCode() + "-" + i);
				}
				
				cnt = 0;
				if(lpMap == null){
					break;
				}
				
				while ((value = lpMap.get("Channel[1]" + "-" + cnt)) != null) {
					chList.clear();
										
					String dateKey = "DateTime[0]-" + cnt;
					String dateTimeValue = (String)lpMap.get(dateKey);
					LinkedList<Double> chValueList = new LinkedList<Double>();
					
					for(int num=1; num < (MAX_PQ_CHANNEL + 1) ; num++) {
						String ChannelKey = "Channel[" + num + "]-" + cnt;
						value = lpMap.get(ChannelKey);
						
						if(value != null) {
							if (value instanceof OCTET)
								chValue = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
							else if (value instanceof Long)
								chValue = ((Long) value).doubleValue();
							else if (value instanceof Float)
								chValue = ((Float) value).doubleValue();
							
							chValueList.add(chValue);
						}
					}
					
					if(chValueList.size() > 0) {
						pq = new Instrument();
						
						pq.setDatetime(dateTimeValue);
						if("NRAM-1405DR60".equals(meterModelName) && chValueList.size() == 6) { // Single Phase Meter
							for(int j=0; j<chValueList.size(); j++) {
								switch(j) {
								case 0:
									pq.setVOL_A(setDecimalPoint(chValueList.get(j), 3)); break;
								case 1:
									pq.setCURR_A(setDecimalPoint(chValueList.get(j), 3)); break;
								case 2:
									pq.setKW_A(setDecimalPoint(chValueList.get(j), 4)); break;
								case 3: //skep active power (-)  
									break;
								case 4:
									pq.setKVA_A(setDecimalPoint(chValueList.get(j), 4)); break;
								case 5: //skep active power (-) 
									break;
								default:
									break;
								}
							}
						} else if(("NRAM-3410DR100".equals(meterModelName) && chValueList.size() == 18) // Three Phase Meter
								|| ("NRAM-3405CT10".equals(meterModelName) && chValueList.size() == 22)) { //CT Meter
							for(int j=0; j<chValueList.size(); j++) {
								switch(j) {
								case 0:
									pq.setVOL_A(setDecimalPoint(chValueList.get(j), 3)); break;
								case 1:
									pq.setVOL_B(setDecimalPoint(chValueList.get(j), 3)); break;
								case 2:
									pq.setVOL_C(setDecimalPoint(chValueList.get(j), 3)); break;
								case 3:
									pq.setCURR_A(setDecimalPoint(chValueList.get(j), 3)); break;
								case 4:
									pq.setCURR_B(setDecimalPoint(chValueList.get(j), 3)); break;
								case 5:
									pq.setCURR_C(setDecimalPoint(chValueList.get(j), 3)); break;
								case 6:
									pq.setKW_A(setDecimalPoint(chValueList.get(j), 4)); break;
								case 7:
									pq.setKW_B(setDecimalPoint(chValueList.get(j), 4)); break;
								case 8:
									pq.setKW_C(setDecimalPoint(chValueList.get(j), 4)); break;
								case 9: //skip L1 active power (-) 
								case 10: //skip L2 active power (-) 
								case 11: //skip L3 active power (-) 
									break;
								case 12:
									pq.setKVA_A(setDecimalPoint(chValueList.get(j), 4)); break;
								case 13: //skip L1 apparent power (-) 
									break;
								case 14:
									pq.setKVA_A(setDecimalPoint(chValueList.get(j), 4)); break;
								case 15: //skip L2 apparent power (-) 
									break;
								case 16:
									pq.setKVA_A(setDecimalPoint(chValueList.get(j), 4)); break;
								case 17: //skip L3 apparent power (-) 
									break;
								default:
									if("NRAM-3405CT10".equals(meterModelName)) {
										switch(j) {
										case 18:
											pq.setLINE_FREQUENCY(setDecimalPoint(chValueList.get(j), 2)); break;
										case 19:
											pq.setLine_frequencyA(setDecimalPoint(chValueList.get(j), 2)); break;
										case 20:
											pq.setLine_frequencyB(setDecimalPoint(chValueList.get(j), 2)); break;
										case 21:
											pq.setLine_frequencyC(setDecimalPoint(chValueList.get(j), 2)); break;
										default:
											break;
										}
									}
									break;
								}
							}
						}
						
						instrumentlist.add(pq);
						log.debug("Power Load Profile" + cnt + " ==> " + pq.toString());
					}
					cnt++;
				}
			}			
			
			if(instrumentlist != null && instrumentlist.size() > 0) {
	            instruments = instrumentlist.toArray(new Instrument[0]);
			}

            log.debug("######################## instrumentlist.length:"+instrumentlist.size());
			
		} catch (Exception e) {
			log.error("DLMSLSSmartMeter setPowerLoadProfileData Error : " + e);
		}
	}
	
	public void setTokenHistory() {		
		
		int cnt = 0;
		stsLogs = new ArrayList<STSLog>();
		Map<String, Object> stsMap = null;
		Object value = null;
		STSLog stsLog = null;
		
		String tokenDate = null;
		String lastInputToken = null;
		String lastInputTID = null;
		Double lastCreditEnergy = null;
		Double lastCreditTriff = null;
		
		for (int i = 0; i < result.size(); i++) {
			if (!result.containsKey(OBIS.TOKEN_CREDIT_HISTORY.getCode() + "-" + i))
				break;

			stsMap = (Map<String, Object>) result.get(OBIS.TOKEN_CREDIT_HISTORY.getCode() + "-" + i);
			
			if (stsMap != null && stsMap.size() > 0) {
				cnt = 0;
				while ((value = stsMap.get(DLMSECGTable.TOKEN_HISTORY_OBIS.CLOCK.name() + "-" + cnt)) != null) {

					tokenDate = (String) stsMap.get(DLMSECGTable.TOKEN_HISTORY_OBIS.CLOCK.name() + "-" + cnt);

					//log.debug("0. [Clock] DateTime = " + tokenDate);
					
					value = stsMap.get(DLMSECGTable.TOKEN_HISTORY_OBIS.LAST_INPUT_TOKEN.name() + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							lastInputToken = ((OCTET) value).toString();						
						else if(value instanceof String)
							lastInputToken = (String) value;
						
						//log.debug("2. [Last Input Token] RAWDATA = " + lastInputToken.toString());
					}
					value = stsMap.get(DLMSECGTable.TOKEN_HISTORY_OBIS.LAST_INPUT_TID.name() + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							lastInputTID = ((OCTET) value).toString();
						else if(value instanceof String)
							lastInputTID = (String) value;

						//log.debug("3. [remainCredit] RAWDATA = " + lastInputTID.toString());
					}
					value = stsMap.get(DLMSECGTable.TOKEN_HISTORY_OBIS.LAST_PURCHASE_CREDIT_ENERGY.name() + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							lastCreditEnergy = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if(value instanceof Long)
							lastCreditEnergy = ((Long) value).doubleValue();						
						
						//log.debug("4. [Last Input Token TID] RAWDATA = " + lastCreditEnergy);
					}
					value = stsMap.get(DLMSECGTable.TOKEN_HISTORY_OBIS.LAST_PURCHASE_CREDIT_TARIFF.name() + "-" + cnt);
					if (value != null) {
						if (value instanceof OCTET)
							lastCreditTriff = (double) DataUtil.getLongToBytes(((OCTET) value).getValue());
						else if(value instanceof Long)
							lastCreditTriff = ((Long) value).doubleValue();						
						
						//log.debug("5. [tokenNumber] RAWDATA = " + lastCreditTriff);
					}

					
					stsLog = new STSLog();
					stsLog.setTokenDate(tokenDate);
					stsLog.setTokenTid(lastInputTID);
					stsLog.setTokenNumber(lastInputToken);
					
					if(lastCreditEnergy != 0)
						stsLog.setLastCredit(lastCreditEnergy * 0.001);
					else if(lastCreditTriff != 0)
						stsLog.setLastCredit(lastCreditTriff * 0.01);
					//stsLog.setRemainCredit(remainCredit); 해당 정보는 OBIS 결과값에 포함되지 않음
					//stsLogs.add(stsLog);
					
					LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
					map.put(TOKEN_HISTORY_OBIS.CLOCK.name(), tokenDate);
					map.put(TOKEN_HISTORY_OBIS.LAST_INPUT_TOKEN.name(), lastInputToken);
					map.put(TOKEN_HISTORY_OBIS.LAST_INPUT_TID.name(), lastInputTID);
					map.put(TOKEN_HISTORY_OBIS.LAST_PURCHASE_CREDIT_ENERGY.name(), (lastCreditEnergy == 0) ? "0" : String.format("%.3f", (lastCreditEnergy * 0.001)));
					map.put(TOKEN_HISTORY_OBIS.LAST_PURCHASE_CREDIT_TARIFF.name(), (lastCreditTriff == 0) ? "0" : String.format("%.2f", (lastCreditTriff * 0.01)));
					tokenChargeData.add(map);
					
					tokenDate = null;
					lastInputToken = null;
					lastInputTID = null;
					lastCreditEnergy = null;
					lastCreditTriff = null;
					
					cnt++;
				}
			}
		}
		log.debug("##### STSLog set STOP #####");
	}

	public List<STSLog> getStsLogs() {
		return stsLogs;
	}

	public void setStsLogs(List<STSLog> stsLogs) {
		this.stsLogs = stsLogs;
	}

	public List<EventLogData> getMeterAlarmLog() {
		return meterAlarmLog;
	}

	public List<Map<String, Object>> getEventLog() {

		List<Map<String, Object>> eventLogs = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < result.size(); i++) {
			if (result.get(OBIS.STANDARD_EVENT.getCode() + "-" + i) != null) {
				eventLogs.add(result.get(OBIS.STANDARD_EVENT.getCode() + "-" + i));
			}
		}

		for (int i = 0; i < result.size(); i++) {
			if (result.get(OBIS.RELAY_EVENT.getCode() + "-" + i) != null)
				eventLogs.add(result.get(OBIS.RELAY_EVENT.getCode() + "-" + i));
		}

		for (int i = 0; i < result.size(); i++) {
			if (result.get(OBIS.FRAUDDETECTIONLOGEVENT.getCode() + "-" + i) != null)
				eventLogs.add(result.get(OBIS.FRAUDDETECTIONLOGEVENT.getCode() + "-" + i));
		}

		for (int i = 0; i < result.size(); i++) {
			if (result.get(OBIS.MEASUREMENT_EVENT.getCode() + "-" + i) != null)
				eventLogs.add(result.get(OBIS.MEASUREMENT_EVENT.getCode() + "-" + i));
		}


		return eventLogs;
	}




	public LinkedHashMap<String, Object> getRelayStatus() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		Map<String, Object> loadControlMap = result.get(OBIS.RELAY_STATUS.getCode());
		log.debug("LoadControlStatus : " + loadControlMap.get("LoadControlStatus"));
		map.put("LoadControlStatus", loadControlMap.get("LoadControlStatus"));
		return map;
	}

	public Double getLQISNRValue() {

		Object obj = null;
		if (result.get(OBIS.WEAK_LQI_VALUE.getCode()) != null) {
			obj = result.get(OBIS.WEAK_LQI_VALUE.getCode()).get(OBIS.WEAK_LQI_VALUE.getName());
			if (obj != null) {
				log.debug("LQI SNR[" + obj + "]");
				if (obj instanceof Double) {
					return (Double) obj;
				}
			}
		}

		return null;
	}

	public Integer getLpInterval() {
		return this.lpInterval;
	}

	public Double getActivePulseConstant() {
		return this.activePulseConstant;
	}

	public Double getReActivePulseConstant() {
		return this.reActivePulseConstant;
	}

	public String getMeterID() {
		return this.meterID;
	}
	
	public String getModemId() {
		return modemID;
	}

	public void setModemId(String modemID) {
		this.modemID = modemID;
	}

	public void setCt(Double ct) {
		this.ct = ct;
	}

	public Double getCt() {
		return this.ct;
	}
	
	public void setVt(Double vt) {
		this.vt = vt;
	}

	public Double getVt() {
		return this.vt;
	}
	
	
	public void setCt_den(Double ct_den) {
		this.ct_den = ct_den;
	}

	public Double getCt_den() {
		return this.ct_den;
	}
	
	public void setVt_den(Double vt_den) {
		this.vt_den = vt_den;
	}

	public Double getVt_den() {
		return this.vt_den;
	}

	public String getFwVersion() {
		if(otaFwVersion != null && otaFwVersion.length() > 0) {
			fwVersion = fwVersion + "___" + otaFwVersion;
		}
		
		return fwVersion;
	}

	public void setFwVersion(String fwVersion) {
		this.fwVersion = fwVersion;
	}
	
	public String getMeterModel() {

    	if(this.meterModel.equals("WS34-201-W5")) {
    		this.meterModel = "NRAM-3405CT10";
    	}
    	else if(this.meterModel.equals("WS34-210-W5")) {
    		this.meterModel = "NRAM-3410DR100";
    	}
    	else if(this.meterModel.startsWith("WS14")) {
    		this.meterModel = "NRAM-1405DR60";
    	}
    	else if(this.meterModel.startsWith("WS12") || this.meterModel.startsWith("NJ12")) {
    		this.meterModel = "NRDT-1205DR60";
    	}
    	
    	
    	return this.meterModel;
    }

	// CTVT 미터인 경우만 CT, VT Value 적용
	// 1차측 = 2차측 * [ CT(num/den) * VT(num/den) ]
	public Double getCalcCTVT(Double val) {
		
		Double primaryVal = 0.0;
		Double _ct = (ct == null) ? 1 : ct;
		Double _ct_den = (ct_den == null) ? 1 : ct_den;
		Double _vt = (vt == null) ? 1 : vt;
		Double _vt_den = (vt_den == null) ? 1 : vt_den;
				
		if(meterModel.equals("NRAM-3405CT10")) { // CTVT 미터인 경우만 해당
			primaryVal = val * ((_ct/_ct_den) * (_vt/_vt_den));
//			log.debug("### val ["+val+"]  primaryVal ["+primaryVal+"]  _ct  ["+_ct+"] _ct_den  ["+_ct_den+"] _vt  ["+_vt+"] _vt_den  ["+_vt_den+"]");
			return primaryVal;
		} else {
			return val;
		}
	}
	
	// CTVT 미터 PowerQuality - Voltage
	// 1차측 = 2차측 * [ VT(num/den) ]
	public Double getCalcVT(Double val) {
		
		Double primaryVal = 0.0;		
		Double _vt = (vt == null) ? 1 : vt;
		Double _vt_den = (vt_den == null) ? 1 : vt_den;
				
		if(meterModel.equals("NRAM-3405CT10")) { // CTVT 미터인 경우만 해당
			primaryVal = val * (_vt/_vt_den);
			return primaryVal;
		} else {
			return val;
		}
	}
	
	// CTVT 미터 PowerQuality - Current
	// 1차측 = 2차측 * [ CT(num/den) ]
	public Double getCalcCT(Double val) {
		
		Double primaryVal = 0.0;
		Double _ct = (ct == null) ? 1 : ct;
		Double _ct_den = (ct_den == null) ? 1 : ct_den;
				
		if(meterModel.equals("NRAM-3405CT10")) { // CTVT 미터인 경우만 해당
			primaryVal = val * (_ct/_ct_den);
			return primaryVal;
		} else {
			return val;
		}
	}	

	public String getStatusData() throws Exception {
		if(meterAlarmLog == null || meterAlarmLog.isEmpty()) {
			log.debug("#### return data empty!");
			return "";
		}
		
		int len = meterAlarmLog.size();
		StringBuilder builder = new StringBuilder();
				
		for(int i=0; i<meterAlarmLog.size(); i++) {
			builder.append(meterAlarmLog.get(i).getAppend());
			
			if(i < (len - 1)) {
				builder.append(",");
			}
		}
		
		log.debug("#### builder:"+builder.toString());
		return builder.toString();		
	}
	
	private double setDecimalPoint(Double value, int decimal) {
		double result = 0d;
		
		switch(decimal) {
		case 1:
			result = Double.parseDouble(String.format("%.1f", (value * 0.1)));
			break;
		case 2:
			result = Double.parseDouble(String.format("%.2f", (value * 0.01)));
			break;
		case 3:
			result = Double.parseDouble(String.format("%.3f", (value * 0.001)));
			break;
		case 4:
			result = Double.parseDouble(String.format("%.4f", (value * 0.0001)));
			break;
		}
		
		return result;
	}
	
	@SuppressWarnings("unused")
	private double diffMaxValue(double value) {
		String strValue = "" + value;
		strValue = strValue.substring(0, strValue.indexOf("."));
		double maxvalue = 1.0;
		for (int i = 0; i < strValue.length(); i++) {
			maxvalue *= 10;
		}
		return maxvalue - value;
	}

	@Override
	public int getFlag() {
		return 0;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public Double getMeteringValue() {
		return meteringValue;
	}

	@Override
	public byte[] getRawData() {
		return null;
	}

	public List<LinkedHashMap<String, Object>> getTokenChargeData() {
		return tokenChargeData;
	}
	
}
