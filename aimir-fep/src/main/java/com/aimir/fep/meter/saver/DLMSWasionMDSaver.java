package com.aimir.fep.meter.saver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import com.aimir.constants.CommonConstants;
import com.aimir.constants.CommonConstants.MeterStatus;
import com.aimir.constants.CommonConstants.MeterType;
import com.aimir.constants.CommonConstants.MeteringFlag;
import com.aimir.constants.CommonConstants.MeteringType;
import com.aimir.constants.CommonConstants.ModemType;
import com.aimir.constants.CommonConstants.Protocol;
import com.aimir.constants.CommonConstants.ResultStatus;
import com.aimir.dao.device.MMIUDao;
import com.aimir.dao.mvm.RealTimeBillingEMDao;
import com.aimir.dao.system.OperatorDao;
import com.aimir.dao.system.PrepaymentLogDao;
import com.aimir.fep.command.conf.DLMSMeta.LOAD_CONTROL_STATUS;
import com.aimir.fep.command.mbean.CommandGW;
import com.aimir.fep.command.mbean.CommandGW.OnDemandOption;
import com.aimir.fep.meter.AbstractMDSaver;
import com.aimir.fep.meter.data.BillingData;
import com.aimir.fep.meter.data.EventLogData;
import com.aimir.fep.meter.data.Instrument;
import com.aimir.fep.meter.data.LPData;
import com.aimir.fep.meter.data.MeterData;
import com.aimir.fep.meter.data.STSLog;
import com.aimir.fep.meter.entry.IMeasurementData;
import com.aimir.fep.meter.parser.DLMSWasion;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.EVENT_LOG;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.LP_STATUS;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.MEASUREMENT_STATUS_ALARM;
import com.aimir.fep.meter.parser.DLMSECGTable.LPComparator;
import com.aimir.fep.meter.parser.DLMSKaifaTable.DLMSVARIABLE.DLMS_CLASS;
import com.aimir.fep.meter.parser.DLMSKaifaTable.DLMSVARIABLE.DLMS_CLASS_ATTR;
import com.aimir.fep.meter.parser.DLMSKaifaTable.DLMSVARIABLE.OBIS;
import com.aimir.fep.meter.parser.DLMSKaifaTable.DLMSVARIABLE.UNIT;
import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandType;
import com.aimir.fep.protocol.nip.frame.GeneralFrame.NIAttributeId;
import com.aimir.fep.protocol.nip.frame.NIFrameConstants.CBControl;
import com.aimir.fep.protocol.nip.frame.NIFrameConstants.CBControlResult;
import com.aimir.fep.protocol.nip.frame.NIFrameConstants.CBStatus;
import com.aimir.fep.protocol.smsp.SMSConstants;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.FMPProperty;
import com.aimir.fep.util.Hex;
import com.aimir.fep.util.Util;
import com.aimir.model.device.EnergyMeter;
import com.aimir.model.device.MMIU;
import com.aimir.model.device.Meter;
import com.aimir.model.device.Modem;
import com.aimir.model.system.Code;
import com.aimir.model.system.Contract;
import com.aimir.model.system.DeviceModel;
import com.aimir.model.system.Operator;
import com.aimir.model.system.PrepaymentLog;
import com.aimir.model.system.Supplier;
import com.aimir.util.Condition;
import com.aimir.util.Condition.Restriction;
import com.aimir.util.DateTimeUtil;
import com.aimir.util.TimeLocaleUtil;
import com.aimir.util.TimeUtil;

import net.sf.json.JSONObject;

@Service
public class DLMSWasionMDSaver extends AbstractMDSaver {
	private static Log log = LogFactory.getLog(DLMSWasionMDSaver.class);

	@Autowired
	RealTimeBillingEMDao billingEmDao;
	
	@Autowired
	MMIUDao mmiuDao;
	
	@Autowired
	PrepaymentLogDao pLogDao;
	
	@Autowired
	OperatorDao operatorDao;

	@Override
	public boolean save(IMeasurementData md) throws Exception {
		DLMSWasion parser = (DLMSWasion) md.getMeterDataParser();
		super.setParser(parser);
		
		log.info("############################## [" + parser.getMDevId()
				+ "] DLMSWasionMDSaver Saving Start!! ################################################");

		saveMeterInfomation(parser);
		log.debug("### 1/10 saveMeterInfomation complete ####");
		
		if (parser.getMeterModel() != null && !"".equals(parser.getMeterModel())) {
			updateMeterModel(parser.getMeter(), parser.getMeterModel());
		}

		String modemTime = parser.getMeteringTime();
		Double val = parser.getLQISNRValue();
		if (val != null && modemTime != null && !"".equals(modemTime) && modemTime.length() == 14) {
			saveSNRLog(parser.getDeviceId(), modemTime.substring(0, 8), modemTime.substring(8, 14),
					parser.getMeter().getModem(), val);
			log.debug("### 2/10 saveSNRLog complete ####");
		}

		// saveLp(md, parser);
		saveLpUsingLPTime(md, parser);
		log.debug("### 3/11 saveLp complete ####");
		saveAlarmLog(parser); // FUNCTION_STATUS_ALARM, DEVICE_STATUS_ALARM, MEASUREMENT_STATUS_ALARM
		log.debug("### 4/11 saveAlarmLog complete ####");
		saveEventLog(parser);
		log.debug("### 5/11 saveEventLog complete ####");
		savePowerQuality(parser);
		log.debug("### 6/8 savePowerQuality complete ####");
		saveDayProfile(parser);
		log.debug("### 7/11 saveDayProfile complete ####");
		saveMonthProfile(parser);
		log.debug("### 8/11 saveMonthProfile complete ####");
		//saveCurrBill(parser); // realtime billing em
		//log.debug("### 7/11 saveCurrBill complete ####");
		//saveCurrBillLog(parser); // realtime billing em
		//log.debug("### 8/11 saveCurrBillLog - overflow event log complete ####");
		savePowerQuality(parser);
		log.debug("### 9/11 savePowerQuality complete ####");
		saveRolloverEventLog(parser);
		log.debug("### 10/11 saveRolloverEventLog complete ####");
		saveSTSLog(parser);
		log.debug("### 11/11 saveRolloverEventLog complete ####");
		try {
			EnergyMeter meter = (EnergyMeter) parser.getMeter();
			String dsttime = DateTimeUtil.getDST(null, md.getTimeStamp());
			String notTime = DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss").trim();
			
			
			if(meter.getLastReadDate() == null) {
				meter.setLastReadDate(notTime); // EVN GPRS 모뎀의 timestamp 값이 안맞는 경우 발생하므로 미터 시간이 null인 경우 현재시간 입력
			}
			
			// [OPF-489]EVN의 경우 검침정보가 있으면 상태를 Normal 로 변경해 준다.
			Code meterStatus = CommonConstants.getMeterStatusByName(MeterStatus.Normal.name());			
			meter.setMeterStatus(meterStatus);
			log.debug("MDevId[" + parser.getMDevId() + "] METER_CHANGED_STATUS[" + meter.getMeterStatus() + "]");
			
			log.debug("METER CT [" + parser.getCt() + "]");
			log.debug("METER CT2 [" + parser.getCt_den() + "]");
			log.debug("METER VT [" + parser.getVt() + "]");
			log.debug("METER VT2 [" + parser.getVt_den() + "]");

			meter.setCt(parser.getCt()); // ct 분자
			meter.setCt2(parser.getCt_den()); // ct 분모
			meter.setVt(parser.getVt()); // vt 분자
			meter.setVt2(parser.getVt_den()); // vt 분모

			// 수검침과 같이 모뎀과 관련이 없는 경우는 예외로 처리한다.
			if (meter.getModem() != null) {
				meter.getModem().setLastLinkTime(notTime); // EVN 모뎀은 서버와
															// 통신한현재시간을 저장한다.
				log.debug("##### [SAVER] LAST LINK TIME : " + notTime);
				String tt = meter.getModem().getDeviceSerial();
				log.debug("##### [SAVER] Check last connected time[modem=" + tt + "] [dsttime-" + dsttime + " / 현재시간-"
						+ DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss").trim());
			}

		} catch (Exception ignore) {
			log.error("Meter/Modem xxxTime save error - " + ignore);
		}

		log.info("############################## [" + parser.getMDevId()
				+ "] DLMSLSSmartMeter Saving End!! ################################################");
		return true;
	}

	public void saveLpUsingLPTime(IMeasurementData md, DLMSWasion parser) throws Exception {
		try {
			LPData[] lplist = parser.getLPData();
			if (lplist == null || lplist.length == 0) {
				log.warn("LP size is 0!!");
				return;
			}
			log.debug("saveLpKaifaUsingLPTime Start Total LPSIZE => " + lplist.length);

			log.debug("active pulse constant:" + parser.getActivePulseConstant());
			log.debug(md.getTimeStamp().substring(0, 8));
			log.debug(md.getTimeStamp().substring(8, 14));
			log.debug(parser.getMeteringValue());
			log.debug(parser.getMeter().getMdsId());
			log.debug(parser.getDeviceType());
			log.debug(parser.getDeviceId());
			log.debug(parser.getMDevType());
			log.debug(parser.getMDevId());
			log.debug(parser.getMeterTime());

			// Save meter information
			Double meteringValue = parser.getMeteringValue() == null ? 0d : parser.getMeteringValue();
			Meter meter = parser.getMeter();
			String dsttime = DateTimeUtil.getDST(null, md.getTimeStamp());
			String meterTime = parser.getMeterTime();
			log.debug("MDevId[" + parser.getMDevId() + "] DSTTime[" + dsttime + "]");

			if (meterTime != null && !"".equals(meterTime))
				meter.setLastReadDate(meterTime);
			else
				meter.setLastReadDate(dsttime);
			meter.setLastMeteringValue(meteringValue);

			log.debug("MDevId[" + parser.getMDevId() + "] DSTTime[" + dsttime + "] LASTREADDate["
					+ meter.getLastReadDate() + "]");
//			Code normalStatus = CommonConstants.getMeterStatusByName(MeterStatus.Normal.name());
//			log.debug("MDevId[" + parser.getMDevId() + "] METER_STATUS["
//					+ (meter.getMeterStatus() == null ? "NULL" : meter.getMeterStatus()) + "]");
//			if (meter.getMeterStatus() == null
//					|| (meter.getMeterStatus() != null && !meter.getMeterStatus().getName().equals("CutOff")
//							&& !meter.getMeterStatus().getName().equals("Delete"))) {
//				meter.setMeterStatus(normalStatus);
//				log.debug("MDevId[" + parser.getMDevId() + "] METER_CHANGED_STATUS[" + meter.getMeterStatus() + "]");
//			}
			if (meterTime != null && !"".equals(meterTime)) {
				try {
					long diff = DateTimeUtil.getDateFromYYYYMMDDHHMMSS(md.getTimeStamp()).getTime()
							- DateTimeUtil.getDateFromYYYYMMDDHHMMSS(meterTime).getTime();
					meter.setTimeDiff(diff / 1000);
					log.debug("MDevId[" + parser.getMDevId() + "] Update timeDiff. diff=[" + meter.getTimeDiff() + "]"); // INSERT
																															// SP-406
				} catch (ParseException e) {
					log.warn("MDevId[" + parser.getMDevId() + "] Check MeterTime[" + meterTime + "] and MeteringTime["
							+ md.getTimeStamp() + "]");
				}
			}

			lpSaveUsingLPTime(md, lplist, parser);

		} catch (Exception e) {
			log.error(e, e);
		} finally {
			log.debug("MDevId[" + parser.getMDevId() + "] saveLpUsingLPTime finish");
		}
	}
	
	private boolean lpSaveUsingLPTime(IMeasurementData md, LPData[] validlplist,
			DLMSWasion parser) throws Exception {
        
		log.info("#########save mdevId:"+parser.getMDevId());
		
		ArrayList<String> dupdateList = new ArrayList<String>();
		ArrayList<String> dateList = new ArrayList<String>();
		
		for(LPData lp: validlplist) {
			dupdateList.add(lp.getDatetime().substring(0, 8));
		}
		
		HashSet hs = new HashSet(dupdateList);
		Iterator it = hs.iterator();
		while(it.hasNext()){
			dateList.add((String)it.next());
		}
		
		String[] dayList = dateList.toArray(new String[0]);
		Arrays.sort(dayList);
		log.debug("dateList:"+ArrayUtils.toString(dayList ,"-"));

		//split by date SP-890
		for(String day : dayList) {

			ArrayList<String> timeList = new ArrayList<String>();
			ArrayList<Integer> flagList = new ArrayList<Integer>();
			ArrayList<Double> pfList = new ArrayList<Double>();
			ArrayList<LPData> lpValueList = new ArrayList<LPData>();
			
			for(LPData lpdata : validlplist) {
				if(lpdata.getDatetime().substring(0, 8).equals(day)){
					timeList.add(lpdata.getDatetime());
					flagList.add(lpdata.getFlag());
					pfList.add(lpdata.getPF());
					lpValueList.add(lpdata);
				}
			}
			
			String[] timelist = timeList.toArray(new String[0]);
			int[] flaglist = ArrayUtils.toPrimitive(flagList.toArray(new Integer[0]));
			double[] pflist = ArrayUtils.toPrimitive(pfList.toArray(new Double[0]));
			
			LPData[] splitlplist = lpValueList.toArray(new LPData[0]);
			double[][] lpValues = new double[splitlplist[0].getCh().length][splitlplist.length];
			
	        double[] _baseValue = new double[lpValues.length];
	        _baseValue[0] = validlplist[0].getLpValue();;
	        for (int i = 1; i < lpValues.length; i++) {
	            _baseValue[i] = 0;
	        }
			
			//test debugging
			log.debug("date="+day+","+ArrayUtils.toString(timelist ,"-"));
			log.debug("date="+day+","+ArrayUtils.toString(flaglist ,"-"));
			log.debug("date="+day+","+ArrayUtils.toString(pflist ,"-"));
			log.debug("date="+day+","+ArrayUtils.toString(_baseValue ,"-"));

			for (int ch = 0; ch < lpValues.length; ch++) {
				for (int lpcnt = 0; lpcnt < lpValues[ch].length; lpcnt++) {
					lpValues[ch][lpcnt] = splitlplist[lpcnt].getCh()[ch];					
					log.debug(lpValues[ch][lpcnt]);
				}
			}

			try {
				saveLPDataUsingLPTime(MeteringType.Normal, timelist, lpValues, flaglist,
						_baseValue, parser.getMeter(), parser.getDeviceType(),
						parser.getDeviceId(), parser.getMDevType(), parser
								.getMDevId());
			}catch(Exception e) {
				log.warn(e,e);;
			}
		}

		//parser.getMeter().setLpInterval(parser.getLpInterval());	// DELETE SP-869
		return true;
	}	
	// INSERT END SP-501

	public void saveLp(IMeasurementData md, DLMSWasion parser) throws Exception {

		try {
			LPData[] lplist = parser.getLPData();

			if (lplist == null || lplist.length == 0) {
				log.warn("LP size is 0!!");
			}

			double lpSum = 0;
			double basePulse = 0;
			double current = parser.getMeteringValue() == null ? -1d : parser.getMeteringValue();

			double addBasePulse = 0;

			if (lplist != null && lplist.length > 0) {

				log.debug("lplist[0]:" + lplist[0]);
				log.debug("lplist[0].getDatetime():" + lplist[0].getDatetime());
				String inityyyymmddhh = lplist[0].getDatetime().substring(0, 10);

				SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHH");

				Calendar cal = Calendar.getInstance();

				cal.setTime(dateFormatter.parse(inityyyymmddhh));
				cal.add(cal.HOUR, 1);

				inityyyymmddhh = dateFormatter.format(cal.getTime());

				log.debug(md.getTimeStamp().substring(0, 8));
				log.debug(md.getTimeStamp().substring(8, 14));
				log.debug(parser.getMeteringValue());
				log.debug(parser.getMeter().getMdsId());
				log.debug(parser.getDeviceType());
				log.debug(parser.getDeviceId());
				log.debug(parser.getMDevType());
				log.debug(parser.getMDevId());
				log.debug(parser.getMeterTime());

				Double meteringValue = parser.getMeteringValue() == null ? 0d : parser.getMeteringValue();
				Meter meter = parser.getMeter();
				String dsttime = DateTimeUtil.getDST(null, md.getTimeStamp());
				String meterTime = parser.getMeterTime();
				log.debug("DSTTime[" + dsttime + "]");
				// if (meter.getLastReadDate() == null || dsttime.substring(0,
				// 10).compareTo(meter.getLastReadDate().substring(0, 10)) >= 0)
				// {
				if (meterTime != null && !"".equals(meterTime))
					meter.setLastReadDate(meterTime);
				else
					meter.setLastReadDate(dsttime);
				meter.setLastMeteringValue(meteringValue);
				// => INSERT START 2016.12.08 SP-303
				log.debug("DSTTime[" + dsttime + "] LASTREADDate[" + meter.getLastReadDate() + "]");
				Code normalStatus = CommonConstants.getMeterStatusByName(MeterStatus.Normal.name());
				log.debug("METER_STATUS[" + (meter.getMeterStatus() == null ? "NULL" : meter.getMeterStatus()) + "]");
				if (meter.getMeterStatus() == null
						|| (meter.getMeterStatus() != null && !meter.getMeterStatus().getName().equals("CutOff")
								&& !meter.getMeterStatus().getName().equals("Delete"))) {
					meter.setMeterStatus(normalStatus);
					log.debug("METER_CHANGED_STATUS[" + meter.getMeterStatus() + "]");
				}
				if (meterTime != null && !"".equals(meterTime)) {
					try {
						long diff = DateTimeUtil.getDateFromYYYYMMDDHHMMSS(md.getTimeStamp()).getTime()
								- DateTimeUtil.getDateFromYYYYMMDDHHMMSS(meterTime).getTime();
						meter.setTimeDiff(diff / 1000);
						log.debug("Update timeDiff. diff=[" + meter.getTimeDiff() + "]"); // INSERT
																							// SP-406
					} catch (ParseException e) {
						log.warn("Check MeterTime[" + meterTime + "] and MeteringTime[" + md.getTimeStamp() + "]");
					}
				}
				// => INSERT END 2016.12.08 SP-303

				//// UPDATE END SP-280
				for (int i = 0; i < lplist.length; i++) {

					lpSum += lplist[i].getLpValue();

					String yyyymmddhh = lplist[i].getDatetime().substring(0, 10);
					if (inityyyymmddhh.equals(yyyymmddhh)) {
						addBasePulse += lplist[i].getLpValue();
					}
					log.debug("time=" + lplist[i].getDatetime() + ":lp=" + lplist[i].getLp() + ":lpValue="
							+ lplist[i].getLpValue() + ":addBasePulse=" + addBasePulse);

				}
			}

			if (current >= 0d) {
				basePulse = current - lpSum;
			} else {
				basePulse = lpSum;
			}
			log.debug("lpSum:" + lpSum);
			log.debug("basePulse:" + basePulse);
			if (lplist == null || lplist.length == 0) {
				log.debug("LPSIZE => 0");
			} else {
				log.debug("##########LPSIZE => " + lplist.length);
				lpSave(md, lplist, parser, basePulse, addBasePulse);
			}
		} finally {
			log.debug("saveLp finish");
		}
	}

	/**
	 * 이전 savelp
	 * 
	 * @param md
	 * @param parser
	 * @throws Exception
	 */
	public void _SaveLp(IMeasurementData md, DLMSWasion parser) throws Exception {

		LPData[] lplist = parser.getLPData();
		if (lplist == null || lplist.length == 0) {
			log.warn("LP size is 0!!");
			return;
		}

		String meterDate = parser.getMeterTime();
		double lpSum = 0;
		double basePulse = 0;
		double current = parser.getMeteringValue() == null ? 0d : parser.getMeteringValue();

		double addBasePulse = 0;
		log.debug("lplist[0]:" + lplist[0]);
		log.debug("lplist[0].getDatetime():" + lplist[0].getDatetime());
		String inityyyymmddhh = lplist[0].getDatetime().substring(0, 10);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHH");

		Calendar cal = Calendar.getInstance();

		cal.setTime(dateFormatter.parse(inityyyymmddhh));
		cal.add(cal.HOUR, 1);

		inityyyymmddhh = dateFormatter.format(cal.getTime());

		log.debug("MD TimeStamp - " + md.getTimeStamp());

		log.debug("MeteringValue - " + parser.getMeteringValue());
		log.debug("MDS ID - " + parser.getMeter().getMdsId());
		log.debug("Meter Time - " + parser.getMeterTime());

		saveMeteringData(MeteringType.Normal, md.getTimeStamp().substring(0, 8), md.getTimeStamp().substring(8, 14),
				parser.getMeteringValue(), parser.getMeter(), parser.getDeviceType(), parser.getDeviceId(),
				parser.getMDevType(), parser.getMDevId(), parser.getMeterTime());

		for (int i = 0; i < lplist.length; i++) {

			lpSum += lplist[i].getLpValue(); // ignore value

			String yyyymmddhh = lplist[i].getDatetime().substring(0, 10);
			if (inityyyymmddhh.equals(yyyymmddhh)) {
				addBasePulse += lplist[i].getLpValue(); // ignore value
			}
			log.debug("time=" + lplist[i].getDatetime() + ":lp=" + lplist[i].getLp() + ":lpValue="
					+ lplist[i].getLpValue() + ":addBasePulse=" + addBasePulse);

		}

		basePulse = current - lpSum;

		if (lplist == null) {
			log.debug("LPSIZE => 0");
		} else {
			// log.debug("##########LPSIZE => "+lplist.length);
			lpSave(md, lplist, parser, basePulse, addBasePulse);
		}
	}

	public void saveAlarmLog(DLMSWasion parser) {
		try {
			List<EventLogData> alarms = parser.getMeterAlarmLog();
			if (alarms == null || alarms.size() == 0) {
				return;
			}
			saveUpdateMeterEventLog(parser.getMeter(), alarms.toArray(new EventLogData[0]));
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	private void saveRolloverEventLog(DLMSWasion parser) {
		try {
			List<EventLogData> alarms = parser.getRolloverEventAlarm();
			if (alarms == null || alarms.size() == 0) {
				return;
			}
			saveUpdateMeterEventLog(parser.getMeter(), alarms.toArray(new EventLogData[0]));
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	public void saveEventLog(DLMSWasion parser) {
		try {
			/*
			 * 미터State 값 적용
			 * 
			 * Load profile status description 0번 => Power down 1번 => Not used
			 * 2번 => Clock adjusted 3번 => Not used 4번 => Daylight saving 5번 =>
			 * Data not valid 6번 => Clock invalid 7번 => Critical error
			 * 
			 */
			Meter meter = parser.getMeter();
			if (meter.getInstallProperty() != null && meter.getInstallProperty().length() != "00000000".length()) {
				meter.setInstallProperty(null);
			}

			LPData[] lpData = parser.getLPData();
			if (lpData == null || lpData.length == 0) {
				return;
			}
			
			EventLogData e = null;
			for (LPData lpD : lpData) {
				log.debug("#### LP Status Info - [" + lpD.getDatetime() + "][OLD]["
						+ (meter.getInstallProperty() == null ? "null" : meter.getInstallProperty()) + "] ==> [NEW]["
						+ (lpD.getStatus() == null ? "null" : lpD.getStatus()) + "]");

				if (meter.getInstallProperty() == null || !meter.getInstallProperty().equals(lpD.getStatus())) {
					// 미터 이벤트 상태가 다르면 발생한다.
					List<EventLogData> events = new ArrayList<EventLogData>();
					int prevState = 0;
					int curState = 0;
					if (lpD.getStatus() != null) {
						for (int flag = 0; flag < lpD.getStatus().length(); flag++) {
							if (meter.getInstallProperty() == null)
								prevState = 0;
							else
								prevState = Integer.parseInt(meter.getInstallProperty().substring(flag, flag + 1));

							curState = Integer.parseInt(lpD.getStatus().substring(flag, flag + 1));
							if (prevState == 0 && curState == 1) {
								e = new EventLogData();
								e.setDate(lpD.getDatetime().substring(0, 8));
								e.setTime(lpD.getDatetime().substring(8));
								e.setFlag(EVENT_LOG.LPStatus.getFlag());
								e.setKind("STE");
								e.setMsg(EVENT_LOG.LPStatus.getMsg());

								switch (flag) {
								case 0: // Bit0 Critical error
									e.setAppend("[ON]" + LP_STATUS.CriticalError.getMsg());
									e.setFlag(LP_STATUS.CriticalError.getFlag());
									e.setMsg(LP_STATUS.CriticalError.getMsg());
									log.debug("[LP_STATUS][ON][" + LP_STATUS.CriticalError.getMsg() + "] "
											+ e.toString());
									break;
								case 1: // Bit1 Clock invalid
									e.setAppend("[ON]" + LP_STATUS.ClockInvalid.getMsg());
									e.setFlag(LP_STATUS.ClockInvalid.getFlag());
									e.setMsg(LP_STATUS.ClockInvalid.getMsg());
									log.debug(
											"[LP_STATUS][ON][" + LP_STATUS.ClockInvalid.getMsg() + "] " + e.toString());
									break;
								case 2: // Bit2 Data not valid
									e.setAppend("[ON]" + LP_STATUS.DataNotValid.getMsg());
									e.setFlag(LP_STATUS.DataNotValid.getFlag());
									e.setMsg(LP_STATUS.DataNotValid.getMsg());
									log.debug(
											"[LP_STATUS][ON][" + LP_STATUS.DataNotValid.getMsg() + "] " + e.toString());
									break;
								case 3: // Bit3 Daylight saving
									e.setAppend("[ON]" + LP_STATUS.DaylightSaving.getMsg());
									e.setFlag(LP_STATUS.DaylightSaving.getFlag());
									e.setMsg(LP_STATUS.DaylightSaving.getMsg());
									log.debug("[LP_STATUS][ON][" + LP_STATUS.DaylightSaving.getMsg() + "] "
											+ e.toString());
									break;
								case 4: // Not used.
									e.setAppend("[ON]" + LP_STATUS.NotUsed04.getMsg());
									e.setFlag(LP_STATUS.NotUsed04.getFlag());
									e.setMsg(LP_STATUS.NotUsed04.getMsg());
									log.debug("[LP_STATUS][ON][" + LP_STATUS.NotUsed04.getMsg() + "] " + e.toString());
									break;
								case 5: // Clock adjusted
									e.setAppend("[ON]" + LP_STATUS.ClockAdjusted.getMsg());
									e.setFlag(LP_STATUS.ClockAdjusted.getFlag());
									e.setMsg(LP_STATUS.ClockAdjusted.getMsg());
									log.debug("[LP_STATUS][ON][" + LP_STATUS.ClockAdjusted.getMsg() + "] "
											+ e.toString());
									break;
								case 6: // Not used
									e.setAppend("[ON]" + LP_STATUS.NotUsed06.getMsg());
									e.setFlag(LP_STATUS.NotUsed06.getFlag());
									e.setMsg(LP_STATUS.NotUsed06.getMsg());
									log.debug("[LP_STATUS][ON][" + LP_STATUS.NotUsed06.getMsg() + "] " + e.toString());
									break;
								case 7: // Bit7 Power down
									e.setAppend("[ON]" + LP_STATUS.PowernDown.getMsg());
									e.setFlag(LP_STATUS.PowernDown.getFlag());
									e.setMsg(LP_STATUS.PowernDown.getMsg());
									log.debug("[LP_STATUS][ON][" + LP_STATUS.PowernDown.getMsg() + "] " + e.toString());
									break;
								default:
									break;
								}

								events.add(e);
							} else if (prevState == 1 && curState == 0) {
								e = new EventLogData();
								e.setDate(lpD.getDatetime().substring(0, 8));
								e.setTime(lpD.getDatetime().substring(8));
								e.setFlag(EVENT_LOG.LPStatus.getFlag());
								e.setKind("STE");
								e.setMsg(EVENT_LOG.LPStatus.getMsg());

								switch (flag) {
								case 0: // Bit0 Critical error
									e.setAppend("[OFF]" + LP_STATUS.CriticalError.getMsg());
									e.setFlag(LP_STATUS.CriticalError.getFlag());
									e.setMsg(LP_STATUS.CriticalError.getMsg());
									log.debug("[LP_STATUS][OFF][" + LP_STATUS.CriticalError.getMsg() + "] "
											+ e.toString());
									break;
								case 1: // Bit1 Clock invalid
									e.setAppend("[OFF]" + LP_STATUS.ClockInvalid.getMsg());
									e.setFlag(LP_STATUS.ClockInvalid.getFlag());
									e.setMsg(LP_STATUS.ClockInvalid.getMsg());
									log.debug("[LP_STATUS][OFF][" + LP_STATUS.ClockInvalid.getMsg() + "] "
											+ e.toString());
									break;
								case 2: // Bit2 Data not valid
									e.setAppend("[OFF]" + LP_STATUS.DataNotValid.getMsg());
									e.setFlag(LP_STATUS.DataNotValid.getFlag());
									e.setMsg(LP_STATUS.DataNotValid.getMsg());
									log.debug("[LP_STATUS][OFF][" + LP_STATUS.DataNotValid.getMsg() + "] "
											+ e.toString());
									break;
								case 3: // Bit3 Daylight saving
									e.setAppend("[OFF]" + LP_STATUS.DaylightSaving.getMsg());
									e.setFlag(LP_STATUS.DaylightSaving.getFlag());
									e.setMsg(LP_STATUS.DaylightSaving.getMsg());
									log.debug("[LP_STATUS][OFF][" + LP_STATUS.DaylightSaving.getMsg() + "] "
											+ e.toString());
									break;
								case 4: // Not used.
									e.setAppend("[OFF]" + LP_STATUS.NotUsed04.getMsg());
									e.setFlag(LP_STATUS.NotUsed04.getFlag());
									e.setMsg(LP_STATUS.NotUsed04.getMsg());
									log.debug("[LP_STATUS][OFF][" + LP_STATUS.NotUsed04.getMsg() + "] " + e.toString());
									break;
								case 5: // Clock adjusted
									e.setAppend("[OFF]" + LP_STATUS.ClockAdjusted.getMsg());
									e.setFlag(LP_STATUS.ClockAdjusted.getFlag());
									e.setMsg(LP_STATUS.ClockAdjusted.getMsg());
									log.debug("[LP_STATUS][OFF][" + LP_STATUS.ClockAdjusted.getMsg() + "] "
											+ e.toString());
									break;
								case 6: // Not used
									e.setAppend("[OFF]" + LP_STATUS.NotUsed06.getMsg());
									e.setFlag(LP_STATUS.NotUsed06.getFlag());
									e.setMsg(LP_STATUS.NotUsed06.getMsg());
									log.debug("[LP_STATUS][OFF][" + LP_STATUS.NotUsed06.getMsg() + "] " + e.toString());
									break;
								case 7: // Bit7 Power down
									e.setAppend("[OFF]" + LP_STATUS.PowernDown.getMsg());
									e.setFlag(LP_STATUS.PowernDown.getFlag());
									e.setMsg(LP_STATUS.PowernDown.getMsg());
									log.debug(
											"[LP_STATUS][OFF][" + LP_STATUS.PowernDown.getMsg() + "] " + e.toString());
									break;
								default:
									break;
								}

								events.add(e);
							}
						}
						EventLogData[] evLogData = null;
						if (events != null && 0 < events.size()) {
							evLogData = events.toArray(new EventLogData[0]);
						}

						saveUpdateMeterEventLog(meter, evLogData);
						meter.setInstallProperty(lpD.getStatus());
					}
				}
			}

			List<Map<String, Object>> list = parser.getEventLog();
			List<EventLogData> events = new ArrayList<EventLogData>();
			for (Map<String, Object> pf : list) {
				String eventTime = "";
				String eventCode = "";
				String key = null;
				for (Iterator<String> i = pf.keySet().iterator(); i.hasNext();) {
					key = i.next();
					if (key.equals("Entry"))
						continue;
					// System.out.println("KEY STR="+key+","+pf.get(key));
					if (key.indexOf("EventTime") >= 0) {
						eventTime = (String) pf.get(key);
						key = i.next();
						if (key.indexOf("EventCode") >= 0) {
							eventCode = (String) pf.get(key);
						}
					}
					log.debug("event code=" + eventTime + "," + eventCode);

					/*
					 * Standard event log, Fraud detection Log event,
					 * Disconnector control Log 처리
					 */
					for (EVENT_LOG el : EVENT_LOG.values()) {
						if (eventCode.equals(el.getMsg())) {
							// log.debug(el.name() + "[eventTime=" + eventTime +
							// ", value=" + eventCode + "]");
							e = new EventLogData();
							String hhmmss = eventTime.substring(14) + "00";
							e.setDate(eventTime.substring(6, 14)); // :date= 제거
							e.setTime(hhmmss.substring(0, 6));
							e.setFlag(el.getFlag());
							e.setKind("STE");
							e.setMsg(el.getMsg());
							events.add(e);

							log.debug("EventSize=" + events.size() + ", added Event = [" + e.toString() + "]");

							/*
							 * // 정복전 이벤트만 식별한다. if
							 * (eventCode.contains((EVENT_LOG.PowerFailure.
							 * getMsg()))){ PowerAlarmLogData p = new
							 * PowerAlarmLogData(); p.setDate(e.getDate());
							 * p.setTime(e.getTime()); p.setFlag(e.getFlag());
							 * p.setKind(e.getKind()); p.setMsg(e.getMsg()); }
							 * else if
							 * (eventCode.contains(EVENT_LOG.PowerRestore.name()
							 * )) { PowerAlarmLogData p = new
							 * PowerAlarmLogData(); p.setCloseDate(e.getDate());
							 * p.setCloseTime(e.getTime());
							 * p.setFlag(e.getFlag()); p.setKind(e.getKind());
							 * p.setMsg(e.getMsg());
							 * 
							 * }
							 */
							break;
						}
					}

					/*
					 * Measurement event log 처리
					 */
					for (MEASUREMENT_STATUS_ALARM alarm : MEASUREMENT_STATUS_ALARM.values()) {
						if (eventCode.equals(alarm.getMsg())) {
							log.debug(alarm.name() + "[eventTime=" + eventTime + ", value=" + eventCode + "]");
							e = new EventLogData();
							String hhmmss = eventTime.substring(14) + "00";
							e.setDate(eventTime.substring(6, 14)); // :date= 제거
							e.setTime(hhmmss.substring(0, 6));
							// e.setFlag(alarm.getFlag());
							e.setFlag(EVENT_LOG.MeasurementAlarm.getFlag());
							e.setKind("STE");
							// e.setMsg(alarm.getMsg());
							e.setMsg(EVENT_LOG.MeasurementAlarm.getMsg());
							e.setAppend(alarm.getMsg());
							events.add(e);
						}
					}
				}
			}

			// List<PowerAlarmLogData> powerDowns = new
			// ArrayList<PowerAlarmLogData>();
			// List<PowerAlarmLogData> powerUps = new
			// ArrayList<PowerAlarmLogData>();

			EventLogData[] evLogData = null;
			if (events != null && 0 < events.size()) {
				evLogData = events.toArray(new EventLogData[0]);
			}

			saveUpdateMeterEventLog(parser.getMeter(), evLogData);

			// Power Down과 Up 시간을 비교하여 Down시간이 작으면 Down 시간을 Up의 DateTime에 설정한다.
			// 최근것부터 적용한다.
			/*
			 * PowerAlarmLogData powerdown = null; PowerAlarmLogData powerup =
			 * null; for (int i = powerUps.size()-1; i >= 0; i--) { powerup =
			 * powerUps.get(i); for (int j = powerDowns.size()-1; j >= 0; j--) {
			 * powerdown = powerDowns.get(j); if (powerdown.getCloseDate() ==
			 * null || "".equals(powerdown.getCloseDate())) { if
			 * ((powerdown.getDate()+powerdown.getTime()).compareTo(powerup.
			 * getCloseDate()+powerup.getCloseTime()) <= 0) {
			 * powerdown.setCloseDate(powerup.getCloseDate());
			 * powerdown.setCloseTime(powerup.getCloseTime());
			 * powerdown.setKind(powerup.getKind());
			 * powerdown.setFlag(powerup.getFlag());
			 * powerdown.setMsg(powerup.getMsg()); break; } } } }
			 * 
			 * savePowerAlarmLog(parser.getMeter(), powerDowns.toArray(new
			 * PowerAlarmLogData[0]));
			 */
		} catch (Exception e) { // 미터 이벤트 로그 생성 중 에러는 경고로 끝냄.
			log.error("Save EventLog error - " + e.getMessage(), e);
		}
	}

	private Map<String, Object> getLpMap(LPData[] lplist, double basePulse) {
		Map<String, Object> lpMap = new HashMap<String, Object>();
		double basePulseSum = 0;
		for (int i = 0; i < lplist.length; i++) {
			String lpTime = lplist[i].getDatetime();
			String yyyymmdd = lpTime.substring(0, 8);

			basePulseSum += lplist[i].getLpValue();
			if (lpMap.get(yyyymmdd) != null) {
				Vector<LPData> lpVec = (Vector<LPData>) lpMap.get(yyyymmdd);
				lpVec.add(lplist[i]);
			} else {
				Vector<LPData> lpVec = new Vector<LPData>();
				lpVec.add(lplist[i]);
				lpMap.put(yyyymmdd, lpVec);

				if (i == 0) {
					lpMap.put(yyyymmdd + "_basePulse", basePulse);
				} else {
					lpMap.put(yyyymmdd + "_basePulse", basePulse + basePulseSum);
				}

			}

		}
		return lpMap;
	}

	public void saveDayProfile(DLMSWasion parser) {

		try {
			List<BillingData> dailyProfiles = parser.getDailyBill();
			if (dailyProfiles == null || dailyProfiles.isEmpty()) {
				return;
			}
			log.debug("DailyBilling Data List size = " + dailyProfiles.size());
						
			for (BillingData dailyProfile : dailyProfiles) {
				saveUpdateDailyBilling(dailyProfile, parser.getMeter(), null, null, parser.getMDevType(),
						parser.getMDevId());
			}
		} catch (Exception e) {
			log.error("Daily Profile Saving error - " + e, e);
		}
	}

	public void saveMonthProfile(DLMSWasion parser) {

		try {
			List<BillingData> monthProfiles = parser.getMonthlyBill();
			if (monthProfiles == null || monthProfiles.isEmpty()) {
				return;
			}
			log.debug("MonthlyBilling Data List size = " + monthProfiles.size());

			for (BillingData monthProfile : monthProfiles) {
				saveMonthlyBilling(monthProfile, parser.getMeter(), null, null, parser.getMDevType(),
						parser.getMDevId());
			}
		} catch (Exception e) {
			log.error("Monthly Profile Saving error - " + e, e);
		}
	}

	private Double retValue(String mm, Double value_00, Double value_15, Double value_30, Double value_45) {

		Double retVal_00 = value_00 == null ? 0d : value_00;
		Double retVal_15 = value_15 == null ? 0d : value_15;
		Double retVal_30 = value_30 == null ? 0d : value_30;
		Double retVal_45 = value_45 == null ? 0d : value_45;
		if ("15".equals(mm)) {
			return retVal_00;
		}

		if ("30".equals(mm)) {
			return retVal_00 + retVal_15;
		}

		if ("45".equals(mm)) {
			return retVal_00 + retVal_15 + retVal_30;
		}

		if ("00".equals(mm)) {
			return retVal_00 + retVal_15 + retVal_30 + retVal_45;
		}

		return retVal_00 + retVal_15;

	}

	private void saveMeterInfomation(DLMSWasion parser) {
		try {				// INSERT SP-486
			String fwVer = parser.getFwVersion();
			String meterModel = parser.getMeterModel();
			Integer lpInterval = parser.getLpInterval();	
			
			//=> UPDATE START 2016.10.11 SP-282
			//eMeter = (EnergyMeter)parser.getMeter();
			EnergyMeter eMeter = null;
			switch (MeterType.valueOf(parser.getMeter().getMeterType().getName())) {
			case EnergyMeter:
				eMeter = (EnergyMeter)parser.getMeter();
				break;
			}
			if ( eMeter == null )
			{
				return;
			}
			//=> UPDATE END  2016.10.11 SP-282
			boolean updateflg = false;
			Integer swSta = 0;
	
			if (eMeter == null ) {
				log.debug("MDevId[" + parser.getMDevId() + "] MeterDataParser is null.");
				return;
			}
			
			// FW Version 
			if ( fwVer.length() != 0 ) {
				if (eMeter.getSwVersion() == null ||
						(eMeter.getSwVersion() != null && !eMeter.getSwVersion().equals(fwVer))) {
					eMeter.setSwVersion(fwVer);
					log.debug("MDevId[" + parser.getMDevId() + "] set Swversion[" + fwVer + "]");
					updateflg = true;
				}
			}
			
			// Meter Model 
			if ( meterModel != null && meterModel.length() != 0 && !eMeter.getModel().getName().equals(meterModel)) {
				List<DeviceModel> list = deviceModelDao.getDeviceModelByName(eMeter.getSupplierId(),meterModel );
				if (list != null && list.size() == 1) {
	                eMeter.setModel(list.get(0));
	                log.debug("[HSW] HHH setModel:"+list.get(0));
					log.debug("MDevId[" + parser.getMDevId() + "] set Model[" + meterModel + "]");
	                updateflg = true;
	            }
			}
			
			// SwitchStatus
			if ((eMeter.getSwitchActivateStatus() != null && swSta != null) && eMeter.getSwitchActivateStatus() != swSta) {
				eMeter.setSwitchActivateStatus(swSta);
				log.debug("MDevId[" + parser.getMDevId() + "] set SwitchActivateStatus[" + swSta + "]");
				updateflg = true;
			}
			
			// LpInterval
			// UPDATE START SP-869
			if ((eMeter.getLpInterval() != null && lpInterval != null) && eMeter.getLpInterval() != lpInterval) {
				eMeter.setLpInterval(lpInterval);
				log.debug("MDevId[" + parser.getMDevId() + "] set setLpInterval[" + lpInterval + "]");
				updateflg = true;
			}

			if ( updateflg ) {
				meterDao.update(eMeter);
				meterDao.flush();
				log.debug("MDevId[" + parser.getMDevId() + "] update meter information");
			}
		// INSERT START SP-486
		} catch (Exception e) {
			log.error(e,e);
		} finally {
			log.debug("MDevId[" + parser.getMDevId() + "] saveMeterInfomation finish");
		}		
		// INSERT END SP-486
	}
	
	private boolean lpSave(IMeasurementData md, LPData[] validlplist, DLMSWasion parser, double base,
			double addBasePulse) throws Exception {
		String yyyymmdd = validlplist[0].getDatetime().substring(0, 8);
		String yyyymmddhh = validlplist[0].getDatetime().substring(0, 10);
		String hhmm = validlplist[0].getDatetime().substring(8, 12);
		double basePulse = validlplist[0].getLpValue();

		log.info("#########save base value:" + basePulse + ":mdevId:" + parser.getMDevId() + ":yyyymmddhh:"
				+ yyyymmddhh);
		double[][] lpValues = new double[validlplist[0].getCh().length][validlplist.length];
		int[] flaglist = new int[validlplist.length];
		double[] pflist = new double[validlplist.length];

		for (int ch = 0; ch < lpValues.length; ch++) {
			for (int lpcnt = 0; lpcnt < lpValues[ch].length; lpcnt++) {
				// lpValues[ch][lpcnt] = validlplist[lpcnt].getLpValue();
				// Kw/h 단위로 저장
				lpValues[ch][lpcnt] = validlplist[lpcnt].getCh()[ch];
			}
		}

		for (int i = 0; i < flaglist.length; i++) {
			flaglist[i] = validlplist[i].getFlag();
			pflist[i] = validlplist[i].getPF();
		}

		parser.getMeter().setLpInterval(parser.getLpInterval());
		// TODO Flag, PF 처리해야 함.
		saveLPData(MeteringType.Normal, yyyymmdd, hhmm, lpValues, flaglist, basePulse, parser.getMeter(),
				parser.getDeviceType(), parser.getDeviceId(), parser.getMDevType(), parser.getMDevId());
		return true;
	}

	private boolean _lpSave(IMeasurementData md, LPData[] validlplist, DLMSWasion parser, double base,
			double addBasePulse) throws Exception {

		String yyyymmdd = validlplist[0].getDatetime().substring(0, 8);
		String hhmm = validlplist[0].getDatetime().substring(8, 12);

		double[][] lpValues = new double[validlplist[0].getCh().length][validlplist.length];
		int[] flaglist = new int[validlplist.length];
		double[] pflist = new double[validlplist.length];

		for (int ch = 0; ch < lpValues.length; ch++) {
			for (int lpcnt = 0; lpcnt < lpValues[ch].length; lpcnt++) {
				// lpValues[ch][lpcnt] = validlplist[lpcnt].getLpValue();
				// Kw/h 단위로 저장
				lpValues[ch][lpcnt] = validlplist[lpcnt].getCh()[ch];
			}
		}

		for (int i = 0; i < flaglist.length; i++) {
			flaglist[i] = validlplist[i].getFlag();
			pflist[i] = validlplist[i].getPF();
		}

		// double[] _baseValue = new double[validlplist.length];
		double[] _baseValue = new double[lpValues.length]; // lpValues
		_baseValue[0] = base;
		for (int i = 1; i < _baseValue.length; i++) {
			_baseValue[i] = 0;
		}

		parser.getMeter().setLpInterval(parser.getLpInterval());
		log.debug("LP Interval Saving - MeterModel=" + parser.getMeterModel() + ", MeterId=" + parser.getMeterID()
				+ ", LPInterval=" + parser.getLpInterval());

		// TODO Flag, PF 처리해야 함.
		saveLPData(MeteringType.Normal, yyyymmdd, hhmm, lpValues, flaglist, _baseValue, parser.getMeter(),
				parser.getDeviceType(), parser.getDeviceId(), parser.getMDevType(), parser.getMDevId());
		return true;
	}

	@Override
	public String relayValveOn(String mcuId, String meterId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		TransactionStatus txstatus = null;
		try {
			txstatus = txmanager.getTransaction(null);
			Meter meter = meterDao.get(meterId);

			txmanager.commit(txstatus);

			CommandGW commandGw = DataUtil.getBean(CommandGW.class);

			if(meter.getModel() != null && meter.getModel().getName().equals("NRAM-3405CT10")) {

				Map<String, String> ret = commandGw.cmdGeneralNiCommand(
						String.valueOf(meter.getModem().getId()), 
						CommandType.Set.name(), 
						Hex.decode(NIAttributeId.CircuitBreaker.getCode()), 
						CBControl.On.getHexId());

				log.info("cmdResult: " + ret.get("cmdResult"));		
				if(ret != null && ret.get("cmdResult") != null && ret.get("cmdResult").equals("Execution OK")) {
					
					if(ret.containsKey("AttributeData")){

						JSONObject jo = JSONObject.fromObject(ret.get("AttributeData"));
						log.info(String.valueOf(jo.get("Value")));
						//succ(1: fail, 0: succ)
						String status = String.valueOf(jo.get("Value"));
						 if(status.indexOf("CBControl-Success") >= 0  || status.indexOf(CBControlResult.Success.getHexId()) >= 0) {
							ret.put("LoadControlStatus", LOAD_CONTROL_STATUS.CLOSE.name());
						}else {
							ret.put("failReason", "Circuit breaker on command fail");
						}
						
						resultMap.put("Response", ret);

					}else{
			            resultMap.put("Result", ResultStatus.FAIL);
					}
				}else {
		            resultMap.put("Result", ResultStatus.FAIL);
				}
			}else {
				resultMap = commandGw.cmdMeterRelay(mcuId, meterId, OnDemandOption.WRITE_OPTION_RELAYON.getCode());
			}

		} catch (Exception e) {
			if (txstatus != null && !txstatus.isCompleted())
				txmanager.rollback(txstatus);
            resultMap.put("failReason", e.getMessage());
            resultMap.put("Result", ResultStatus.FAIL);
		}

		return MapToJSON(resultMap);
	}

	@Override
	public String relayValveOff(String mcuId, String meterId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		TransactionStatus txstatus = null;
		try {
			txstatus = txmanager.getTransaction(null);
			Meter meter = meterDao.get(meterId);

			txmanager.commit(txstatus);

			CommandGW commandGw = DataUtil.getBean(CommandGW.class);

			if(meter.getModel() != null && meter.getModel().getName().equals("NRAM-3405CT10")) {
				Map<String, String> ret = commandGw.cmdGeneralNiCommand(
						String.valueOf(meter.getModem().getId()), 
						CommandType.Set.name(), 
						Hex.decode(NIAttributeId.CircuitBreaker.getCode()), 
						CBControl.Off.getHexId());
				
				log.info("cmdResult: " + ret.get("cmdResult"));		
				if(ret != null && ret.get("cmdResult") != null && ret.get("cmdResult").equals("Execution OK")) {
					
					if(ret.containsKey("AttributeData")){

						JSONObject jo = JSONObject.fromObject(ret.get("AttributeData"));
						log.info(String.valueOf(jo.get("Value")));
						//succ(1: fail, 0: succ)
						String status = String.valueOf(jo.get("Value"));
						 if(status.indexOf("CBControl-Success") >= 0 || status.indexOf(CBControlResult.Success.getHexId()) >= 0) {
							ret.put("LoadControlStatus", LOAD_CONTROL_STATUS.OPEN.name());
						}else {
							ret.put("failReason", "Circuit breaker off command fail");
						}
						
						resultMap.put("Response", ret);

					}else{
			            resultMap.put("Result", ResultStatus.FAIL);
					}
				}else {
		            resultMap.put("Result", ResultStatus.FAIL);
				}
			}else {
				resultMap = commandGw.cmdMeterRelay(mcuId, meterId, OnDemandOption.WRITE_OPTION_RELAYOFF.getCode());
			}

		} catch (Exception e) {
			if (txstatus != null && !txstatus.isCompleted())
				txmanager.rollback(txstatus);
            resultMap.put("failReason", e.getMessage());
            resultMap.put("Result", ResultStatus.FAIL);
		}

		return MapToJSON(resultMap);
	}

	@Override
	public String relayValveStatus(String mcuId, String meterId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		TransactionStatus txstatus = null;
		try {
			txstatus = txmanager.getTransaction(null);
			Meter meter = meterDao.get(meterId);

			txmanager.commit(txstatus);

			CommandGW commandGw = DataUtil.getBean(CommandGW.class);

			int nOption = OnDemandOption.READ_OPTION_RELAY.getCode(); // read
																		// table
			
			if(meter.getModel() != null && meter.getModel().getName().equals("NRAM-3405CT10")) {
				Map<String, String> ret = commandGw.cmdGeneralNiCommand(
						String.valueOf(meter.getModem().getId()), 
						CommandType.Get.name(), 
						Hex.decode(NIAttributeId.CircuitBreaker.getCode()), null);

			
				log.info("cmdResult: " + ret.get("cmdResult"));		
				if(ret != null && ret.get("cmdResult") != null && ret.get("cmdResult").equals("Execution OK")) {
					
					if(ret.containsKey("AttributeData")){

						JSONObject jo = JSONObject.fromObject(ret.get("AttributeData"));
						log.info(String.valueOf(jo.get("Value")));
						//CB Status(1: ON, 0: OFF)
						String status = String.valueOf(jo.get("Value"));
						if(status.indexOf("CBStatus-ON") >= 0 || status.indexOf(CBStatus.On.getHexId()) >= 0) {
							ret.put("LoadControlStatus", LOAD_CONTROL_STATUS.CLOSE.name());
						}else if(status.indexOf("CBStatus-OFF") >= 0 || status.indexOf(CBStatus.Off.getHexId()) >= 0) {
							ret.put("LoadControlStatus", LOAD_CONTROL_STATUS.OPEN.name());
						}else {
							ret.put("failReason", "Get circuit breaker status command fail");
						}
						
						resultMap.put("Response", ret);

					}else{
			            resultMap.put("Result", ResultStatus.FAIL);
					}
				}else {
		            resultMap.put("Result", ResultStatus.FAIL);
				}

			}else {
				resultMap = commandGw.cmdMeterRelay(mcuId, meterId, nOption);
			}

		} catch (Exception e) {
			if (txstatus != null && !txstatus.isCompleted())
				txmanager.rollback(txstatus);
			log.error(e, e);
            resultMap.put("failReason", e.getMessage());
            resultMap.put("Result", ResultStatus.FAIL);
		}

		return MapToJSON(resultMap);
	}

    // INSERT START SP-279
    public String getTravelTime(String meterId) {
        Meter meter = meterDao.get(meterId);
        Modem modem = meter.getModem();

        String travelTime = "0";
        if (modem.getModemType() == ModemType.MMIU) {
        	travelTime = FMPProperty.getProperty("soria.meter.synctime.traveltime.mmiu", "0");
        } else if (modem.getModemType() == ModemType.IEIU) {
        	travelTime = FMPProperty.getProperty("soria.meter.synctime.traveltime.ieiu", "0");
        } else if (modem.getModemType() == ModemType.SubGiga) {
        	travelTime = FMPProperty.getProperty("soria.meter.synctime.traveltime.subgiga", "0");
        } else {
        	travelTime = "0";
        }
        
        return travelTime;
    }
    // INSERT END SP-279
    
    @Override
    public String syncTime(String mcuId, String meterId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        int result = 0;
        try {
            Meter meter = meterDao.get(meterId);
            Modem modem = meter.getModem();
            String travelTime = "0";
            if (modem.getModemType() == ModemType.MMIU) {
            	travelTime = FMPProperty.getProperty("soria.meter.synctime.traveltime.mmiu", "0");
            } else if (modem.getModemType() == ModemType.IEIU) {
            	travelTime = FMPProperty.getProperty("soria.meter.synctime.traveltime.ieiu", "0");
            } else if (modem.getModemType() == ModemType.SubGiga) {
            	travelTime = FMPProperty.getProperty("soria.meter.synctime.traveltime.subgiga", "0");
            } else {
            	travelTime = "0";
            }
            
    		log.debug("syncTime() MeterID[" + meter.getMdsId() + "] ModemType[" + modem.getModemType().name() + 
    				"] diff[" + meter.getTimeDiff() + "] travel[" + travelTime + "]");
    		            
			String obisCode = this.convertObis(OBIS.CLOCK.getCode());
			int classId = DLMS_CLASS.CLOCK.getClazz();
			int attrId = DLMS_CLASS_ATTR.CLOCK_ATTR02.getAttr();
			String accessRight = "RW";
			String dataType = "octet-string";
            String param = obisCode+"|"+classId+"|"+attrId+"|"+accessRight+"|"+dataType+"|"+travelTime;

            CommandGW commandGw = DataUtil.getBean(CommandGW.class);                        
            
            if((modem.getModemType() == ModemType.MMIU) && (modem.getProtocolType() == Protocol.SMS)) {
            	// MBB
            	// for AutoTimeSync Task
       			String cmd = "cmdSyncTime";
				String rtnString="";
       			Map<String, String> paramMap = new HashMap<String, String>();
    			//paramSet
    			paramMap.put("paramSet", param);        			
    			paramMap.put("meterId", meter.getMdsId());
				paramMap.put("option", "synctime");

        		MMIU mmiuModem = mmiuDao.get(modem.getId());           				
   				List<String> paramListForSMS  = new ArrayList<String>();
   				Properties prop = new Properties();

   				//smpp.hes.fep.server=10.40.200.46
   				//soria.modem.tls.port=8900*
   				//smpp.auth.port=9001
   		        paramListForSMS.add(FMPProperty.getProperty("smpp.hes.fep.server", ""));
   		        paramListForSMS.add(FMPProperty.getProperty("soria.modem.tls.port", ""));
   		        paramListForSMS.add(FMPProperty.getProperty("smpp.auth.port", ""));
           		        
   		        String cmdMap = null;
   		        ObjectMapper om = new ObjectMapper();
   		        if(paramMap != null)
   		        	cmdMap = om.writeValueAsString(paramMap);
           		                  		    	
   		    	rtnString = commandGw.sendSMS(
   		    			cmd, 
   		    			SMSConstants.MESSAGE_TYPE.REQ_NON_ACK.getTypeCode(), 
   		    			mmiuModem.getPhoneNumber(), 
   		    			modem.getDeviceSerial(),
   		    			SMSConstants.COMMAND_TYPE.NI.getTypeCode(), 
   		    			paramListForSMS, cmdMap);
            }
            else {            
	            // if synctime option, value param is travel time.
	            resultMap = commandGw.cmdMeterParamSetWithOption(modem.getDeviceSerial(), meterId, param, "synctime");                       
	            if(resultMap != null) {
	            	log.debug("MDevId[" + meterId + "] resultMap[" + resultMap.toString() + "]");
	
	            	String afterTime = resultMap.get("aftertime").toString();
	            	if (afterTime != null) {
		        		String beforeTime = DateTimeUtil.getDateString(TimeUtil.getLongTime(afterTime) - (meter.getTimeDiff()*1000));
		
		            	String diff = String.valueOf((TimeUtil.getLongTime(afterTime) - TimeUtil.getLongTime(beforeTime))/1000);
		                resultMap.put("diff", diff);
		                
		                if (resultMap.get("RESULT_VALUE") == null || resultMap.get("RESULT_VALUE").toString() != "Success") {
		                	result = 1;
		                }
		                saveMeterTimeSyncLog(meter, beforeTime, afterTime, result);
	            	}
	            }
            }
        }
        catch (Exception e) {
            result = 1;
            resultMap.put("failReason", e.getMessage());
        }
        return MapToJSON(resultMap);
    }

	private List<LPData> checkEmptyLP(int lpInterval, List<LPData> list) throws Exception {
		ArrayList<LPData> emptylist = new ArrayList<LPData>();
		List<LPData> totalList = list;
		int channelCount = 4;
		if (list != null && list.size() > 0) {
			channelCount = list.get(0).getCh().length;
		}
		Double[] ch = new Double[channelCount];

		for (int i = 0; i < channelCount; i++) {
			ch[i] = new Double(0.0);
		}

		String prevTime = "";
		String currentTime = "";

		Iterator<LPData> it = totalList.iterator();
		while (it.hasNext()) {

			LPData prev = (LPData) it.next();
			currentTime = prev.getDatetime();

			currentTime = Util.getQuaterYymmddhhmm(currentTime, lpInterval);

			if (prevTime != null && !prevTime.equals("")) {

				String temp = Util.addMinYymmdd(prevTime, lpInterval);
				if (!temp.equals(currentTime)) {

					int diffMin = (int) ((Util.getMilliTimes(currentTime + "00") - Util.getMilliTimes(prevTime + "00"))
							/ 1000 / 60) - lpInterval;

					if (diffMin > 0 && diffMin <= 1440) { // 하루이상 차이가 나지 않을때
															// 빈값으로 채운다.
						for (int i = 0; i < (diffMin / lpInterval); i++) {
							log.debug("empty lp temp : " + currentTime + ", diff Min=" + diffMin);
							LPData data = new LPData();
							data.setCh(ch);
							data.setStatus("Power down");
							data.setFlag(MeteringFlag.Missing.getFlag());
							data.setPF(1.0);
							data.setDatetime(Util.addMinYymmdd(prevTime, lpInterval * (i + 1)));
							emptylist.add(data);
						}
					}

				}
			}
			prevTime = currentTime;

		}

		Iterator<LPData> it2 = emptylist.iterator();
		while (it2.hasNext()) {
			totalList.add((LPData) it2.next());
		}

		Collections.sort(totalList, LPComparator.TIMESTAMP_ORDER);
		totalList = checkDupLP(lpInterval, totalList);

		return totalList;
	}

	private List<LPData> checkDupLP(int lpInterval, List<LPData> list) throws Exception {

		log.debug("###########  org list size = " + list.size());

		List<LPData> totalList = new LinkedList<LPData>(list);
		List<LPData> removeList = new LinkedList<LPData>();
		Collections.copy(totalList, list);

		log.debug("###########  org total list size = " + totalList.size());

		LPData prevLPData = null;

		for (LPData lpData : totalList) {

			if (prevLPData != null && prevLPData.getDatetime() != null && !prevLPData.getDatetime().equals("")) {
				if (lpData.getDatetime().equals(prevLPData.getDatetime())
						&& !lpData.getStatus().contains("Clock adjusted")
						&& !lpData.getStatus().contains("Daylight saving")) {
					log.debug("time equls:" + lpData.getDatetime());
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

	public void saveCurrBill(DLMSWasion parser) {

		try {
			BillingData bill = parser.getCurrBill();
			if (bill != null && parser.getMeterTime() != null && !"".equals(parser.getMeterTime())) {
				log.debug("saveCurrBill : " + parser.getMDevId() + " BillingTimestamp : " + parser.getMeterTime());
				bill.setBillingTimestamp(parser.getMeterTime());
				saveCurrentBilling(bill, parser.getMeter(), null, null, parser.getMDevType(), parser.getMDevId());
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	/**
	 * saveCurrBillLog - overflow event log
	 * 
	 * @param parser
	 */
	private void saveCurrBillLog(DLMSWasion parser) {

		try {
			List<BillingData> billLog = parser.getCurrBillLog();
			if (billLog == null || billLog.size() == 0) {
				return;
			}
			
			for (BillingData bill : billLog) {
				if (bill != null && parser.getMeterTime() != null && !"".equals(parser.getMeterTime())) {
					log.debug("saveCurrBillLog : " + parser.getMDevId());
					saveCurrentBilling(bill, parser.getMeter(), null, null, parser.getMDevType(), parser.getMDevId());
				}
			}

		} catch (Exception e) {
			log.error(e, e);
		}
	}

	private void savePowerQuality(DLMSWasion parser) {
		try {

			Instrument[] instrument = parser.getPowerQuality();
			if(instrument != null && instrument.length > 0) {
				savePowerQuality(parser.getMeter(), parser.getMeteringTime(), instrument, parser.getDeviceType(),
						parser.getDeviceId(), parser.getMDevType(), parser.getMDevId());
			}

		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	public MeterData onDemandMeterBypass(String mcuId, String meterId,String modemId, String nOption, String fromDate, String toDate)
            throws Exception
	{
		log.info("mcuId[" + mcuId + "] meterId[" + meterId+"] modemId[" + modemId+"] nOption[" + nOption + "] fromDate[" + fromDate + "] toDate[" + toDate +"]");
        MeterData mdata = new MeterData();
		
        TransactionStatus txstatus = null;
        int modemPort = 0;
        
        Map<String,Object> result = null;
		try {
            txstatus = txmanager.getTransaction(null);
			Meter meter = meterDao.get(meterId);
			if (meter == null)
				return null;
			if ( meter.getModemPort() != null ){
				modemPort = meter.getModemPort().intValue();
			}
			if ( modemPort > 5){
				throw new Exception("ModemPort:" + modemPort + " is not Support");
			}
            txmanager.commit(txstatus);	
        }
        catch (Exception e) {
        	log.debug(e,e);
        	if (txstatus != null) txmanager.rollback(txstatus);
        	throw e;
        } 
		
		try {				                           
			CommandGW commandGw = DataUtil.getBean(CommandGW.class);
	        if ( modemPort == 0 )
	        	result = (Map<String,Object>)commandGw.cmdGetLoadProfile(modemId, meterId, fromDate, toDate);
	        else 
	        	throw new Exception("modemPort:"+modemPort +" is not Support");
	        
	        //Get Load Profile Rawdata
	        byte[] rawdata = (byte[])result.get("lprawdata");
	     	log.debug("MDevId[" + meterId + "] LoadProfile Data =" + Hex.decode(rawdata));
	     	
	     	txstatus = txmanager.getTransaction(null);	
	     	Meter meter = meterDao.get(meterId);
	     	Modem modem = modemDao.get(modemId);
	     	
	     	DLMSWasion mdp = new DLMSWasion();
			mdp.setModemId(modemId);
			mdp.setMeter(meter);
			mdp.setMeteringTime(DateTimeUtil.getDateString(new Date()));
			mdp.setMeterTime(mdp.getMeteringTime());
			mdp.parse(rawdata);
			
			mdata.setMeterId(meterId);
			mdata.setParser(mdp);
			
			commandGw.saveMeteringDataByQueue(mcuId, meterId, modemId, rawdata);
	        
			BigDecimal lastValue = null;
			if ( modemPort == 0 ){
				MeterData.Map meterMap = mdata.getMap();
				MeterData.Map.Entry entry ;

				// INSERT START SP-369
		        DecimalFormat decimalf=null;		         
		        if(meter!=null && meter.getSupplier()!=null){
		            Supplier supplier = meter.getSupplier();
		            if(supplier !=null){
		                String lang = supplier.getLang().getCode_2letter();
		                String country = supplier.getCountry().getCode_2letter();
		                
		                decimalf = TimeLocaleUtil.getDecimalFormat(supplier);
		            }
		        }else{
		            //locail, If no information is to use the default format.
		            decimalf = new DecimalFormat();
		        }				
		        // INSERT END SP-369
				
				OBIS cumulatives[] =  new OBIS [4];
				cumulatives[0] = OBIS.CUMULATIVE_ACTIVEENERGY_IMPORT;
				cumulatives[1] = OBIS.CUMULATIVE_ACTIVEENERGY_EXPORT;
				cumulatives[2] = OBIS.CUMULATIVE_REACTIVEENERGY_IMPORT;
				cumulatives[3] = OBIS.CUMULATIVE_REACTIVEENERGY_EXPORT; 
				for ( int i = 0; i < cumulatives.length; i++ ){
					if ( result.get(cumulatives[i].name()) != null && 
							result.get(cumulatives[i].name()+ "_UNIT") != null){
						long value = (long)result.get(cumulatives[i].name());
						Map<String, Object> scaleunit = (Map<String, Object>)result.get(cumulatives[i].name()+ "_UNIT");
						UNIT unit = UNIT.getItem((int)scaleunit.get("unit"));
						int scaler = (int)scaleunit.get("scaler");
						BigDecimal dcValue = new BigDecimal( new BigInteger(String.valueOf(value)), -scaler);
						// INSERT START SP-369
						BigDecimal multiplicand = new BigDecimal(new BigInteger("1"), 3);
						dcValue = dcValue.multiply(multiplicand);	
						// INSERT END SP-369
						log.debug("MDevId[" + meterId + "] OBIS=" + cumulatives[i].name() +",value=" + value + ",unit=" + unit.getName() + ",scaler=" + scaler + ",dcValue=" + dcValue.toString());

						entry= new MeterData.Map.Entry();
						entry.setKey(cumulatives[i].getName());
						// UPDATE START SP-369
				        //entry.setValue(dcValue.toPlainString() + " " + unit.getName());
						String newUnit = unit.getName().replace("[", "[k");
						entry.setValue(decimalf.format(dcValue.doubleValue()) + " " + newUnit);
						// UPDATE END SP-369
				        meterMap.getEntry().add(entry);
				        if (cumulatives[i] == OBIS.CUMULATIVE_ACTIVEENERGY_IMPORT ||
				        		cumulatives[i] == OBIS.CUMULATIVE_ACTIVEENERGY_EXPORT){
				        	if ( dcValue.compareTo(new BigDecimal(0)) > 0 ) {
				        		lastValue = dcValue;
				        	}
				        }
					}
				}
	
		        if( lastValue != null ){
		        	meter.setLastMeteringValue((double)lastValue.doubleValue());
		        }
				meter.setLastReadDate(DateTimeUtil.getDateString(new Date()));
				meterDao.update(meter);
			}
			
			if(TimeLocaleUtil.isThisDateValid(meter.getLastReadDate(), "yyyyMMddHHmmss")){
				modem.setLastLinkTime(meter.getLastReadDate());
			}else{
				modem.setLastLinkTime(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
			}
			modemDao.update(modem);			
            txmanager.commit(txstatus);	
		}catch(Exception e) {
			log.debug(e,e);
			if (txstatus != null) txmanager.rollback(txstatus);
			throw e;
		}
		
		return mdata;
	}
	
    public void saveSTSLog(DLMSWasion parser) throws Exception
    {
        TransactionStatus txstatus = null;
        try {
            txstatus = txmanager.getTransaction(null);
            String currentTime = TimeUtil.getCurrentTimeMilli();
            
            if(parser.getStsLogs() == null || parser.getStsLogs().size() < 1) {
            	return;
            }

            Meter meter = parser.getMeter();
            if (meter != null) {
                
                if(meter.getContract() != null) {

                	Contract contract = meter.getContract();
                	
                	for(STSLog stsLog : parser.getStsLogs()) {
                    	PrepaymentLog pLog = new PrepaymentLog();
                		pLog.setId(Long.parseLong(Integer.toString(contract.getId())
                                + Long.toString(System.currentTimeMillis())));
                    	pLog.setContract(contract);
                    	pLog.setCustomer(meter.getCustomer());
                    	Operator operator = operatorDao.getOperatorByLoginId("admin");
                    	pLog.setOperator(operator);
                    	pLog.setLocation(contract.getLocation());
                    	pLog.setLastTokenDate(stsLog.getTokenDate());
                    	pLog.setBalance(stsLog.getRemainCredit());
                    	pLog.setChargedCredit(stsLog.getLastCredit());
                    	pLog.setLastTokenId(stsLog.getTokenTid());
                    	pLog.setToken(stsLog.getTokenNumber());

                    	Set<Condition> condition = new HashSet<Condition>();
                        condition.add(new Condition("contract.id", new Object[] {contract.getId()}, null, Restriction.EQ));
        				condition.add(new Condition("lastTokenDate", new Object[] { stsLog.getTokenDate() }, null, Restriction.EQ));
                    	
                    	List<PrepaymentLog> logs = pLogDao.findByConditions(condition);
                    	if(logs == null || logs.size() < 1) {
                            pLogDao.add(pLog);
                    	}
                        
                		log.info("save pLog");
                	}

            		//contract.setCurrentCredit(credit);
            		// OPF-253 contract의 is_sts flag를 true로 바꿔준다.
            		//contract.setIsSts(true);            		
                	//contractDao.update(contract);
                }
            }
        }
        catch (Exception e) {
        	log.error(e,e);
            throw e;
        }
        finally {
            if (txstatus != null) txmanager.commit(txstatus);
        }
    }
	
	
    /**
     * Hex ObisCode => x.x.x.x.x.x 
     * 
     * @param obisCode
     * @return
     */
    private String convertObis(String obisCode) {
    	String returnData = "";
    	if(obisCode.length() == 12) {
    		byte[] obisCodeArr = Hex.encode(obisCode);
    		obisCode="";
    		for (int i = 0; i < obisCodeArr.length; i++) {
    			if(i == 0) {
    				obisCode += DataUtil.getIntToByte(obisCodeArr[i]);
    			} else {
    				obisCode += "."+DataUtil.getIntToByte(obisCodeArr[i]);
    			}
			}
    		returnData = obisCode;
    	} else {
    		returnData = "Wrong Obis";
    	}
    	
    	return returnData;
    }

}
