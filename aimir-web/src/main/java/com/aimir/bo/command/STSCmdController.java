package com.aimir.bo.command;

import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.aimir.bo.common.CommandProperty;
import com.aimir.constants.CommonConstants.CmdResultType;
import com.aimir.constants.CommonConstants.McuType;
import com.aimir.constants.CommonConstants.ModemType;
import com.aimir.constants.CommonConstants.OperatorType;
import com.aimir.constants.CommonConstants.Protocol;
import com.aimir.constants.CommonConstants.ResultStatus;
import com.aimir.constants.CommonConstants.TR_OPTION;
import com.aimir.constants.CommonConstants.TR_STATE;
import com.aimir.dao.system.EcgSTSLogDao;
import com.aimir.dao.system.ObisCodeDao;
import com.aimir.dao.system.SupplierDao;
import com.aimir.fep.command.mbean.CommandGW;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.DLMS_CLASS;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.DLMS_CLASS_ATTR;
import com.aimir.fep.meter.parser.DLMSECGTable.DLMSVARIABLE.OBIS;
import com.aimir.fep.protocol.nip.frame.GeneralFrame.CommandType;
import com.aimir.fep.util.TunnelListInfo;
import com.aimir.fep.util.sms.SendSMS;
import com.aimir.model.device.AsyncCommandLog;
import com.aimir.model.device.AsyncCommandParam;
import com.aimir.model.device.AsyncCommandResult;
import com.aimir.model.device.MMIU;
import com.aimir.model.device.Meter;
import com.aimir.model.device.Modem;
import com.aimir.model.system.Code;
import com.aimir.model.system.Contract;
import com.aimir.model.system.DeviceModel;
import com.aimir.model.system.EcgSTSLog;
import com.aimir.model.system.EcgSTSLogPk;
import com.aimir.model.system.Operator;
import com.aimir.model.system.PrepaymentLog;
import com.aimir.model.system.Supplier;
import com.aimir.schedule.command.CmdOperationUtil;
import com.aimir.service.device.AsyncCommandLogManager;
import com.aimir.service.device.MeterManager;
import com.aimir.service.device.ModemManager;
import com.aimir.service.system.CodeManager;
import com.aimir.service.system.ContractManager;
import com.aimir.service.system.DeviceModelManager;
import com.aimir.service.system.OperatorManager;
import com.aimir.service.system.PrepaymentLogManager;
import com.aimir.util.DateTimeUtil;
import com.aimir.util.FileUtils;
import com.aimir.util.STSToken;
import com.aimir.util.StringUtil;
import com.aimir.util.TimeLocaleUtil;
import com.aimir.util.TimeUtil;

import net.sf.json.JSONArray;

@Service(value = "stsCmdController")
@Controller
public class STSCmdController<V> {

	private static Log log = LogFactory.getLog(STSCmdController.class);

	@Resource(name = "transactionManager")
	HibernateTransactionManager transactionManager;

	@Autowired
	MeterManager meterManager;

	@Autowired
	ModemManager modemManager;

	@Autowired
	DeviceModelManager modelManager;

	@Autowired
	SupplierDao supplierDao;

	@Autowired
	AsyncCommandLogManager asyncCommandLogManager;

	@Autowired
	EcgSTSLogDao stslogDao;
	
	@Autowired
	CmdOperationUtil cmdOperationUtil;

	@Autowired
	ObisCodeDao obisCodeManager;
	
	@Autowired
    PrepaymentLogManager prepaymentLogManager;
	
	@Autowired
	OperatorManager operatorManager;
	
	@Autowired
	ContractManager contractManager;
	
	@Autowired
	CodeManager codeManager;
	
	final static ApplicationContext appCtx = ContextLoader.getCurrentWebApplicationContext();
	final static String cmdZigbeeSTS = "ZigBeeSTS_";
	final static String meterUART1 = "01"; // DLMS
	final static String meterUART2 = "02"; // UIU

	/**
	 * Get/Set Bypass Meter Interface
	 * 
	 **/
	public Map<String, Object> cmdExecNiCommand(String meterId, String requestType, String attrID, String attrParam) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		ResultStatus status = ResultStatus.FAIL;
		String rtnStr = "";

		if (meterId == null || "".equals(meterId)) {
			resultMap.put("status", status);
			resultMap.put("rtnStr", "FAIL : Meter ID null!");
			return resultMap;
		}

		Meter meter = meterManager.getMeter(Integer.parseInt(meterId));
		String meterSerial = meter.getMdsId();
		if (meter == null || "".equals(meter.getId()) || meter.getModemId() == null) {
			resultMap.put("status", status);
			resultMap.put("rtnStr", "Meter or Modem is not in the system!");
			return resultMap;
		}

		String modemId = Integer.toString(meter.getModemId());
		if (modemId == null || "".equals(modemId)) {
			status = ResultStatus.INVALID_PARAMETER;
			rtnStr = "Target ID(modem id) is null!";

			resultMap.put("status", status);
			resultMap.put("rtnStr", rtnStr);
			return resultMap;
		}

		if (attrID != null && attrID.startsWith("0x")) {
			attrID = attrID.replace("0x", "");
		}

		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = cmdOperationUtil.cmdGeneralNiCommand(modemId, requestType, attrID, StringUtil.nullToBlank(attrParam));
			rtnStr = result.get("rtnStr").toString();
			status = ResultStatus.SUCCESS;
		} catch (Exception e) {
			log.error(e, e);
			status = ResultStatus.FAIL;
			rtnStr = e.getMessage();
			resultMap.put("rtnStr", rtnStr);
		}

		for (String key : result.keySet()) {
			resultMap.put(key, result.get(key).toString());
		}

		resultMap.put("status", status.name());
		resultMap.put("rtnStr", rtnStr);
		resultMap.put("modemId", modemId);
		resultMap.put("meterSerial", meterSerial);
		
		return resultMap;
	}

	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetPaymentMode")
	public ModelAndView cmdGetPaymentMode(String target, String mode) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdGetPaymentMode";
		
		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");
		
		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("result", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}
		
		CommandGW cgw = new CommandGW();
		String param = "";
		int classId = DLMS_CLASS.DATA.getClazz();
		int attrId = DLMS_CLASS_ATTR.REGISTER_ATTR02.getAttr();
		String requestType = CommandType.Get.name().toUpperCase();
		String value = "";
		String obisCode = "";
		
		if (mode.equals("0")) {
			obisCode = cgw.convertObis(OBIS.ACTIVE_STS_PAYMENTMODE_SETTING.getCode());
			param = obisCode + "|" + classId + "|" + attrId + "|RW|bit-string|" + value;
		} else if (mode.equals("1")) {
			obisCode = cgw.convertObis(OBIS.PASSIVE_STS_PAYMENTMODE_SETTING.getCode());
			param = obisCode + "|" + classId + "|" + attrId + "|RW|bit-string|" + value + ",";

			obisCode = cgw.convertObis(OBIS.PASSIVE_STS_PAYMENTMODE_SWITCH_TIME.getCode());
			param += obisCode + "|" + classId + "|" + attrId + "|||" + value;
		}

		log.debug("param: " + param);
		
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(), requestType, param);
			log.debug("map: " + map);
			// map: {RESULT_VALUE=Success, RESULT_STEP=GET_STS_SWITCH_TIME, value=[{"paymentMode":"04","paymentSwitchTime":"{date=65535/255/255(), datetime=65535255255255255255, daylight=true, time=255:255:255}"}]}
			
			if (map.get("RESULT_VALUE").equals("Success")) {
				String jsonStr = map.get("value").toString();
				JSONArray jsonArr = JSONArray.fromObject(jsonStr);
				Map jsonMap = (Map<String, Object>) jsonArr.toArray()[0];
				String paymentMode = jsonMap.get("paymentMode").toString();
				String result = "";

				if (mode.equals("1")) { // Passive
					String temp = "";
					String[] filter_word = { "\\[", "\\]", "\\{", "\\}" };
					String paymentSwitchTime = jsonMap.get("paymentSwitchTime").toString();
					
					for (int i = 0; i < filter_word.length; i++) {
						temp = paymentSwitchTime.replaceAll(filter_word[i], "");
						paymentSwitchTime = temp;
					}
					
					// meter에 setting된 passive time이 이미 적용된 값인지 확인한다
					// Example) paymentSwitchTime: {date=65535/255/255(), datetime=65535255255255255255, daylight=true, time=255:255:255}
					String[] modeParams = paymentSwitchTime.replace(" ", "").split(",");
					if (modeParams[0].contains("255")) {
						if (paymentMode.equals("04")) {
							result += "<b>[Passive] Payment Mode: </b> Prepay<br/>";
							result += "<b>Switch Time: </b> Already applied.";
							
							mav.addObject("result", result);
						} else if (paymentMode.equals("08")) {
							result += "<b>[Passive] Payment Mode: </b> Postpay<br/>";
							result += "<b>Switch Time: </b> Already applied.";
							
							mav.addObject("result", result);
						}
						
						paramMap.put("switchTime", "");
					} else {
						if (paymentMode.equals("04")) {
							result += "<b>[Passive] Payment Mode: </b> Prepay<br/>";
							result += "<b>Switch Time: </b>" + paymentSwitchTime;
							
							mav.addObject("result", result);
						} else if (paymentMode.equals("08")) {
							result += "<b>[Passive] Payment Mode: </b> Postpay<br/>";
							result += "<b>Switch Time: </b>" + paymentSwitchTime;
							
							mav.addObject("result", result);
						}
						paramMap.put("switchTime", paymentSwitchTime);
					}
				} else if (mode.equals("0")){ // Active
					if (paymentMode.equals("04")) {
						result += "<b>[Active] Payment Mode: </b> Prepay";
						mav.addObject("result", result);
					} else if (paymentMode.equals("08")) {
						result = "<b>[Active] Payment Mode: </b> Postpay";
						mav.addObject("result", result);
					}
					
					paramMap.put("switchTime", "");
				}
				
				paramMap.put("requestType", requestType);
				paramMap.put("meterId", meter.getMdsId());
				paramMap.put("paymentMode", paymentMode);

				saveLog(cmd, paramMap);
				updatePaymodeInfo(meter.getMdsId(), paymentMode);
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;
	}

	/**
	 * cmdSetPaymentMode Payment 타입.
	 * 
	 * 
	 * @param target
	 * @param mode
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetPaymentMode")
	public ModelAndView cmdSetPaymentMode(String target, String mode, String yyyymmdd) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdSetPaymentMode";
		
		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");
		
		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("result", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}
		
		CommandGW cgw = new CommandGW();
		String param = "";
		int classId = DLMS_CLASS.DATA.getClazz();
		int attrId = DLMS_CLASS_ATTR.REGISTER_ATTR02.getAttr();
		String requestType = CommandType.Set.name().toUpperCase();
		String obisCode = "";
		
		obisCode = cgw.convertObis(OBIS.PASSIVE_STS_PAYMENTMODE_SETTING.getCode());
		param = obisCode + "|" + classId + "|" + attrId + "|RW|bit-string|" + mode + ",";

		obisCode = cgw.convertObis(OBIS.PASSIVE_STS_PAYMENTMODE_SWITCH_TIME.getCode());
		param += obisCode + "|" + classId + "|" + attrId + "|||" + yyyymmdd + "000000";
		
		log.debug("param: " + param);
		
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(), requestType, param);
			log.debug("map: " + map);
			
			if (map.get("RESULT_VALUE").equals("Success")) {
				mav.addObject("result", "SUCCESS");
				
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("requestType", requestType);
				paramMap.put("meterId", meter.getMdsId());
				paramMap.put("paymentMode", mode);
				paramMap.put("switchTime", yyyymmdd);

				saveLog(cmd, paramMap);
				updatePaymodeInfo(meter.getMdsId(), mode);
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;
	}

	/**
	 * cmdGetRemainingCredit 잔액정보를 가져옴 (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetRemainingCredit")
	public ModelAndView cmdGetRemainingCredit(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdGetRemainingCredit";
		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");

		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("rtnStr", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}

		CommandGW cgw = new CommandGW();
		String param = "";
		int classId = DLMS_CLASS.REGISTER.getClazz();
		int attrId = DLMS_CLASS_ATTR.REGISTER_ATTR02.getAttr();
		String value = "";
		String obisCode = cgw.convertObis(OBIS.REMAINING_CREDIT.getCode());
		String requestType = CommandType.Get.name().toUpperCase();

		log.debug("(" + cmd + ")obisCode => " + obisCode + ", classId => " + classId + ", attributeId => " + attrId);
		param = obisCode + "|" + classId + "|" + attrId + "|RO|UINT32|" + value;

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(), requestType, param);

			if (map.get("RESULT_VALUE").equals("Success")) {
				mav.addObject("result", "Remaining Credit: " + Integer.parseInt(map.get("value").toString()) * 0.01 + " cedi");

				Map<String, Object> paramMap = new HashMap<String, Object>();
				double remainingCredit = (double) (Integer.parseInt(map.get("value").toString()) * 0.01);
				paramMap.put("remainingCredit", remainingCredit);
				paramMap.put("meterId", meter.getMdsId());
				paramMap.put("requestType", requestType);

				saveLog(cmd, paramMap);

				// update remaining credit
				updateCurrentCredit(meter.getMdsId(), DateTimeUtil.getDateString(new Date()), Integer.parseInt(map.get("value").toString()) * 0.01);
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;

	}

	/**
	 * cmdSetInitialCredit InitialCredit default 값으로 set. (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetInitialCredit")
	public ModelAndView cmdSetInitialCredit(String target) {

		ModelAndView mav = new ModelAndView("jsonView");
		final String OID = "244.0.0";
		String cmd = "cmdSetSTSToken";
		String userCmd = "SetInitialCredit";
		try {

			// validate 체크
			Map<String, Object> validateMap = commonSTSValidate(target);
			Boolean validateCheck = (Boolean) validateMap.get("result");
			if (!validateCheck) {
				mav.addObject("rtnStr", validateMap.get("errorMessage"));
				return mav;
			}

			String deviceSerial = (String) validateMap.get("deviceSerial");

			Meter meter = (Meter) validateMap.get("meter");
			// Credit 기준으로 작성
			String meterTypeCode = meter.getMeterType().getCode();
			String subClass = "4"; // default 4
			if ("1.3.1.1".equals(meterTypeCode)) {
				subClass = "4";
			} else if ("1.3.1.2".equals(meterTypeCode)) {
				subClass = "5";
			}

			// STS Number가 IhdId컬럼에 저장되어 있다
			String DSN = "";
			if (meter.getIhdId() != null && (meter.getIhdId().length() == 8 || meter.getIhdId().length() == 11)) {
				DSN = meter.getIhdId();
			} else {
				mav.addObject("rtnStr", "FAIL : Cannot find STS Number.");
				return mav;
			}

			Properties prop = new Properties();
			prop.load(getClass().getClassLoader().getResourceAsStream("command.properties"));
			String stsBaseUrl = prop.getProperty("sts.baseUrl");
			String SGC = prop.getProperty("sts.sgcNumber") == null ? "" : prop.getProperty("sts.sgcNumber");
			Double credit = prop.getProperty("prepay.init.credit") == null ? null
					: Double.parseDouble(prop.getProperty("prepay.init.credit"));

			// Prepay Initical Credit 및 STS접속 속성이 설정되지 않으면 에러 메시지 리턴
			if (credit == null || stsBaseUrl == null || SGC == null) {
				mav.addObject("rtnStr", "FAIL : Options for Initial Credit are not loaded. ");
				return mav;
			}

			String TCT = "02";
			String EA = "07";
			String tokenDate = DateTimeUtil.getDateString(new Date());
			String DOE = STSToken.getDOE(tokenDate);
			String MfrCode = "96";
			String TI = "01";
			Contract contract = meter.getContract();
			String KRN = "1";
			String idRecord = STSToken.getIdRecord(TCT, EA, SGC, KRN, DSN, DOE, MfrCode, TI);

			// value format : A decimal integer greater than 0.
			// base currency units : 0.01, R50.01 금액 충전시 value는 5001로 넣어야한다.
			Double currencyUnits = 0.01;
			Double chargedFormatCredit = Double.parseDouble(String.format("%.2f", credit));

			Double value = chargedFormatCredit / currencyUnits;
			String tokenValue = String.valueOf(Math.round(value));

			String token = "";
			String sendURL = "";

			if (stsBaseUrl != null && !"".equals(stsBaseUrl)) {
				sendURL = stsBaseUrl.trim() + "VendCredit.ini?";
				sendURL += "meterId=" + idRecord + "&subclass=" + subClass + "&value=" + tokenValue;
				token = STSToken.makeToken(sendURL);
			}

			if (token != null && !token.isEmpty()) {
				// 비동기 내역 저장
				List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
				Map<String, String> paramMap = new HashMap<String, String>();
				// paramMap.put("token", token);
				paramMap.put("string", token);
				paramList.add(paramMap);

				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, paramList, DateTimeUtil.getDateString(new Date()));

				String mdsId = meter.getMdsId();
				String createDate = DateTimeUtil.getDateString(new Date());
				if (mdsId != null) {
					/*
					 * createEcgStsLog(mdsId, cmd, createDate, null, tokenDate,
					 * token, null, null, null, null, null, null, new int[]{},
					 * new double[]{}, new double[]{}, new double[]{}, new
					 * double[]{}, null, null, 100, "", trId,
					 * DateTimeUtil.getDateString(new Date()), null, null, null,
					 * null, null, null, null, null, null, null, 0, null, null,
					 * null, null, null, null, null);
					 */
					createEcgStsLog(mdsId, userCmd, createDate, null, tokenDate, token, null, null, null, null, null,
							null, new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {},
							null, null, 100, "Waiting", trId, DateTimeUtil.getDateString(new Date()), null, null, null,
							null, null, null, null, null, null, null, 0, null, null, null, null, null, null, null);
				}

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 상태가 바뀌는 시간을 기다려주기 위해 1분 sleep
					Thread.sleep(60000);
					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success: Command Result[" + cmd + "]");
						mav.addObject("rtnStr", "Done: Command Result[" + cmd + "]");
						EcgSTSLogPk stsPk = new EcgSTSLogPk();
						stsPk.setAsyncTrId(trId);
						stsPk.setCmd(cmd);
						stsPk.setMeterNumber(mdsId);
						stsPk.setCreateDate(createDate);
						EcgSTSLog stsLog = stslogDao.get(stsPk);
						int rtnMode = stsLog.getResult();
						String resultMsg = "Done:Send SMS Command(" + cmd + ").\n";
						Supplier supplier = supplierDao.get(contract.getSupplierId());

						String resultDate = stsLog.getResultDate() == null ? null
								: TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(stsLog.getResultDate()),
										supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter());
						if (rtnMode == 0) {
							resultMsg += "Result : SUCCESS\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						} else if (rtnMode == 1) {
							String failReason = stsLog.getFailReason() == null ? "" : stsLog.getFailReason();
							resultMsg += "Result : FAIL\n";
							resultMsg += "Result Date : " + resultDate + "\n";
							resultMsg += "Fail Reason : " + failReason + "\n";
						} else if (rtnMode == 100) {
							resultMsg += "Result : NOT RESPONSE\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						} else {
							resultMsg += "Result : UnKnown\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						}

						mav.addObject("rtnMsg", resultMsg);
					}
				}
			} else {
				mav.addObject("rtnStr", "FAIL : Cannot make token.");
			}

		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
			return mav;
		}

		return mav;
	}

	/**
	 * cmdSetInitialArrears InitialCredit을 입력한 Arrears금액만큼 생성/전송. (STS 미터용)
	 * 
	 * @param target
	 *            미터 IntegerId
	 * @param initArrears
	 *            금액 DoubleValue
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetInitialCredit")
	public ModelAndView cmdSetInitialArrears(String target, String initArrears) {

		ModelAndView mav = new ModelAndView("jsonView");
		final String OID = "244.0.0";
		String cmd = "cmdSetSTSToken";
		String userCmd = "SetInitialCredit";
		try {

			// validate 체크
			Map<String, Object> validateMap = commonSTSValidate(target);
			Boolean validateCheck = (Boolean) validateMap.get("result");
			if (!validateCheck) {
				mav.addObject("rtnStr", validateMap.get("errorMessage"));
				return mav;
			}

			String deviceSerial = (String) validateMap.get("deviceSerial");

			Meter meter = (Meter) validateMap.get("meter");
			// Credit 기준으로 작성
			String meterTypeCode = meter.getMeterType().getCode();
			String subClass = "4"; // default 4
			if ("1.3.1.1".equals(meterTypeCode)) {
				subClass = "4";
			} else if ("1.3.1.2".equals(meterTypeCode)) {
				subClass = "5";
			}

			// STS Number가 Ihdid컬럼에 저장되어 있다.
			String DSN = "";
			if (meter.getIhdId() != null && (meter.getIhdId().length() == 8 || meter.getIhdId().length() == 11)) {
				DSN = meter.getIhdId();
			} else {
				mav.addObject("rtnStr", "FAIL : Cannot find STS Number.");
				return mav;
			}

			Properties prop = new Properties();
			prop.load(getClass().getClassLoader().getResourceAsStream("command.properties"));
			String stsBaseUrl = prop.getProperty("sts.baseUrl");
			String SGC = prop.getProperty("sts.sgcNumber") == null ? "" : prop.getProperty("sts.sgcNumber");
			Double credit = Double.parseDouble(initArrears);

			// Credit이 정상적으로 설정되었는지 확인함.
			if (credit == null || credit < 0) {
				mav.addObject("rtnStr", "FAIL : Invalid amount of arrears. ");
				return mav;
			}
			// Prepay Initical Credit 및 STS접속 속성이 설정되지 않으면 에러 메시지 리턴
			if (credit == null || stsBaseUrl == null || SGC == null) {
				mav.addObject("rtnStr", "FAIL : Options for STS property are not loaded. ");
				return mav;
			}

			String TCT = "02";
			String EA = "07";
			String tokenDate = DateTimeUtil.getDateString(new Date());
			String DOE = STSToken.getDOE(tokenDate);
			String MfrCode = "96";
			String TI = "01";
			Contract contract = meter.getContract();
			String KRN = "1";
			String idRecord = STSToken.getIdRecord(TCT, EA, SGC, KRN, DSN, DOE, MfrCode, TI);

			// value format : A decimal integer greater than 0.
			// base currency units : 0.01, R50.01 금액 충전시 value는 5001로 넣어야한다.
			Double currencyUnits = 0.01;
			Double chargedFormatCredit = Double.parseDouble(String.format("%.2f", credit));

			Double value = chargedFormatCredit / currencyUnits;
			String tokenValue = String.valueOf(Math.round(value));

			String token = "";
			String sendURL = "";

			if (stsBaseUrl != null && !"".equals(stsBaseUrl)) {
				sendURL = stsBaseUrl.trim() + "VendCredit.ini?";
				sendURL += "meterId=" + idRecord + "&subclass=" + subClass + "&value=" + tokenValue;
				token = STSToken.makeToken(sendURL);
			}

			if (token != null && !token.isEmpty()) {
				// 비동기 내역 저장
				List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
				Map<String, String> paramMap = new HashMap<String, String>();
				// paramMap.put("token", token);
				paramMap.put("string", token);
				paramList.add(paramMap);

				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, paramList, DateTimeUtil.getDateString(new Date()));

				String mdsId = meter.getMdsId();
				String createDate = DateTimeUtil.getDateString(new Date());
				if (mdsId != null) {
					createEcgStsLog(mdsId, userCmd, createDate, null, tokenDate, token, null, null, null, null, null,
							null, new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {},
							null, null, 100, "Waiting", trId, DateTimeUtil.getDateString(new Date()), null, null, null,
							null, null, null, null, null, null, null, 0, null, null, null, null, null, null, null);
				}

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, TCP Connection 맺음.
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							messageId = "tcpSkip";
						} else {
							messageId = "tcpSuccess";
						}
					} else {
						log.info("No use TCP Trigger");
						messageId = "tcpSkip";
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("tcpSkip".equals(messageId)) {
					mav.addObject("rtnStr", "SUCCESS : But fail to transfer the token to the meter.");
					log.debug("Token is OK, But fail to create TCP socket");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 상태가 바뀌는 시간을 기다려주기 위해 1분 sleep
					Thread.sleep(60000);
					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", "FAIL : Communication Error(" + cmd + "), No access from modem.");
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success: Command Result[" + cmd + "]");
						mav.addObject("rtnStr", "Done: Command Result[" + cmd + "]");
						EcgSTSLogPk stsPk = new EcgSTSLogPk();
						stsPk.setAsyncTrId(trId);
						stsPk.setCmd(cmd);
						stsPk.setMeterNumber(mdsId);
						stsPk.setCreateDate(createDate);
						EcgSTSLog stsLog = stslogDao.get(stsPk);
						int rtnMode = stsLog.getResult();
						String resultMsg = "Done:Send SMS Command(" + cmd + ").\n";
						// Supplier supplier =
						// supplierDao.get(contract.getSupplierId());
						Supplier supplier = supplierDao.get(meter.getSupplierId());

						String resultDate = stsLog.getResultDate() == null ? null
								: TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(stsLog.getResultDate()),
										supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter());
						if (rtnMode == 0) {
							resultMsg += "Result : SUCCESS\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						} else if (rtnMode == 1) {
							String failReason = stsLog.getFailReason() == null ? "" : stsLog.getFailReason();
							resultMsg += "Result : FAIL\n";
							resultMsg += "Result Date : " + resultDate + "\n";
							resultMsg += "Fail Reason : " + failReason + "\n";
						} else if (rtnMode == 100) {
							resultMsg += "Result : NOT RESPONSE\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						} else {
							resultMsg += "Result : UnKnown\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						}

						mav.addObject("rtnMsg", resultMsg);
					}
				}
			} else {
				mav.addObject("rtnStr", "FAIL : Cannot make token.");
			}

		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("rtnStr", "ERROR : " + userCmd + " Fail. ");
			return mav;
		}

		return mav;
	}

	/**
	 * 토큰번호로 Set. (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetSTSToken")
	public ModelAndView cmdSetSTSToken(String target, String token, Boolean isClear) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdSetSTSToken";

		if (target == null || "".equals(target)) {
			mav.addObject("rtnStr", "FAIL : Meter ID null!");
			return mav;
		}

		Meter meter = meterManager.getMeter(Integer.parseInt(target));
		if (meter == null || "".equals(meter.getId()) || meter.getModemId() == null) {
			mav.addObject("rtnStr", "FAIL : Meter or Modem is not in the system!");
			return mav;
		}

		CommandGW cgw = new CommandGW();
		String param = "";
		int classId = DLMS_CLASS.TOKEN_GATEWAY.getClazz();
		int attrId = DLMS_CLASS_ATTR.TOKEN_ATTR01.getAttr();
		String value = token;
		String obisCode = cgw.convertObis(OBIS.TOKEN_GATEWAY.getCode());
		String requestType = "ACT";

		log.debug("(" + "cmd" + ")obisCode => " + obisCode + ", classId => " + classId + ", attributeId => " + attrId);
		param = obisCode + "|" + classId + "|" + attrId + "|RW|octet-string|" + value;

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial(), meter.getMdsId(), requestType, param);
			log.info("result: " + map);

			if (map.get("RESULT_VALUE").equals("Success")) {
				mav.addObject("result", "SUCCESS");
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		// Save Log (S)
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("requestType", requestType);
			paramMap.put("token", token);
			paramMap.put("meterId", meter.getMdsId());
			
			saveLog(cmd, paramMap);
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : " + e);
		}
		// Save Log (E)
		
		return mav;
	}

	/**
	 * cmdGetSTSToken 토큰 정보를 가져옴. (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @param tCount
	 *            (1~5 숫자만 가능)
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetSTSToken")
	public ModelAndView cmdGetSTSToken(String target, String tCount) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdGetSTSToken";

		if (target == null || "".equals(target)) {
			mav.addObject("result", "FAIL : Meter ID null!");
			return mav;
		}

		Meter meter = meterManager.getMeter(Integer.parseInt(target));
		if (meter == null || "".equals(meter.getId()) || meter.getModemId() == null) {
			mav.addObject("result", "FAIL : Meter or Modem is not in the system!");
			return mav;
		}

		CommandGW cgw = new CommandGW();
		String param = "";
		int classId = DLMS_CLASS.PROFILE_GENERIC.getClazz();
		int attrId = DLMS_CLASS_ATTR.TOKEN_ATTR02.getAttr();
		String value = tCount;
		String obisCode = cgw.convertObis(OBIS.TOKEN_CREDIT_HISTORY.getCode());
		String requestType = CommandType.Get.name().toUpperCase();

		log.debug("(" + "cmd" + ")obisCode => " + obisCode + ", classId => " + classId + ", attributeId => " + attrId);
		param = obisCode + "|" + classId + "|" + attrId + "|RW|octet-string|" + value;

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial(), meter.getMdsId(),
					requestType, param);
			log.info("result: " + map);

			if (map.get("RESULT_VALUE").equals("Success")) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				String jsonStr = map.get("value").toString();
				JSONArray jsonArr = JSONArray.fromObject(jsonStr);

				String result = "";
				String rechargeDate = "";
				String token = "";
				String rechargeAmount = "";

				for (int i = 0; i < jsonArr.size(); i++) {
					Map jsonMap = (Map<String, Object>) jsonArr.toArray()[i];

					rechargeDate = jsonMap.get("CLOCK").toString();
					rechargeDate = TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(rechargeDate));
					rechargeAmount = jsonMap.get("LAST_PURCHASE_CREDIT_TARIFF").toString();

					result += "<b>Recharge Date: </b>" + rechargeDate + "<br/>";
					result += "<b>Token Number: </b>" + jsonMap.get("LAST_INPUT_TOKEN").toString() + "<br/>";
					result += "<b>Recharge Amount: </b>" + rechargeAmount + " cedi" + "<br/><br/>";

					if (i == jsonArr.size() - 1) {
						token += jsonMap.get("LAST_INPUT_TOKEN").toString();
					} else {
						token += jsonMap.get("LAST_INPUT_TOKEN").toString() + ", ";
					}
				}

				mav.addObject("result", result);

				paramMap.put("requestType", requestType);
				paramMap.put("token", token);
				paramMap.put("meterId", meter.getMdsId());

				saveLog(cmd, paramMap);
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;
	}

	/**
	 * cmdSetTariff (STS 미터용) - Group Command
	 * 
	 * 
	 * @param target
	 * @param yyyymmdd
	 * @param condLimit1
	 * @param condLimit2
	 * @param param
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetTariff")
	public ModelAndView cmdSetTariff(Integer supplierId, String target, Integer tariffType, String yyyymmdd,
			String condLimit1, String condLimit2, String param) {

		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdSetTariff";
		JSONArray jsonArr = null;
		List<Map<String, Object>> targetList = new ArrayList<Map<String, Object>>();
		List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
		Map<String, Object> validateMap = null;
		int[] consArr = null;
		double[] fixedRateArr = null;
		double[] varRateArr = null;
		double[] condRate1Arr = null;
		double[] condRate2Arr = null;

		try {
			if (param == null || param.isEmpty()) {
				jsonArr = new JSONArray();
			} else {
				jsonArr = JSONArray.fromObject(param);
			}

			List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
			Map<String, String> dataMap = new HashMap<String, String>();
			if (jsonArr.size() > 0) {
				Object[] tempJson = jsonArr.toArray();
				for (int i = 0; i < tempJson.length; i++) {
					JSONArray jsonArr2 = (JSONArray) tempJson[i];
					Map<String, Object> jsonMap = (Map<String, Object>) jsonArr2.get(0);
					dataMap = new HashMap<String, String>();
					String cons = jsonMap.get("cons").toString();
					String fixedRate = jsonMap.get("fixedRate").toString();
					String varRate = jsonMap.get("varRate").toString();
					String condRate1 = jsonMap.get("condRate1").toString();
					String condRate2 = jsonMap.get("condRate2").toString();
					dataMap.put("cons", cons);
					dataMap.put("fixedRate", fixedRate);
					dataMap.put("varRate", varRate);
					dataMap.put("condRate1", condRate1);
					dataMap.put("condRate2", condRate2);
					dataList.add(dataMap);
				}
			}

			if (target == null || "".equals(target)) {
				String modelName = "OmniPower STS";
				List<DeviceModel> model = modelManager.getDeviceModelByName(supplierId, modelName);
				if (model.size() > 0) {
					// 미터 모델이 OmniPower STS이고, mmiu 타입이고 폰넘버가 존재하고 프로토콜 타이이
					// 0102이고, 프로토콜이 sms이고 미터가 존재하는 모뎀.
					Map<String, Object> condition = new HashMap<String, Object>();
					condition.put("protocolType", Protocol.SMS.name());
					condition.put("modemType", ModemType.MMIU.name());
					condition.put("modelName", "OmniPower STS");
					condition.put("meterTypeCode", "1.3.1.1");
					condition.put("protocolVersion", "0102");
					condition.put("tariffIndexId", tariffType);
					targetList = modemManager.getSTSModem(condition);
					if (targetList.size() > 0) {
						target = targetList.get(0).get("METERID").toString();
					}
				}

				validateMap = commonSTSValidate(target);
			} else {
				validateMap = commonSTSValidate(target);

				Meter meter = (Meter) validateMap.get("meter");
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("DEVICESERIAL", (String) validateMap.get("deviceSerial"));
				map.put("MDSID", meter.getMdsId());
				map.put("PHONENUMBER", (String) validateMap.get("mobileNo"));
				targetList.add(map);
			}

			// validate 체크
			Boolean validateCheck = (Boolean) validateMap.get("result");
			if (!validateCheck) {
				mav.addObject("rtnStr", "FAIL : " + validateMap.get("errorMessage"));
				return mav;
			}

			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("string", yyyymmdd);
			paramList.add(paramMap);
			paramMap = new HashMap<String, String>();
			paramMap.put("string", condLimit1);
			paramList.add(paramMap);
			paramMap = new HashMap<String, String>();
			paramMap.put("string", condLimit2);
			paramList.add(paramMap);

			String cons = "";
			String fixedRate = "";
			String varRate = "";
			String condRate1 = "";
			String condRate2 = "";
			int tariffSize = dataList.size();
			consArr = new int[tariffSize];
			fixedRateArr = new double[tariffSize];
			varRateArr = new double[tariffSize];
			condRate1Arr = new double[tariffSize];
			condRate2Arr = new double[tariffSize];
			for (int i = 0; i < tariffSize; i++) {
				Map<String, String> map = dataList.get(i);
				if (i == 0) {
					cons = map.get("cons").toString();
					fixedRate = map.get("fixedRate").toString();
					varRate = map.get("varRate").toString();
					condRate1 = map.get("condRate1").toString();
					condRate2 = map.get("condRate2").toString();
				} else {
					cons += "," + map.get("cons").toString();
					fixedRate += "," + map.get("fixedRate").toString();
					varRate += "," + map.get("varRate").toString();
					condRate1 += "," + map.get("condRate1").toString();
					condRate2 += "," + map.get("condRate2").toString();
				}
				consArr[i] = Integer.parseInt(map.get("cons").toString());
				fixedRateArr[i] = Double.parseDouble(map.get("fixedRate").toString());
				varRateArr[i] = Double.parseDouble(map.get("varRate").toString());
				condRate1Arr[i] = Double.parseDouble(map.get("condRate1").toString());
				condRate2Arr[i] = Double.parseDouble(map.get("condRate2").toString());
			}
			paramMap = new HashMap<String, String>();
			paramMap.put("int", cons);
			paramList.add(paramMap);
			paramMap = new HashMap<String, String>();
			paramMap.put("double", fixedRate);
			paramList.add(paramMap);
			paramMap = new HashMap<String, String>();
			paramMap.put("double", varRate);
			paramList.add(paramMap);
			paramMap = new HashMap<String, String>();
			paramMap.put("double", condRate1);
			paramList.add(paramMap);
			paramMap = new HashMap<String, String>();
			paramMap.put("double", condRate2);
			paramList.add(paramMap);
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("rtnStr", "FAIL : Unknown.(" + e.getMessage() + ")");
			return mav;
		}

		try {
			String messageId = null;
			String deviceSerial = null;
			long trId = 0L;
			String mdsId = null;
			String createDate = null;

			for (int i = 0; i < targetList.size(); i++) {
				try {
					Map<String, Object> targetMap = targetList.get(i);
					deviceSerial = (String) targetMap.get("DEVICESERIAL");
					mdsId = (String) targetMap.get("MDSID");

					trId = System.currentTimeMillis();
					saveAsyncCommandList(deviceSerial, trId, cmd, paramList, TimeUtil.getCurrentTime());
					// ECGSTSLog 저장
					createDate = DateTimeUtil.getDateString(new Date());
					if (mdsId != null) {
						createEcgStsLog(mdsId, cmd, createDate, null, null, null, null, null, consArr.length, yyyymmdd,
								condLimit1, condLimit2, consArr, fixedRateArr, varRateArr, condRate1Arr, condRate2Arr,
								null, null, 100, null, trId, DateTimeUtil.getDateString(new Date()), null, null, null,
								null, null, null, null, null, null, null, null, null, null, null, null, null, null,
								null);
					}

					// SMS 전송
					String smsSkip = (String) validateMap.get("smsSkip");
					if ("true".equals(smsSkip) || smsSkip == "true") {
						// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
						messageId = "smsSkip";
						boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
								: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
						if (triggerUse == true) {
							Modem modem = modemManager.getModem(deviceSerial);
							if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
								mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
								return mav;
							}
						} else {
							log.info("No use TCP Trigger");
						}
					} else {
						messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
								(String) targetMap.get("PHONENUMBER"), (String) validateMap.get("smsMsg"),
								(Properties) validateMap.get("prop"));
					}

				} catch (Exception e) {
					log.error(e, e);
					continue;
				}
			}

			// 상태가 바뀌는 시간을 기다려주기 위해 1분 sleep
			Thread.sleep(60000);

			if (targetList.size() == 1) {
				try {
					if ("fail".equals(messageId)) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Request CMD Fail");
					} else if ("smsSkip".equals(messageId)) {
						mav.addObject("rtnStr",
								"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
						log.debug("Save AsyncCommandLog");
					} else {
						mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
						log.debug("Request CMD Success");

						Integer lastStatus = null;

						lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

						if (TR_STATE.Success.getCode() != lastStatus) {
							mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
							log.debug("Fail : Command Result[" + cmd + "]");
						} else {
							log.debug("Success: Command Result[" + cmd + "]");
							mav.addObject("rtnStr", "Done: Command Result[" + cmd + "]");
							EcgSTSLogPk stsPk = new EcgSTSLogPk();
							stsPk.setAsyncTrId(trId);
							stsPk.setCmd(cmd);
							stsPk.setMeterNumber(mdsId);
							stsPk.setCreateDate(createDate);
							EcgSTSLog stsLog = stslogDao.get(stsPk);

							Supplier supplier = supplierDao.get(supplierId);
							// 1:실패발샏
							int rtnMode = stsLog.getResult();
							String resultMsg = "Done:Send SMS Command(" + cmd + ").\n";

							String resultDate = stsLog.getResultDate() == null ? null
									: TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(stsLog.getResultDate()),
											supplier.getLang().getCode_2letter(),
											supplier.getCountry().getCode_2letter());

							if (rtnMode == 0) {
								resultMsg += "Result : SUCCESS\n";
								resultMsg += "Result Date : " + resultDate + "\n";
							} else if (rtnMode == 1) {
								String failReason = stsLog.getFailReason() == null ? "" : stsLog.getFailReason();
								resultMsg += "Result : FAIL\n";
								resultMsg += "Result Date : " + resultDate + "\n";
								resultMsg += "Fail Reason : " + failReason + "\n";
							} else if (rtnMode == 100) {
								resultMsg += "Result : NOT RESPONSE\n";
								resultMsg += "Result Date : " + resultDate + "\n";
							} else {
								resultMsg += "Result : Unknown\n";
								resultMsg += "Result Date : " + resultDate + "\n";
							}

							mav.addObject("rtnMsg", resultMsg);
						}
					}
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
		}

		return mav;
	}

	/**
	 * cmdSetTariff_Single (STS 미터용) - Single Command
	 * 
	 * 
	 * @param target
	 * @param yyyymmdd
	 * @param condLimit1
	 * @param condLimit2
	 * @param param
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetTariffSingle")
	public ModelAndView cmdSetTariff_Single(Integer supplierId, String target, Integer tariffType, String yyyymmdd,
			String govLey, String streetLightLevy, String vat, String param) {
		ModelAndView mav = new ModelAndView("jsonView");
		JSONArray jsonArr = null;
		List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");
		String cmd = "cmdSetTariff";
		String cons = "";
		String fixedRate = "";

		String condRate1 = "";
		String condRate2 = "";
		String activeEnergyCharge = "";
		String lifeLineSubsidy = "";
		String requestType = CommandType.Set.name().toUpperCase();

		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("result", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}

		try {
			if (param == null || param.isEmpty()) {
				jsonArr = new JSONArray();
			} else {
				jsonArr = JSONArray.fromObject(param);
			}

			List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
			Map<String, String> dataMap = new HashMap<String, String>();
			if (jsonArr.size() > 0) {
				Object[] tempJson = jsonArr.toArray();
				for (int i = 0; i < tempJson.length; i++) {
					JSONArray jsonArr2 = (JSONArray) tempJson[i];
					Map<String, Object> jsonMap = (Map<String, Object>) jsonArr2.get(0);
					dataMap = new HashMap<String, String>();

					String j_cons = jsonMap.get("cons").toString();
					String j_fixedRate = jsonMap.get("fixedRate").toString();
					String j_activeEnergyCharge = jsonMap.get("activeEnergyCharge").toString();
					String j_lifeLineSubsidy = jsonMap.get("lifeLineSubsidy").toString();
					String j_condRate1 = jsonMap.get("condRate1").toString();
					String j_condRate2 = jsonMap.get("condRate2").toString();

					dataMap.put("cons", j_cons);
					dataMap.put("fixedRate", j_fixedRate);
					dataMap.put("activeEnergyCharge", j_activeEnergyCharge);
					dataMap.put("lifeLineSubsidy", j_lifeLineSubsidy);
					dataMap.put("condRate1", j_condRate1);
					dataMap.put("condRate2", j_condRate2);
					dataList.add(dataMap);
				}
			}

			int tariffSize = dataList.size();
			for (int i = 0; i < tariffSize; i++) {
				Map<String, String> map = dataList.get(i);
				if (i == 0) {
					cons = map.get("cons").toString();
					fixedRate = map.get("fixedRate").toString();
					activeEnergyCharge = map.get("activeEnergyCharge").toString();
					lifeLineSubsidy = map.get("lifeLineSubsidy").toString();

					condRate1 = map.get("condRate1").toString();
					condRate2 = map.get("condRate2").toString();
				} else {
					cons += "," + map.get("cons").toString();
					fixedRate += "," + map.get("fixedRate").toString();
					activeEnergyCharge += "," + map.get("activeEnergyCharge").toString();
					condRate1 += "," + map.get("condRate1").toString();
					condRate2 += "," + map.get("condRate2").toString();
					lifeLineSubsidy += "," + map.get("lifeLineSubsidy").toString();
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Unknown.(" + e.getMessage() + ")");
			return mav;
		}

		try {
			// Save Log (S)
			Map<String, Object> paramMap = new HashMap<String, Object>();
			String switchTime = yyyymmdd + "000000";

			paramMap.put("modemId", meter.getModem().getDeviceSerial());
			paramMap.put("meterId", meter.getMdsId());
			paramMap.put("requestType", requestType);
			paramMap.put("supplySize", cons);
			paramMap.put("serviceCharge", fixedRate);
			paramMap.put("utilityRelief", condRate2); // Utility Relief Subsidy
			paramMap.put("normalSubsidy", condRate1); // Gov Subsidy
			paramMap.put("activeEnergyCharge", activeEnergyCharge);
			paramMap.put("govLey", govLey);
			paramMap.put("streetLightLevy", streetLightLevy);
			paramMap.put("vat", vat);
			paramMap.put("lifeLineSubsidy", lifeLineSubsidy);
			paramMap.put("switchTime", switchTime);

			saveLog(cmd, paramMap);
			// Save Log (E)

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap = cmdOperationUtil.cmdSTSBlockSetTariff(paramMap);

			if (resultMap.get("result").toString() == "Success") {
				mav.addObject("result", "Success : Please check the command results on the STS Information tab in the [Customer and Contract] gadget.");
			} else {
				mav.addObject("result", "FAIL : " + resultMap.get("result").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;
	}

	/**
	 * cmdGetTariff (STS 미터용) - Group Command
	 * 
	 * 
	 * @param target
	 * @param tariffMode
	 *            (0:current tariff, 1:future tariff)
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetTariff")
	public ModelAndView cmdGetTariff(Integer supplierId, String target, String tariffMode) {

		return null;
	}

	/**
	 * cmdGetTariff_Single (STS 미터용) - Single Command
	 * 
	 * 
	 * @param target
	 * @param tariffMode
	 *            (0:current tariff, 1:future tariff)
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetTariffSingle")
	public ModelAndView cmdGetTariff_Single(Integer supplierId, String target, String tariffMode) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdGetTariff";

		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");

		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("result", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}

		String requestType = CommandType.Get.name().toUpperCase();

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSBlockGetTariff(meter.getModem().getDeviceSerial(), meter.getMdsId(),
					requestType, tariffMode);

			if (map.get("result").toString() == "Success") {
				mav.addObject("result", "Please check the command results on the STS Information tab in the [Customer and Contract] gadget.");
			} else {
				mav.addObject("result", "FAIL : " + map.get("result").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;
	}

	/**
	 * cmdSetFriendlyCreditSchedule 휴일 정보를 Set. (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetFriendlyCreditSchedule")
	public ModelAndView cmdSetFriendlyCreditSchedule(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdSetFriendlyCreditSchedule";
		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");
		String requestType = CommandType.Set.name().toUpperCase();

		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("rtnStr", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}

		// get properties (S)
		String friendlyCredit = "";
		String friendlyDays = "";

		try {
			Properties prop = new Properties();
			prop.load(getClass().getClassLoader().getResourceAsStream("command.properties"));

			friendlyCredit = prop.getProperty("sts.friendlyCredit") == null ? "0"
					: prop.getProperty("sts.friendlyCredit");
			friendlyDays = prop.getProperty("sts.friendlyDays"); // example)
																	// yyyyMMdd,yyyyMMdd,yyyyMMdd,yyyyMMdd

			/** fmp.properties에 설정되어있는 friendly property 정보 */
			// WEB UI상으로는 credit amount, friendly day 정보만 설정할 수 있도록하고, 나머지
			// weekday, time 정보는 properties에 정의되어있는 값을 default로 사용한다.

			// prepost.sts.protocol.friendly.set.use=false
			// prepost.sts.protocol.friendly.time.set=0930,1830
			// prepost.sts.protocol.friendly.weekday.set=5,6
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "Unable to find relevant information. Please check the <b>property file.</b>");
			return mav;
		}
		// get properties (E)

		// Save Log (S)
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("requestType", requestType);
		paramMap.put("meterId", meter.getMdsId());
		paramMap.put("friendlyCredit", friendlyCredit);
		paramMap.put("friendlyDays", friendlyDays);

		saveLog(cmd, paramMap);
		// Save Log (E)

		CommandGW cgw = new CommandGW();

		// friendly day table (S)
		try {
			int classId = DLMS_CLASS.SPECIAL_DAY.getClazz();
			int attrId = DLMS_CLASS_ATTR.REGISTER_ATTR02.getAttr();
			String param = cgw.convertObis(OBIS.STS_FRIENDLY_DAY_TABLE.getCode()) + "|" + classId + "|" + attrId + "|RW|ARRAY|" + friendlyDays;

			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(), requestType, param);

			if (!map.get("RESULT_VALUE").equals("Success")) {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}
		// friendly day table (E)

		// friendly credit threshold (S)
		try {
			Thread.sleep(25000); // 25 sec

			int classId = DLMS_CLASS.REGISTER.getClazz();
			int attrId = DLMS_CLASS_ATTR.REGISTER_ATTR02.getAttr();
			String param = cgw.convertObis(OBIS.FRIENDLY_CREDIT_THRESHOLD.getCode()) + "|" + classId + "|" + attrId
					+ "|RW|UINT32|" + friendlyCredit;

			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(),
					requestType, param);

			if (map.get("RESULT_VALUE").equals("Success")) {
				mav.addObject("result", "SUCCESS");
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}
		// friendly credit threshold (E)

		return mav;
	}

	/**
	 * cmdGetFriendlyCreditSchedule 휴일 정보를 Get. (STS 미터용)
	 * 
	 * 
	 * @param modemId
	 * @param fcMode
	 *            (0:Current schedule, 1:Pending schedule)
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetFriendlyCreditSchedule")
	public ModelAndView cmdGetFriendlyCreditSchedule(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String cmd = "cmdGetFriendlyCreditSchedule";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		CommandGW cgw = new CommandGW();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String requestType = CommandType.Get.name().toUpperCase();
		String result = "";

		// friendly day table (S)
		try {
			int classId = DLMS_CLASS.SPECIAL_DAY.getClazz();
			int attrId = DLMS_CLASS_ATTR.REGISTER_ATTR02.getAttr();
			String param = cgw.convertObis(OBIS.STS_FRIENDLY_DAY_TABLE.getCode()) + "|" + classId + "|" + attrId
					+ "|RW|ARRAY|";

			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(),
					requestType, param);

			if (map.get("RESULT_VALUE").equals("Success")) {
				String jsonStr = map.get("value").toString();
				JSONArray jsonArr = JSONArray.fromObject(jsonStr);
				Map jsonMap = (Map<String, Object>) jsonArr.toArray()[0];

				String friendlyTime = jsonMap.get("GET_STS_FRIENDLY_TIME").toString();
				String friendlyWeek = jsonMap.get("GET_STS_FRIENDLY_WEEK").toString();
				result += "<b>Friendly Time: </b>" + friendlyTime + "<br/>";
				result += "<b>Friendly Week: </b>" + friendlyWeek + "<br/>";

				String temp = "";
				String friendlyDays = jsonMap.get("GET_STS_FRIENDLY_DAYS_TABLE").toString();
				String[] filter_word = { "\\[", "\\]", "\\{", "\\}" };

				for (int i = 0; i < filter_word.length; i++) {
					temp = friendlyDays.replaceAll(filter_word[i], "");
					friendlyDays = temp;
				}

				result += "<b>Friendly Days: </b>" + friendlyDays;

				paramMap.put("friendlyTime", friendlyTime);
				paramMap.put("friendlyWeek", friendlyWeek);
				paramMap.put("friendlyDays", friendlyDays);
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}
		// friendly day table (E)

		// friendly credit threshold (S)
		try {
			Thread.sleep(25000); // 25 sec

			int classId = DLMS_CLASS.REGISTER.getClazz();
			int attrId = DLMS_CLASS_ATTR.REGISTER_ATTR02.getAttr();
			String param = cgw.convertObis(OBIS.FRIENDLY_CREDIT_THRESHOLD.getCode()) + "|" + classId + "|" + attrId
					+ "|RW|UINT32|";

			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(),
					requestType, param);

			if (map.get("RESULT_VALUE").equals("Success")) {
				String friendlyCredit = map.get("value").toString();
				result += "<br/><br/><b>Credit Amount: </b>" + friendlyCredit + " cedi" + "<br/>";
				mav.addObject("result", result);

				paramMap.put("requestType", requestType);
				paramMap.put("meterId", meter.getMdsId());
				paramMap.put("friendlyCredit", map.get("value").toString());
				saveLog(cmd, paramMap);
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}
		// friendly credit threshold (E)

		return mav;
	}

	/**
	 * cmdSetEmergencyCredit EmergencyCredit정보 Setting (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @param ec_mode
	 * @param days
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetEmergencyCredit")
	public ModelAndView cmdSetEmergencyCredit(String target, String ec_mode, String days) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdSetEmergencyCredit";

		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");

		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("result", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}

		CommandGW cgw = new CommandGW();
		String param = "";
		int classId = DLMS_CLASS.REGISTER.getClazz();
		int attrId = DLMS_CLASS_ATTR.REGISTER_ATTR02.getAttr();
		String value = days; // amount
		String obisCode = cgw.convertObis(OBIS.EMERGENCY_CREDIT.getCode());
		String requestType = CommandType.Set.name().toUpperCase();

		log.debug("(" + cmd + ")obisCode => " + obisCode + ", classId => " + classId + ", attributeId => " + attrId);
		param = obisCode + "|" + classId + "|" + attrId + "|RW|UINT32|" + value;

		try {
			// Save Log (S)
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("requestType", requestType);
			paramMap.put("meterId", meter.getMdsId());
			paramMap.put("emergencyCredit", value);
			saveLog(cmd, paramMap);
			// Save Log (E)

			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(),
					requestType, param);
			log.info("result: " + map);

			if (map.get("RESULT_VALUE").equals("Success")) {
				mav.addObject("result", "SUCCESS");
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;
	}

	/**
	 * cmdGetEmergencyCredit EmergencyCredit정보 Getting (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetEmergencyCredit")
	public ModelAndView cmdGetEmergencyCredit(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdGetEmergencyCredit";

		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");

		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("result", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}

		CommandGW cgw = new CommandGW();
		String param = "";
		int classId = DLMS_CLASS.REGISTER.getClazz();
		int attrId = DLMS_CLASS_ATTR.REGISTER_ATTR02.getAttr();
		String value = "";
		String obisCode = cgw.convertObis(OBIS.EMERGENCY_CREDIT.getCode());
		String requestType = CommandType.Get.name().toUpperCase();

		log.debug("(" + cmd + ")obisCode => " + obisCode + ", classId => " + classId + ", attributeId => " + attrId);
		param = obisCode + "|" + classId + "|" + attrId + "|RW|UINT32|" + value;

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSGeneralCommand(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(),
					requestType, param);

			if (map.get("RESULT_VALUE").equals("Success")) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				mav.addObject("result", "Emergency Credit : " + map.get("value").toString() + " cedi");

				paramMap.put("requestType", requestType);
				paramMap.put("meterId", meter.getMdsId());
				paramMap.put("emergencyCredit", map.get("value").toString());

				saveLog(cmd, paramMap);
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}
		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;
	}

	/**
	 * cmdGetSpecificMonthNetCharge 특정달을 입력받아 월정산 내역을 가져옴. (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @param yyyy
	 * @param mm
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetSpecificMonthNetCharge")
	public ModelAndView cmdGetSpecificMonthNetCharge(String target, String yyyy, String mm) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String cmd = "cmdGetSpecificMonthNetCharge";
		Meter meter = (Meter) validateMap.get("meter");

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		if (mm.length() == 1) {
			mm = "0" + mm;
		}

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSNetCharge(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(),
					yyyy + mm);
			log.debug("map: " + map);

			if (map.get("RESULT_VALUE").equals("Success")) {
				mav.addObject("result", "Usage Fee: " + map.get("value").toString() + " cedi");

				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("requestType", CommandType.Get.name().toUpperCase());
				paramMap.put("meterId", meter.getMdsId());
				paramMap.put("usedCost", map.get("value").toString());
				saveLog(cmd, paramMap);
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}

		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;
	}

	/**
	 * cmdSetMessage STS미터에 message를 보낸다. (STS 미터용) - Group Command
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetMessage")
	public ModelAndView cmdSetMessage(String target, Integer supplierId, String message) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdSetMessage";

		try {

			List<Map<String, Object>> targetList = new ArrayList<Map<String, Object>>();
			Map<String, Object> validateMap = null;

			if (target == null || "".equals(target)) {
				String modelName = "OmniPower STS";
				List<DeviceModel> model = modelManager.getDeviceModelByName(supplierId, modelName);
				if (model.size() > 0) {
					// 미터 모델이 OmniPower STS이고, mmiu 타입이고 폰넘버가 존재하고 프로토콜 타이이
					// 0102이고, 프로토콜이 sms이고 미터가 존재하는 모뎀.
					Map<String, Object> condition = new HashMap<String, Object>();
					condition.put("protocolType", Protocol.SMS.name());
					condition.put("modemType", ModemType.MMIU.name());
					condition.put("modelName", "OmniPower STS");
					condition.put("meterTypeCode", "1.3.1.1");
					condition.put("protocolVersion", "0102");
					targetList = modemManager.getSTSModem(condition);
					if (targetList.size() > 0) {
						target = targetList.get(0).get("METERID").toString();
					}
				}

				validateMap = commonSTSValidate(target);
			} else {
				validateMap = commonSTSValidate(target);

				Meter meter = (Meter) validateMap.get("meter");
				Contract contract = meter.getContract();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("DEVICESERIAL", (String) validateMap.get("deviceSerial"));
				map.put("MDSID", meter.getMdsId());
				map.put("PHONENUMBER", (String) validateMap.get("mobileNo"));
				targetList.add(map);
			}

			// validate 체크
			Boolean validateCheck = (Boolean) validateMap.get("result");
			if (!validateCheck) {
				mav.addObject("rtnStr", "FAIL : " + validateMap.get("errorMessage"));
				return mav;
			}

			List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
			Map<String, String> paramMap = new HashMap<String, String>();
			// paramMap.put("message", message);
			paramMap.put("string", message);
			paramList.add(paramMap);

			String messageId = null;
			String deviceSerial = null;
			long trId = 0L;
			String mdsId = null;
			String createDate = null;
			for (int i = 0; i < targetList.size(); i++) {
				try {
					Map<String, Object> targetMap = targetList.get(i);
					deviceSerial = (String) targetMap.get("DEVICESERIAL");
					mdsId = (String) targetMap.get("MDSID");

					trId = System.currentTimeMillis();
					saveAsyncCommandList(deviceSerial, trId, cmd, paramList, TimeUtil.getCurrentTime());

					// ECGSTSLog 저장
					createDate = DateTimeUtil.getDateString(new Date());
					if (mdsId != null) {
						createEcgStsLog(mdsId, cmd, createDate, null, null, null, null, null, null, null, null, null,
								new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
								null, 100, null, trId, createDate, null, null, null, null, null, null, null, null, null,
								null, null, null, null, null, null, null, null, null);
					}

					// SMS 전송
					String smsSkip = (String) validateMap.get("smsSkip");
					if ("true".equals(smsSkip) || smsSkip == "true") {
						// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
						messageId = "smsSkip";
						boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
								: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
						if (triggerUse == true) {
							Modem modem = modemManager.getModem(deviceSerial);
							if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
								mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
								return mav;
							}
						} else {
							log.info("No use TCP Trigger");
						}
					} else {
						messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
								(String) validateMap.get("PHONENUMBER"), (String) validateMap.get("smsMsg"),
								(Properties) validateMap.get("prop"));
					}

				} catch (Exception e1) {
					log.error(e1, e1);
					continue;
				}
			}

			// 상태가 바뀌는 시간을 기다려주기 위해 1분 sleep
			Thread.sleep(60000);

			if (targetList.size() == 1) {
				try {
					if ("fail".equals(messageId)) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Request CMD Fail");
					} else if ("smsSkip".equals(messageId)) {
						mav.addObject("rtnStr",
								"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
						log.debug("Save AsyncCommandLog");
					} else {
						mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
						log.debug("Request CMD Success");

						Integer lastStatus = null;

						lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

						if (TR_STATE.Success.getCode() != lastStatus) {
							mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
							log.debug("Fail : Command Result[" + cmd + "]");
						} else {
							log.debug("Success: Command Result[" + cmd + "]");
							mav.addObject("rtnStr", "Done: Command Result[" + cmd + "]");
							EcgSTSLogPk stsPk = new EcgSTSLogPk();
							stsPk.setAsyncTrId(trId);
							stsPk.setCmd(cmd);
							stsPk.setMeterNumber(mdsId);
							stsPk.setCreateDate(createDate);
							EcgSTSLog stsLog = stslogDao.get(stsPk);

							Supplier supplier = supplierDao.get(supplierId);
							// 1:실패발샏
							int rtnMode = stsLog.getResult();
							String resultMsg = "Done:Send SMS Command(" + cmd + ").\n";

							String resultDate = stsLog.getResultDate() == null ? null
									: TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(stsLog.getResultDate()),
											supplier.getLang().getCode_2letter(),
											supplier.getCountry().getCode_2letter());

							if (rtnMode == 0) {
								resultMsg += "Result : SUCCESS\n";
								resultMsg += "Result Date : " + resultDate + "\n";
							} else if (rtnMode == 1) {
								String failReason = stsLog.getFailReason() == null ? "" : stsLog.getFailReason();
								resultMsg += "Result : FAIL\n";
								resultMsg += "Result Date : " + resultDate + "\n";
								resultMsg += "Fail Reason : " + failReason + "\n";
							} else if (rtnMode == 100) {
								resultMsg += "Result : NOT RESPONSE\n";
								resultMsg += "Result Date : " + resultDate + "\n";
							} else {
								resultMsg += "Result : Unknown\n";
								resultMsg += "Result Date : " + resultDate + "\n";
							}

							mav.addObject("rtnMsg", resultMsg);
						}
					}
				} catch (Exception e) {
					log.error(e, e);
				}
			}

		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
			return mav;
		}

		return mav;
	}

	/**
	 * cmdSetMessage_Single STS미터에 message를 보낸다. (STS 미터용) - Single Command
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetMessageSingle")
	public ModelAndView cmdSetMessage_Single(String target, Integer supplierId, String message) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");
		String modemType = (String) validateMap.get("modemType");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String phoneNumber = (String) validateMap.get("mobileNo");
		String mdsId = meter.getMdsId().toString();
		String cmd = "cmdSetMessage";
		String createDate = DateTimeUtil.getDateString(new Date());

		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("rtnStr", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}

		/** GPRS STS METER */
		if (modemType == ModemType.MMIU.toString()) {
			try {
				List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
				Map<String, String> paramMap = new HashMap<String, String>();
				paramMap.put("string", message);
				paramList.add(paramMap);

				String messageId = null;
				// String createDate = null;
				long trId = 0L;
				trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, paramList, TimeUtil.getCurrentTime());

				// ECGSTSLog 저장
				// createDate = DateTimeUtil.getDateString(new Date());
				if (mdsId != null) {
					createEcgStsLog(mdsId, cmd, createDate, null, null, null, null, null, null, null, null, null,
							new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
							null, 100, null, trId, createDate, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null);
				}

				// SMS 전송
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"), phoneNumber,
							(String) validateMap.get("smsMsg"), (Properties) validateMap.get("prop"));
				}

				// 상태가 바뀌는 시간을 기다려주기 위해 1분 sleep
				Thread.sleep(60000);

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;
					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success: Command Result[" + cmd + "]");
						mav.addObject("rtnStr", "Done: Command Result[" + cmd + "]");
						EcgSTSLogPk stsPk = new EcgSTSLogPk();
						stsPk.setAsyncTrId(trId);
						stsPk.setCmd(cmd);
						stsPk.setMeterNumber(mdsId);
						stsPk.setCreateDate(createDate);
						EcgSTSLog stsLog = stslogDao.get(stsPk);

						Supplier supplier = supplierDao.get(supplierId);
						// 1:실패발생
						int rtnMode = stsLog.getResult();
						String resultMsg = "Done:Send SMS Command(" + cmd + ").\n";

						String resultDate = stsLog.getResultDate() == null ? null
								: TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(stsLog.getResultDate()),
										supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter());

						if (rtnMode == 0) {
							resultMsg += "Result : SUCCESS\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						} else if (rtnMode == 1) {
							String failReason = stsLog.getFailReason() == null ? "" : stsLog.getFailReason();
							resultMsg += "Result : FAIL\n";
							resultMsg += "Result Date : " + resultDate + "\n";
							resultMsg += "Fail Reason : " + failReason + "\n";
						} else if (rtnMode == 100) {
							resultMsg += "Result : NOT RESPONSE\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						} else {
							resultMsg += "Result : Unknown\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						}

						mav.addObject("rtnMsg", resultMsg);
					}
				}
			} catch (Exception e) {
				log.error(e, e);
				mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
				return mav;
			}
		} else {
			/** ZigBee STS METER */
			String mcuSysId = validateMap.get("mcuSysId").toString();

			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			cmd = cmdZigbeeSTS + cmd;
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());
			datas.add("message," + message);

			Map<String, Object> map = new HashMap<String, Object>();
			map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
			String successMessage = "";
			String result = map.get("result").toString();

			if (result.equals("fail")) {
				mav.addObject("rtnStr", map.get("reason").toString());
				mav.addObject("rtnMsg", null);
			} else if (result.equals("success")) {
				successMessage += "Result : SUCCESS\n";

				mav.addObject("rtnStr", null);
				mav.addObject("rtnMsg", successMessage);
			}
		}

		return mav;
	}

	/**
	 * cmdGetPriviousMothNetCharge 이전달의 월정산 로그를 가져온다.
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetPreviousMonthNetCharge")
	public ModelAndView cmdGetPreviousMonthNetCharge(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdGetPreviousMonthNetCharge";
		Map<String, Object> validateMap = commonSTSValidate(target);
		Meter meter = (Meter) validateMap.get("meter");

		// validate 체크
		Boolean validateCheck = (Boolean) validateMap.get("result");
		if (!validateCheck) {
			mav.addObject("rtnStr", "FAIL : " + validateMap.get("errorMessage"));
			return mav;
		}

		CommandGW cgw = new CommandGW();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		String yyyymm = sdf.format(cal.getTime()); // param

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map = cmdOperationUtil.cmdSTSNetCharge(meter.getModem().getDeviceSerial().toString(), meter.getMdsId(),
					yyyymm);

			if (map.get("RESULT_VALUE").equals("Success")) {
				mav.addObject("result", "Usage Fee: " + map.get("value").toString() + " cedi");

				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("requestType", CommandType.Get.name().toUpperCase());
				paramMap.put("meterId", meter.getMdsId());
				paramMap.put("usedCost", map.get("value").toString());
				saveLog(cmd, paramMap);
			} else {
				mav.addObject("result", "FAIL : " + map.get("resultValue").toString());
			}

		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("result", "FAIL : Communication Error");
		}

		return mav;
	}

	/**
	 * cmdSuniFirmwareWrite SUNI Module 펌웨어 업그레이드 (STS 미터용)
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSuniFirmwareWrite")
	public ModelAndView cmdSTSOTA(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("jsonView");
		String target = null;
		String ext = null;
		String finalFilePath = null;
		String deviceSerial = null;
		String modemType = null;
		Map<String, Object> validateMap = null;
		String cmd = "cmdSuniFirmwareWrite";

		try {
			byte[] fileBinary = null;
			String filePath = null;

			MultipartHttpServletRequest multiReq = (MultipartHttpServletRequest) request;
			MultipartFile multipartFile = multiReq.getFile("userfile");

			target = request.getParameter("target");
			validateMap = commonSTSValidate(target);
			modemType = (String) validateMap.get("modemType");
			ext = request.getParameter("ext");

			Boolean validateCheck = (Boolean) validateMap.get("result");
			if (!validateCheck) {
				mav.addObject("rtnStr", validateMap.get("errorMessage"));
				return mav;
			}

			deviceSerial = (String) validateMap.get("deviceSerial");

			fileBinary = multipartFile.getBytes();
			filePath = multipartFile.getOriginalFilename().substring(0,
					multipartFile.getOriginalFilename().lastIndexOf("."));

			// 파일 저장
			String osName = System.getProperty("os.name");
			String homePath = "";
			if (osName != null && !"".equals(osName) && osName.toLowerCase().indexOf("window") >= 0) {
				homePath = CommandProperty.getProperty("firmware.window.dir");
			} else {
				homePath = CommandProperty.getProperty("firmware.dir");
			}

			finalFilePath = FileUtils.makeFirmwareDirectory(homePath, filePath, ext, true);

			log.info(String.format("Save firmware file - %s", finalFilePath));
			FileOutputStream foutStream = new FileOutputStream(finalFilePath, true);

			foutStream.write(fileBinary);
			foutStream.flush();
			foutStream.close();

		} catch (Exception e) {
			log.error(e);
			mav.addObject("rtnStr", "Fail: Save firmware file error!!");
			return mav;
		}

		/** GPRS STS METER */
		if (modemType == ModemType.MMIU.toString()) {
			try {
				List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
				Map<String, String> map = new HashMap<String, String>();
				map.put("string", finalFilePath);
				paramList.add(map);

				// 비동기 내역 저장
				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, paramList, TimeUtil.getCurrentTime());

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 서버에서 모뎀으로 내리는데 걸리는 시간 10분 sleep
					Thread.sleep(1000 * 60 * 10);

					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						mav.addObject("rtnStr", "Done:Send SMS Command(" + cmd + ").");
						log.debug("Success : Command Result[" + cmd + "]");
					}
				}

			} catch (Exception e) {
				log.error(e, e);
				mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
				return mav;
			}

		} else {
			/** ZigBee STS METER */
			String mcuSysId = validateMap.get("mcuSysId").toString();
			Meter meter = (Meter) validateMap.get("meter");
			String mdsId = meter.getMdsId();

			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			cmd = cmdZigbeeSTS + cmd;
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());
			datas.add("filePath," + finalFilePath);

			Map<String, Object> map = new HashMap<String, Object>();
			map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
			String successMessage = "";
			String result = map.get("result").toString();

			if (result.equals("fail")) {
				mav.addObject("rtnStr", map.get("reason").toString());
				mav.addObject("rtnMsg", null);
			} else if (result.equals("success")) {
				successMessage += "Result : SUCCESS\n";

				mav.addObject("rtnStr", null);
				mav.addObject("rtnMsg", successMessage);
			}
		}

		return mav;
	}

	/**
	 * cmdSuniFirmwareRead SUNI Module 펌웨어 읽기 (STS 미터용)
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSuniFirmwareRead")
	public ModelAndView cmdSuniFirmwareRead(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		String modemType = (String) validateMap.get("modemType");
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String cmd = "cmdSuniFirmwareRead";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		/** GPRS STS METER */
		if (modemType == ModemType.MMIU.toString()) {
			try {
				// 비동기 내역 저장
				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, null, TimeUtil.getCurrentTime());

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 상태가 바뀌는 시간을 기다려주기 위해 60초 sleep
					Thread.sleep(60000);

					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success : Command Result[" + cmd + "]");
						mav.addObject("rtnStr", "Done : Command Result[" + cmd + "]");
						StringBuffer resultSb = new StringBuffer();
						resultSb.append("Done:Send SMS Command(" + cmd + ").\n");
						List<AsyncCommandResult> acplist = asyncCommandLogManager.getCmdResults(deviceSerial, trId,
								null);
						if (acplist != null && acplist.size() > 0) {
							resultSb.append("Result : SUCCESS\n");
							StringBuilder sb = new StringBuilder();
							for (AsyncCommandResult result : acplist) {
								if (result.getResultType().contains("result")) {
									sb.append(result.getResultValue() + "\n");
								}
							}
							resultSb.append("\n" + sb.toString());
						}
						mav.addObject("rtnMsg", resultSb.toString());
					}
				}

			} catch (Exception e) {
				log.error(e, e);
				mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
				return mav;
			}

		} else {
			/** ZigBee STS METER */
			String mcuSysId = validateMap.get("mcuSysId").toString();
			Meter meter = (Meter) validateMap.get("meter");
			String mdsId = meter.getMdsId();

			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			cmd = cmdZigbeeSTS + cmd;
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());

			Map<String, Object> map = new HashMap<String, Object>();
			map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
			String successMessage = "";
			String result = map.get("result").toString();

			if (result.equals("fail")) {
				mav.addObject("rtnStr", map.get("reason").toString());
				mav.addObject("rtnMsg", null);
			} else if (result.equals("success")) {
				successMessage += "Result : SUCCESS\n";
				successMessage += "status : " + map.get("status") + "\n";
				successMessage += "moduleSerial(hex) : " + map.get("moduleSerial") + "\n";
				successMessage += "moduleTypeId : " + map.get("moduleTypeId") + "\n";
				successMessage += "moduleRevision : " + map.get("moduleRevision") + "\n";
				successMessage += "moduleMinorVer : " + map.get("moduleMinorVer") + "\n";
				successMessage += "moduleMajorVer : " + map.get("moduleMajorVer") + "\n";
				successMessage += "fileCtrl : " + map.get("fileCtrl") + "\n";
				successMessage += "fileTypeId : " + map.get("fileTypeId") + "\n";
				successMessage += "fileRevision : " + map.get("fileRevision") + "\n";
				successMessage += "fileMinorVer : " + map.get("fileMinorVer") + "\n";
				successMessage += "fileMajorVer : " + map.get("fileMajorVer") + "\n";
				successMessage += "fileByteSize : " + map.get("fileByteSize") + "\n";

				mav.addObject("rtnStr", null);
				mav.addObject("rtnMsg", successMessage);
			}
		}

		return mav;
	}

	/**
	 * cmdSuniFirmwareUpdateKeyWrite SUNI Module 펌웨어 덥데이트 정보 쓰기 (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSuniFirmwareUpdateKeyWrite")
	public ModelAndView cmdSuniFirmwareUpdateKeyWrite(String target, String keyNo, String keyHex) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		String modemType = (String) validateMap.get("modemType");
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String cmd = "cmdSuniFirmwareUpdateKeyWrite";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		/** GPRS STS METER */
		if (modemType == ModemType.MMIU.toString()) {
			try {
				// 비동기 내역 저장
				List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
				Map<String, String> paramMap = new HashMap<String, String>();
				paramMap.put("int", keyNo);
				paramList.add(paramMap);
				paramMap = new HashMap<String, String>();
				paramMap.put("string", keyHex);
				paramList.add(paramMap);

				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, paramList, TimeUtil.getCurrentTime());

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 상태가 바뀌는 시간을 기다려주기 위해 1분 sleep
					Thread.sleep(60000);
					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success : Command Result[" + cmd + "]");
						mav.addObject("rtnStr", "Done : Command Result[" + cmd + "]");

						StringBuffer resultSb = new StringBuffer();

						List<AsyncCommandResult> acplist = asyncCommandLogManager.getCmdResults(deviceSerial, trId,
								null);
						if (acplist != null && acplist.size() > 0) {
							StringBuilder sb = new StringBuilder();
							for (AsyncCommandResult result : acplist) {
								if (result.getResultType().contains("result")) {
									sb.append(result.getResultValue() + "\n");
								}
							}
							resultSb.append("\n" + sb.toString());
						}
						mav.addObject("rtnMsg", resultSb.toString());
					}
				}

			} catch (Exception e) {
				log.error(e, e);
				mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
				return mav;
			}
		} else {
			/** ZigBee STS METER */
			String mcuSysId = validateMap.get("mcuSysId").toString();
			Meter meter = (Meter) validateMap.get("meter");
			String mdsId = meter.getMdsId();

			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			cmd = cmdZigbeeSTS + cmd;
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());
			datas.add("keyNo," + keyNo);
			datas.add("keyHex," + keyHex);

			Map<String, Object> map = new HashMap<String, Object>();
			map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
			String successMessage = "";
			String result = map.get("result").toString();

			if (result.equals("fail")) {
				mav.addObject("rtnStr", map.get("reason").toString());
				mav.addObject("rtnMsg", null);
			} else if (result.equals("success")) {
				successMessage += "Result : SUCCESS\n";

				mav.addObject("rtnStr", null);
				mav.addObject("rtnMsg", successMessage);
			}
		}

		return mav;
	}

	/**
	 * cmdGetSuniFirmwareUpdateInfo SUNI Module 펌웨어 업데이트 정보 읽기 (STS 미터용)
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetSuniFirmwareUpdateInfo")
	public ModelAndView cmdGetSuniFirmwareUpdateInfo(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		String modemType = (String) validateMap.get("modemType");
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String cmd = "cmdGetSuniFirmwareUpdateInfo";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		/** GPRS STS METER */
		if (modemType == ModemType.MMIU.toString()) {
			try {
				// 비동기 내역 저장
				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, null, TimeUtil.getCurrentTime());

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 상태가 바뀌는 시간을 기다려주기 위해 60초 sleep
					Thread.sleep(60000);

					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success : Command Result[" + cmd + "]");
						StringBuffer resultSb = new StringBuffer();
						resultSb.append("Done:Command Result[" + cmd + "]\n");

						List<AsyncCommandResult> acplist = asyncCommandLogManager.getCmdResults(deviceSerial, trId,
								null);
						if (acplist != null && acplist.size() > 0) {
							StringBuilder sb = new StringBuilder();
							for (AsyncCommandResult result : acplist) {
								if (result.getResultType().contains("result")) {
									sb.append(result.getResultValue() + "\n");
								}
							}
							resultSb.append("\n" + sb.toString());
						}
						mav.addObject("rtnStr", resultSb.toString());
					}
				}

			} catch (Exception e) {
				log.error(e, e);
				mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
				return mav;
			}
		} else {
			/** ZigBee STS METER */
			String mcuSysId = validateMap.get("mcuSysId").toString();
			Meter meter = (Meter) validateMap.get("meter");
			String mdsId = meter.getMdsId();

			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			cmd = cmdZigbeeSTS + cmd;
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());

			Map<String, Object> map = new HashMap<String, Object>();
			map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
			String successMessage = "";
			String result = map.get("result").toString();

			if (result.equals("fail")) {
				mav.addObject("rtnStr", map.get("reason").toString());
				mav.addObject("rtnMsg", null);
			} else if (result.equals("success")) {
				successMessage += "Result : SUCCESS\n";

				mav.addObject("rtnStr", null);
				mav.addObject("rtnMsg", successMessage);
			}
		}

		return mav;
	}

	/**
	 * cmdSuniFirmwareUpdateKeyRead SUNI Module 펌웨어 키 읽기 (STS 미터용)
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSuniFirmwareUpdateKeyRead")
	public ModelAndView cmdSuniFirmwareUpdateKeyRead(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		String modemType = (String) validateMap.get("modemType");
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String cmd = "cmdSuniFirmwareUpdateKeyRead";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		/** GPRS STS METER */
		if (modemType == ModemType.MMIU.toString()) {
			try {
				// 비동기 내역 저장
				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, null, TimeUtil.getCurrentTime());

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 상태가 바뀌는 시간을 기다려주기 위해 60초 sleep
					Thread.sleep(60000);

					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success : Command Result[" + cmd + "]");
					}
				}

			} catch (Exception e) {
				log.error(e, e);
				mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
				return mav;
			}
		} else {
			/** ZigBee STS METER */
			String mcuSysId = validateMap.get("mcuSysId").toString();
			Meter meter = (Meter) validateMap.get("meter");
			String mdsId = meter.getMdsId();

			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			cmd = cmdZigbeeSTS + cmd;
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());

			Map<String, Object> map = new HashMap<String, Object>();
			map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
			String successMessage = "";
			String result = map.get("result").toString();

			if (result.equals("fail")) {
				mav.addObject("rtnStr", map.get("reason").toString());
				mav.addObject("rtnMsg", null);
			} else if (result.equals("success")) {
				successMessage += "Result : SUCCESS\n";
				successMessage += "Key Number : " + map.get("keyNumber").toString() + "\n";
				successMessage += "Key : " + map.get("key").toString() + "\n";

				mav.addObject("rtnStr", null);
				mav.addObject("rtnMsg", successMessage);
			}

		}

		return mav;
	}

	/**
	 * cmdGetCIUCommStateHistory CIU 통신 상태 내역 조회 (STS 미터용)
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetCIUCommStateHistory")
	public ModelAndView cmdGetCIUCommStateHistory(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		String modemType = (String) validateMap.get("modemType");
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String cmd = "cmdGetCIUCommStateHistory";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		/** GPRS STS METER */
		if (modemType == ModemType.MMIU.toString()) {
			try {
				// 비동기 내역 저장
				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, null, TimeUtil.getCurrentTime());

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 상태가 바뀌는 시간을 기다려주기 위해 60초 sleep
					Thread.sleep(60000);

					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success : Command Result[" + cmd + "]");
					}
				}

			} catch (Exception e) {
				log.error(e, e);
				mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
				return mav;
			}
		} else {
			/** ZigBee STS METER */
			String mcuSysId = validateMap.get("mcuSysId").toString();
			Meter meter = (Meter) validateMap.get("meter");
			String mdsId = meter.getMdsId();

			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			cmd = cmdZigbeeSTS + cmd;
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());

			mav.addObject("rtnStr", executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas));
		}

		return mav;
	}

	/**
	 * cmdSetSTSSetup (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @param stsNumber
	 * @param kct1
	 * @param kct2
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetSTSSetup")
	public ModelAndView cmdSetSTSSetup(String target, String stsNumber, String kct1, String kct2) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		String modemType = (String) validateMap.get("modemType");
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String cmd = "cmdSetSTSSetup";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		/** GPRS STS METER */
		if (modemType == ModemType.MMIU.toString()) {
			try {
				List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
				Map<String, String> paramMap = new HashMap<String, String>();
				paramMap.put("String", DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmm"));
				paramList.add(paramMap);
				paramMap = new HashMap<String, String>();
				paramMap.put("string", kct1);
				paramList.add(paramMap);
				paramMap = new HashMap<String, String>();
				paramMap.put("string", kct2);
				paramList.add(paramMap);
				paramMap = new HashMap<String, String>();
				paramMap.put("string", stsNumber);
				paramList.add(paramMap);

				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, paramList, TimeUtil.getCurrentTime());

				// ECGSTSLog 저장
				Meter meter = (Meter) validateMap.get("meter");
				String mdsId = meter.getMdsId();
				String createDate = DateTimeUtil.getDateString(new Date());
				if (mdsId != null) {
					createEcgStsLog(mdsId, cmd, createDate, null, null, null, null, null, null, null, null, null,
							new int[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, null,
							null, 100, null, trId, DateTimeUtil.getDateString(new Date()), null, null, null, null, null,
							null, null, null, null, null, null, null, null, stsNumber, kct1, kct2, null, null);
				}

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 상태가 바뀌는 시간을 기다려주기 위해 1분 sleep
					Thread.sleep(60000);
					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);

					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success: Command Result[" + cmd + "]");
						mav.addObject("rtnStr", "Done: Command Result[" + cmd + "]");
						EcgSTSLogPk stsPk = new EcgSTSLogPk();
						stsPk.setAsyncTrId(trId);
						stsPk.setCmd(cmd);
						stsPk.setMeterNumber(mdsId);
						stsPk.setCreateDate(createDate);
						EcgSTSLog stsLog = stslogDao.get(stsPk);

						String resultMsg = "Done:Send SMS Command(" + cmd + ").\n";
						int rtnMode = stsLog.getResult();
						Supplier supplier = supplierDao.get(meter.getSupplierId());

						String resultDate = stsLog.getResultDate() == null ? null
								: TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(stsLog.getResultDate()),
										supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter());
						if (rtnMode == 0) {
							resultMsg += "Result : SUCCESS\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						} else if (rtnMode == 1) {
							String failReason = stsLog.getFailReason() == null ? "" : stsLog.getFailReason();
							resultMsg += "Result : FAIL\n";
							resultMsg += "Result Date : " + resultDate + "\n";
							resultMsg += "Fail Reason : " + failReason + "\n";
						} else if (rtnMode == 100) {
							resultMsg += "Result : NOT RESPONSE\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						} else {
							resultMsg += "Result : UnKnown\n";
							resultMsg += "Result Date : " + resultDate + "\n";
						}

						mav.addObject("rtnMsg", resultMsg);
					}
				}

			} catch (Exception e) {
				log.error(e, e);
				mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
				return mav;
			}
		} else {
			/** ZigBee STS METER */
			String mcuSysId = validateMap.get("mcuSysId").toString();
			Meter meter = (Meter) validateMap.get("meter");
			String mdsId = meter.getMdsId();

			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			cmd = cmdZigbeeSTS + cmd;
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());
			datas.add("date," + DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmm"));
			datas.add("kct1," + kct1);
			datas.add("kct2," + kct2);
			datas.add("stsNumber," + stsNumber);

			Map<String, Object> map = new HashMap<String, Object>();
			map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
			String successMessage = "";
			String result = map.get("result").toString();

			if (result.equals("fail")) {
				mav.addObject("rtnStr", map.get("reason").toString());
				mav.addObject("rtnMsg", null);
			} else if (result.equals("success")) {
				successMessage += "Result : SUCCESS\n";

				mav.addObject("rtnStr", null);
				mav.addObject("rtnMsg", successMessage);
			}
		}

		return mav;
	}

	/**
	 * cmdGetSTSSetup (STS 미터용)
	 * 
	 * 
	 * @param target
	 * @param stsNumber
	 * @param kct1
	 * @param kct2
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetSTSSetup")
	public ModelAndView cmdGetSTSSetup(String target) {
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		String modemType = (String) validateMap.get("modemType");
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String cmd = "cmdGetSTSSetup";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		/** GPRS STS METER */
		if (modemType == ModemType.MMIU.toString()) {
			try {
				// 비동기 내역 저장
				long trId = System.currentTimeMillis();
				saveAsyncCommandList(deviceSerial, trId, cmd, null, TimeUtil.getCurrentTime());

				// SMS 전송
				String messageId = "";
				String smsSkip = (String) validateMap.get("smsSkip");
				if ("true".equals(smsSkip) || smsSkip == "true") {
					// 문자 전송 없이, 대기만 한다. (STS 미터 정책)
					messageId = "smsSkip";
					boolean triggerUse = (CommandProperty.getProperty("tcp.trigger.use") == null) ? false
							: Boolean.parseBoolean(CommandProperty.getProperty("tcp.trigger.use"));
					if (triggerUse == true) {
						Modem modem = modemManager.getModem(deviceSerial);
						if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
							mav.addObject("rtnStr", CmdResultType.SocketError.getDesc());
							return mav;
						}
					} else {
						log.info("No use TCP Trigger");
					}
				} else {
					messageId = commonSendSMS((String) validateMap.get("smsClassPath"),
							(String) validateMap.get("mobileNo"), (String) validateMap.get("smsMsg"),
							(Properties) validateMap.get("prop"));
				}

				if ("fail".equals(messageId)) {
					mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
					log.debug("Request CMD Fail");
				} else if ("smsSkip".equals(messageId)) {
					mav.addObject("rtnStr",
							"Done : Please check the result on the [STS History], [Async Comm. History]  after a few minutes.");
					log.debug("Save AsyncCommandLog");
				} else {
					mav.addObject("rtnStr", CmdResultType.Waiting.getDesc());
					log.debug("Request CMD Success");

					Integer lastStatus = null;

					// 상태가 바뀌는 시간을 기다려주기 위해 1분 sleep
					Thread.sleep(60000);
					lastStatus = asyncCommandLogManager.getCmdStatus(deviceSerial, cmd);
					lastStatus = TR_STATE.Success.getCode();
					lastStatus = TR_STATE.Success.getCode();
					if (TR_STATE.Success.getCode() != lastStatus) {
						mav.addObject("rtnStr", CmdResultType.CommError.getDesc());
						log.debug("Fail : Command Result[" + cmd + "]");
					} else {
						log.debug("Success : Command Result[" + cmd + "]");
						mav.addObject("rtnStr", "Done : Command Result[" + cmd + "]");
						Meter meter = (Meter) validateMap.get("meter");
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("cmd", cmd);
						map.put("meterNumber", meter.getMdsId());
						map.put("trId", trId);
						List<EcgSTSLog> stsLogList = stslogDao.getEcgSTSLog(map);
						EcgSTSLog stsLog = null;
						if (stsLogList.size() > 0) {
							stsLog = stsLogList.get(0);
						}

						String resultMsg = "Done:Send SMS Command(" + cmd + ").\n";
						Integer rtnMode = null;
						if (stsLog != null) {
							rtnMode = stsLog.getResult();
							Supplier supplier = supplierDao.get(meter.getSupplierId());
							String resultDate = stsLog.getResultDate() == null ? null
									: TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(stsLog.getResultDate()),
											supplier.getLang().getCode_2letter(),
											supplier.getCountry().getCode_2letter());

							if (rtnMode == 0) {
								String stsNumber = stsLog.getStsNumber() == null ? "" : stsLog.getStsNumber();
								String kct1 = stsLog.getKct1() == null ? "" : stsLog.getKct1();
								String kct2 = stsLog.getKct2() == null ? "" : stsLog.getKct2();
								resultMsg += "Result : SUCCESS\n";
								resultMsg += "Result Date : " + resultDate + "\n";
								resultMsg += "STS Number : " + stsNumber + "\n";
								// STS module 프로토콜에 따르면 KCT를 반환하지 않음.
								// resultMsg += "KCT1 : " + kct1+"\n";
								// resultMsg += "KCT2 : " + kct2+"\n";
							} else if (rtnMode == 1) {
								String failReason = stsLog.getFailReason() == null ? "" : stsLog.getFailReason();
								resultMsg += "Result : FAIL\n";
								resultMsg += "Result Date : " + resultDate + "\n";
								resultMsg += "Fail Reason : " + failReason + "\n";
							}
						}
						StringBuffer resultSb = new StringBuffer();

						List<AsyncCommandResult> acplist = asyncCommandLogManager.getCmdResults(deviceSerial, trId,
								null);
						if (acplist != null && acplist.size() > 0) {
							StringBuilder sb = new StringBuilder();
							for (AsyncCommandResult result : acplist) {
								if (result.getResultType().contains("result")) {
									sb.append(result.getResultValue() + "\n");
									log.info("\n" + result.getResultValue());
								}
							}
							resultSb.append(sb.toString());
						} else if (rtnMode == null) {
							// resultMsg += "Result : NOT RESPONSE\n";
						}

						mav.addObject("rtnMsg", resultMsg);
					}
				}

			} catch (Exception e) {
				log.error(e, e);
				mav.addObject("rtnStr", "FAIL : Send SMS Fail.");
				return mav;
			}
		} else {
			/** ZigBee STS METER */
			String mcuSysId = validateMap.get("mcuSysId").toString();
			Meter meter = (Meter) validateMap.get("meter");
			String mdsId = meter.getMdsId();

			List<java.lang.String> datas = new ArrayList<java.lang.String>();
			cmd = cmdZigbeeSTS + cmd;
			datas.add("cmd," + cmd);
			datas.add("meterId," + mdsId);
			datas.add("modemType," + ModemType.ZigBee.toString());

			Map<String, Object> map = new HashMap<String, Object>();
			map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
			String successMessage = "";
			String result = map.get("result").toString();

			if (result.equals("fail")) {
				mav.addObject("rtnStr", map.get("reason").toString());
				mav.addObject("rtnMsg", null);
			} else if (result.equals("success")) {
				Supplier supplier = supplierDao.get(meter.getSupplierId());
				String dateTime = TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(map.get("date").toString()),
						supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter());

				successMessage += "Result : SUCCESS\n";
				successMessage += "Date : " + dateTime + "\n";
				successMessage += "STS Number : " + map.get("stsNumber").toString() + "\n";

				mav.addObject("rtnStr", null);
				mav.addObject("rtnMsg", successMessage);
			}

		}

		return mav;
	}

	/** Create a tunnel for HES-DCU-MODEM-SUNI STS Module */
	public Map<String, Object> createZigbeeTunnel(String mcuSysId, String modemEuiId) {
		final int MSG_TIMEOUT = 60;
		final int TUNNEL_TIMEOUT = 100;

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", false);

		int portNumber = 0;
		String rtnStr = "Create Zigbee Tunnel - FAIL";

		// if (!commandAuthCheck(loginId, CommandType.DeviceRead, "110.4.1")) {
		// mav.addObject("status", status.name());
		// mav.addObject("rtnStr", CmdResultType.PermissionError.getDesc());
		// return mav;
		// }

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

	/** Delete tunnel for HES-DCU-MODEM-SUNI STS Module */
	public Boolean deleteZigbeeTunnel(String mcuSysId, String modemEuiId) {
		// if (!commandAuthCheck(loginId, CommandType.DeviceRead, "110.4.1")) {
		// mav.addObject("status", status.name());
		// mav.addObject("rtnStr", CmdResultType.PermissionError.getDesc());
		// return mav;
		// }

		try {
			cmdOperationUtil.cmdDeleteTunnel2(mcuSysId, modemEuiId);
			return true;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		}
	}

	/** Retrieve a tunnel list for HES-DCU-MODEM-SUNI STS Module */
	public String getZigbeeTunnelList(String mcuSysId, String[] modemEuiId) {
		log.debug("Get Zigbee Tunnel List[mcuSysId = " + mcuSysId + "modemEuiId = " + modemEuiId[0]);
		String rtnStr = "Get Zigbee Tunnel List - FAIL";

		// 권한체크 필요함
		// if (!commandAuthCheck(loginId, CommandType.DeviceRead, "110.4.1")) {
		// mav.addObject("status", status.name());
		// mav.addObject("rtnStr", CmdResultType.PermissionError.getDesc());
		// return mav;
		// }

		StringBuffer resultString = new StringBuffer("");
		try {
			List<TunnelListInfo> tunnelList = cmdOperationUtil.cmdGetTunnelList(mcuSysId, modemEuiId);
			if (tunnelList != null && tunnelList.size() > 0) {
				for (int i = 0; i < tunnelList.size(); i++) {
					resultString.append("<" + i + ">");
					resultString.append(tunnelList.get(i).toString());
				}

				return resultString.toString();
			}

		} catch (Exception ex) {
			rtnStr = "Get Zigbee Tunnel List - Exception[" + ex.getMessage() + "]";
			return rtnStr;
		}

		return rtnStr;
	}

	/**
	 * 
	 * STS 미터용 validate체크 (STS 미터용)
	 * 
	 * @param meter.id
	 * @return
	 */
	private Map<String, Object> commonSTSValidate(String meterId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", true);

		try {
			if (meterId == null || "".equals(meterId)) {
				resultMap.put("result", false);
				resultMap.put("errorMessage", "FAIL : Meter ID null!");
				return resultMap;
			}

			Meter meter = meterManager.getMeter(Integer.parseInt(meterId));

			if (meter == null || "".equals(meter.getId()) || meter.getModemId() == null) {
				resultMap.put("result", false);
				resultMap.put("errorMessage", "FAIL : Meter or Modem is not in the system!");
				return resultMap;
			} else {
				resultMap.put("meter", meter);
			}

			Properties prop = new Properties();
			prop.load(getClass().getClassLoader().getResourceAsStream("command.properties"));
			String ipAddr = prop.getProperty("GG.sms.ipAddr") == null ? "" : prop.getProperty("GG.sms.ipAddr").trim();
			String port = prop.getProperty("GG.sms.port") == null ? "" : prop.getProperty("GG.sms.port").trim();
			String smsClassPath = prop.getProperty("smsClassPath");
			String smsSkip = prop.getProperty("GG.sms.stsSkip") == null ? "false"
					: prop.getProperty("GG.sms.stsSkip").trim();
			Map<String, Object> condition = new HashMap<String, Object>();
			String modemType = meter.getModem().getModemType().toString();
			String modelName = meter.getModel().getName();
			String vendorName = meter.getModel().getDeviceVendor().getName();

			resultMap.put("modelName", modelName);
			resultMap.put("vendorName", vendorName);

			/** GPRS STS METER */
			if (ModemType.MMIU.toString() == modemType) {
				resultMap.put("modemType", ModemType.MMIU.toString());

				condition.put("modemId", meter.getModemId());
				condition.put("modemType", ModemType.MMIU.toString());

				MMIU mmiu = null;
				String OID = "244.0.0";

				try {
					mmiu = (MMIU) modemManager.getModemByType(condition);
					if (mmiu == null || "".equals(mmiu.getDeviceSerial()) || mmiu.getDeviceSerial() == null) {
						resultMap.put("result", false);
						resultMap.put("errorMessage", CmdResultType.NullTarget.getDesc());
						return resultMap;
					} else {
						resultMap.put("deviceSerial", mmiu.getDeviceSerial());
					}
				} catch (Exception e) {
					log.error(e, e);
					resultMap.put("result", false);
					resultMap.put("errorMessage", CmdResultType.NullTarget.getDesc());
					return resultMap;
				}

				// SMS sts skip - 17.03.29 이후 ECG STS F/W에서는 SMS 기능 대신 주기적 접속으로
				// 변경됨.
				// 문자를 보내지 않고, Async_Command_Log에 저장만 시키고 대기하는 방식을 적용하기 위해 사용.
				String mobileNo = mmiu.getPhoneNumber();
				if (mobileNo == null || "".equals(mobileNo)) {
					// smsSkip이 true이면 문자를 보내지 않으므로, 에러 처리할 필요 없다.
					if ("true".equals(smsSkip) || smsSkip == "true") {
						resultMap.put("mobileNo", "0000");
					} else {
						resultMap.put("result", false);
						resultMap.put("errorMessage", "FAIL : Phone number is empty!");
						return resultMap;
					}
				} else {
					resultMap.put("mobileNo", mobileNo);
				}

				if ("".equals(ipAddr) || "".equals(port)) {
					resultMap.put("result", false);
					resultMap.put("errorMessage", "FAIL : Invalid Ip Address or port!");
					return resultMap;
				} else {
					resultMap.put("ipAddr", ipAddr);
					resultMap.put("port", port);
					resultMap.put("smsClassPath", smsClassPath);
					resultMap.put("smsSkip", smsSkip);
					resultMap.put("prop", prop);
					int seq = new Random().nextInt(100) & 0xFF;
					String smsMsg = cmdMsg((byte) seq, OID, ipAddr.replaceAll("\\.", ","), port);
					resultMap.put("smsMsg", smsMsg);
				}
			} else if (ModemType.ZigBee.toString() == modemType || ModemType.ZRU
					.toString() == modemType) { /** ZigBee STS METER */
				resultMap.put("modemType", ModemType.ZigBee.toString());

				condition.put("modemId", meter.getModemId());
				condition.put("modemType", ModemType.ZigBee.toString());

				try {
					String mcuSysId = meter.getModem().getMcu().getSysID();
					String deviceSerial = meter.getModem().getDeviceSerial();

					resultMap.put("mcuSysId", mcuSysId);
					resultMap.put("deviceSerial", deviceSerial);
				} catch (Exception e) {
					log.error(e, e);
					resultMap.put("result", false);
					String message = "FAIL : Target ID null. Meter Serial [" + meter.getMdsId() + "] "
							+ "Modem Serial [" + meter.getModem().getDeviceSerial() + "] " + "MCU ["
							+ meter.getModem().getMcu().getSysID() + "]";
					resultMap.put("errorMessage", message);
					return resultMap;
				}

				// STS 미터 체크
				DeviceModel model = meter.getModel();
				if (model == null || !model.getName().contains("STS")) {
					resultMap.put("result", false);
					resultMap.put("errorMessage", "FAIL : Invalid Model! (Model name does not contain 'STS' keyword.)");
					return resultMap;
				}

				if ("".equals(ipAddr) || "".equals(port)) {
					resultMap.put("result", false);
					resultMap.put("errorMessage", "FAIL : Invalid Ip Address or port!");
					return resultMap;
				} else {
					resultMap.put("ipAddr", ipAddr);
				}
			} else {
				log.error("Modem Type [" + modemType + "], This modem type does not support STS functionality.");
			}
		} catch (Exception e) {
			log.error(e, e);
			resultMap.put("result", false);
			resultMap.put("errorMessage", "FAIL : " + e.getMessage());
		}

		return resultMap;
	}

	private String commonSendSMS(String smsClassPath, String mobileNo, String smsMsg, Properties prop)
			throws Exception {
		SendSMS obj = (SendSMS) Class.forName(smsClassPath).newInstance();

		Method m = obj.getClass().getDeclaredMethod("send", String.class, String.class, Properties.class);
		String messageId = (String) m.invoke(obj, mobileNo.replace("-", "").trim(), smsMsg, prop);

		return messageId;
	}

	private void saveLog(String cmd, Map<String, Object> paramMap) {
		TransactionStatus txStatus = null;
		String createDate = DateTimeUtil.getDateString(new Date());

		try {
			txStatus = transactionManager.getTransaction(null);
			EcgSTSLog stslog = new EcgSTSLog();

			stslog.setAsyncTrId(Long.parseLong(createDate));
			stslog.setMeterNumber(paramMap.get("meterId").toString());
			stslog.setCreateDate(createDate);
			stslog.setGetDate(null);
			stslog.setResult(null);
			stslog.setFailReason(null);
			stslog.setCmd(paramMap.get("requestType").toString() + ", " + cmd);

			if (cmd.equals("cmdSetPaymentMode") || cmd.equals("cmdGetPaymentMode")) {
				stslog.setPayMode(Integer.parseInt(paramMap.get("paymentMode").toString()));
				stslog.setSwitchTime(paramMap.get("switchTime").toString());
			} else if (cmd.equals("cmdGetRemainingCredit")) {
				stslog.setRemainingCredit((double) paramMap.get("remainingCredit"));
			} else if (cmd.equals("cmdSetSTSToken") || cmd.equals("cmdGetSTSToken")) {
				stslog.setToken(paramMap.get("token").toString());
				stslog.setTokenDate(null);
			} else if (cmd.equals("cmdSetEmergencyCredit") || cmd.equals("cmdGetEmergencyCredit")) {
				double emergencyCredit = Double.parseDouble(paramMap.get("emergencyCredit").toString());
				stslog.setEmergencyCreditAmount(emergencyCredit);
				stslog.setEmergencyCreditMode(null);
			} else if (cmd.equals("cmdGetFriendlyCreditSchedule")) {
				double friendlyCredit = Double.parseDouble(paramMap.get("friendlyCredit").toString());
				String friendlyDays = paramMap.get("friendlyDays").toString();
				friendlyDays = friendlyDays.replace("friendlyDay=", "");

				String friendlyWeek = paramMap.get("friendlyWeek").toString();
				String friendlyTime = paramMap.get("friendlyTime").toString();
				String[] hhmm = friendlyTime.replace(" ", "").split("~");

				stslog.setFriendlyCreditAmount(friendlyCredit);
				stslog.setFriendlyDate(friendlyDays);
				stslog.setFriendlyDayType(friendlyWeek);
				stslog.setFriendlyFromHHMM(hhmm[0]);
				stslog.setFriendlyEndHHMM(hhmm[1]);

			} else if (cmd.equals("cmdSetFriendlyCreditSchedule")) {
				double friendlyCredit = Double.parseDouble(paramMap.get("friendlyCredit").toString());
				stslog.setFriendlyCreditAmount(friendlyCredit);
				stslog.setFriendlyDate(paramMap.get("friendlyDays").toString());
			} else if (cmd.equals("cmdGetPreviousMonthNetCharge") || cmd.equals("cmdGetSpecificMonthNetCharge")) {
				stslog.setNetChargeMonthCost((double) Integer.parseInt(paramMap.get("usedCost").toString()));
			} else if (cmd.equals("cmdSetTariff")) {
				stslog.setConsumption(paramMap.get("supplySize").toString());
				stslog.setFixedRate(paramMap.get("serviceCharge").toString());
				stslog.setCondRate2(paramMap.get("utilityRelief").toString());
				stslog.setCondRate1(paramMap.get("normalSubsidy").toString());
				stslog.setActiveEnergyCharge(paramMap.get("activeEnergyCharge").toString());
				stslog.setGovLey(paramMap.get("govLey").toString());
				stslog.setStreetLightLevy(paramMap.get("streetLightLevy").toString());
				stslog.setVat(paramMap.get("vat").toString());
				stslog.setLifeLineSubsidy(paramMap.get("lifeLineSubsidy").toString());
				stslog.setSwitchTime(paramMap.get("switchTime").toString());
			}

			stslogDao.add(stslog);
			stslogDao.flushAndClear();
			transactionManager.commit(txStatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txStatus != null)
				transactionManager.rollback(txStatus);
		}
	}

	private void createEcgStsLog(String mdsId, String cmd, String createDate, Integer payMode, String tokenDate,
			String token, Integer tariffMode, Integer tariffKind, Integer tariffCount, String tariffDate,
			String condLimit1, String condLimit2, int[] consumption, double[] fixedRate, double[] varRate,
			double[] condRate1, double[] condRate2, Integer emergencyCreditMode, Integer emergencyCreditDay,
			Integer result, String failReason, long asyncTrId, String asyncCreateDate, String remainingCreditDate,
			Double remainingCredit, String netChargeYyyymm, Integer netChargeMonthConsumption,
			Double netChargeMonthCost, String netChargeYyyymmdd, Integer netChargeDayConsumption,
			Double netChargeDayCost, String friendlyDate, int[] friendlyDayType, Integer fcMode,
			String[] friendlyFromHHMM, String[] friendlyEndHHMM, String stsNumber, String kct1, String kct2,
			Integer channel, Integer panId) {
		TransactionStatus txStatus = null;
		try {
			txStatus = transactionManager.getTransaction(null);

			EcgSTSLog stslog = new EcgSTSLog();
			stslog.setCmd(cmd);
			stslog.setAsyncTrId(asyncTrId);
			stslog.setMeterNumber(mdsId);
			stslog.setCreateDate(createDate);
			stslog.setGetDate(asyncCreateDate);
			stslog.setResult(result);
			stslog.setFailReason(failReason);
			if (payMode != null) {
				stslog.setPayMode(payMode);
			}
			stslog.setTokenDate(tokenDate);
			stslog.setToken(token);
			stslog.setSeq(0);
			if (emergencyCreditMode != null) {
				stslog.setEmergencyCreditMode(emergencyCreditMode);
			}
			if (emergencyCreditDay != null) {
				stslog.setEmergencyCreditDay(emergencyCreditDay);
			}
			stslog.setResultDate(stslog.getCreateDate());
			if (tariffMode != null) {
				stslog.setTariffMode(tariffMode);
			}
			if (tariffKind != null) {
				stslog.setTariffKind(tariffKind);
			}
			if (tariffCount != null) {
				stslog.setTariffCount(tariffCount);
			}
			stslog.setTariffDate(tariffDate);
			stslog.setRemainingCreditDate(remainingCreditDate);
			if (remainingCredit != null) {
				stslog.setRemainingCredit(remainingCredit);
			}

			stslog.setNetChargeYyyymm(netChargeYyyymmdd);
			if (netChargeMonthConsumption != null) {
				stslog.setNetChargeMonthConsumption(netChargeMonthConsumption);
			}
			if (netChargeMonthCost != null) {
				stslog.setNetChargeMonthCost(netChargeMonthCost);
			}
			stslog.setNetChargeYyyymmdd(netChargeYyyymmdd);
			if (netChargeDayConsumption != null) {
				stslog.setNetChargeDayConsumption(netChargeDayConsumption);
			}
			if (netChargeDayCost != null) {
				stslog.setNetChargeDayCost(netChargeDayCost);
			}
			if (stsNumber != null) {
				stslog.setStsNumber(stsNumber);
			}
			if (kct1 != null) {
				stslog.setKct1(kct1);
			}
			if (kct2 != null) {
				stslog.setKct2(kct2);
			}
			if (channel != null) {
				stslog.setChannel(channel);
			}
			if (panId != null) {
				stslog.setPanId(panId);
			}
			if (condLimit1 != null) {
				stslog.setCondLimit1(condLimit1);
			}
			if (condLimit2 != null) {
				stslog.setCondLimit2(condLimit2);
			}

			if (consumption != null && fixedRate != null && varRate != null && condRate1 != null && condRate2 != null) {
				String s_consumption = "";
				String s_fixedRate = "";
				String s_varRate = "";
				String s_condRate1 = "";
				String s_condRate2 = "";
				for (int i = 0; i < consumption.length; i++) {
					if (i != 0) {
						s_consumption += ",";
						s_fixedRate += ",";
						s_varRate += ",";
						s_condRate1 += ",";
						s_condRate2 += ",";
					}
					s_consumption += consumption[i];
					s_fixedRate += fixedRate[i];
					s_varRate += varRate[i];
					s_condRate1 += condRate1[i];
					s_condRate2 += condRate2[i];
				}
				stslog.setConsumption(s_consumption);
				stslog.setFixedRate(s_fixedRate);
				stslog.setVarRate(s_varRate);
				stslog.setCondRate1(s_condRate1);
				stslog.setCondRate2(s_condRate2);
			}

			stslog.setFriendlyDate(friendlyDate);
			if (fcMode != null) {
				stslog.setFcMode(fcMode);
			}

			if (friendlyDayType != null && friendlyDayType.length > 0) {
				String dayType = "";

				for (int i = 0; i < friendlyDayType.length; i++) {
					if (i != 0)
						dayType += ",";
					dayType += friendlyDayType[i];
				}
				stslog.setFriendlyDayType(dayType);
			}
			if (friendlyFromHHMM != null && friendlyEndHHMM != null) {
				String fromHHMM = "";
				String endHHMM = "";
				for (int i = 0; i < friendlyFromHHMM.length; i++) {
					if (i != 0) {
						fromHHMM += ",";
						endHHMM += ",";
					}
					fromHHMM += friendlyFromHHMM[i];
					endHHMM += friendlyEndHHMM[i];
				}

				stslog.setFriendlyFromHHMM(fromHHMM);
				stslog.setFriendlyEndHHMM(endHHMM);
			}
			stslogDao.add(stslog);
			stslogDao.flushAndClear();
			transactionManager.commit(txStatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txStatus != null)
				transactionManager.rollback(txStatus);
		}
	}

	/*
	 * Remaining Credit 실행 후 현재 잔액에 대한 PrepaymentLog를 생성하고 계약 정보를 갱신한다.
	 */
	private void updateCurrentCredit(String mdsId, String remainingCreditDate, double remainingCredit) {
		log.debug("updateCurrentCredit Start");
		Operator operator = operatorManager.getOperatorByLoginId("admin");
		TransactionStatus txstatus = null;

		try {
			txstatus = transactionManager.getTransaction(null);
			Meter meter = meterManager.getMeter(mdsId);
			Contract contract = meter.getContract();

			if (contract != null) {
				contract.setCurrentCredit(remainingCredit);
				contractManager.updateContract(contract);

				PrepaymentLog prelog = new PrepaymentLog();
				// Prepaymentlog의 ID 생성방법 변경(id+시스템시간)
				prelog.setId(Long.parseLong(Integer.toString(contract.getId()) + Long.toString(System.currentTimeMillis())));
				prelog.setOperator(operator);
				prelog.setContract(contract);
				prelog.setCustomer(meter.getCustomer());
				prelog.setBalance(remainingCredit);
				prelog.setLastTokenDate(remainingCreditDate);
				prelog.setLocation(contract.getLocation());

				prepaymentLogManager.addPrepaymentLog(prelog);
			}

			transactionManager.commit(txstatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txstatus != null)
				transactionManager.rollback(txstatus);
		}
	}
	
	private void updatePaymodeInfo(String mdsId, String paymentMode) {
		log.debug("updateCurrentCredit Start");
		Operator operator = operatorManager.getOperatorByLoginId("admin");
		TransactionStatus txstatus = null;

		try {
			txstatus = transactionManager.getTransaction(null);
			Meter meter = meterManager.getMeter(mdsId);
			Contract contract = meter.getContract();

			if (contract != null) {
				
				if (paymentMode.equals("04")) { 
					contract.setChargeAvailable(true);
					contract.setCreditType((Code)codeManager.getCodeByName("prepay"));
				} else if (paymentMode.equals("08")) { 
					contract.setChargeAvailable(false);
					contract.setCreditType((Code)codeManager.getCodeByName("postpay"));
				}
				
				contractManager.updateContract(contract);
			}

			transactionManager.commit(txstatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txstatus != null)
				transactionManager.rollback(txstatus);
		}
	}

	private void saveAsyncCommandList(String deviceSerial, long trId, String cmd, List<Map<String, String>> paramList,
			String currentTime) {
		AsyncCommandLog asyncCommandLog = new AsyncCommandLog();
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
		asyncCommandLogManager.add(asyncCommandLog);
		Integer num = 0;
		if (paramList != null && paramList.size() > 0) {
			// parameter가 존재할 경우.
			Integer maxNum = asyncCommandLogManager.getParamMaxNum(deviceSerial, trId);
			if (maxNum != null)
				num = maxNum + 1;

			for (int i = 0; i < paramList.size(); i++) {
				Map<String, String> param = paramList.get(i);
				Iterator<String> iter = param.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();

					AsyncCommandParam asyncCommandParam = new AsyncCommandParam();
					asyncCommandParam.setMcuId(deviceSerial);
					asyncCommandParam.setNum(num);
					asyncCommandParam.setParamType(key);
					asyncCommandParam.setParamValue((String) param.get(key));
					asyncCommandParam.setTrId(trId);

					asyncCommandLogManager.addParam(asyncCommandParam);
					num += 1;
				}
			}
		}
	}

	private String cmdMsg(byte seq, String oid, String ip, String port) {
		int sequence = (int) (seq & 0xFF);
		String smsMsg = "NT,";
		if (sequence >= 10 && sequence < 100) {
			smsMsg += "0" + sequence;
		} else if (sequence < 10) {
			smsMsg += "00" + sequence;
		} else {
			smsMsg += "" + sequence;
		}
		smsMsg += ",Q,B," + oid + "," + ip + "," + port;

		return smsMsg;
	}

	public boolean cmdTCPTrigger(String cmd, String ipAddr) throws Exception {
		log.info("[cmdTCPTrigger] Source Command: " + cmd + " IP: " + ipAddr);
		boolean isConnect = false;
		try {
			isConnect = cmdOperationUtil.cmdTCPTrigger(cmd, ipAddr);
		} catch (Exception e) {
			log.error(e, e);
			throw new Exception(e.toString());
		}
		return isConnect;
	}

	/**
	 * cmdTCPRetry STS미터에 TCP연결 재시도(STS 미터용) -> OPF-255
	 * 
	 * @param
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdTCPRetry")
	public ModelAndView cmdTCPRetry(String target) {

		ModelAndView mav = new ModelAndView("jsonView");
		String cmd = "cmdTCPRetry";

		try {
			Map<String, Object> validateMap = commonSTSValidate(target);
			Boolean validateCheck = (Boolean) validateMap.get("result");
			if (!validateCheck) {
				mav.addObject("rtnStr", validateMap.get("errorMessage"));
				return mav;
			}

			String deviceSerial = (String) validateMap.get("deviceSerial");
			Modem modem = modemManager.getModem(deviceSerial);

			if (!modem.getModemType().equals(ModemType.MMIU)) {
				mav.addObject("rtnStr", "This function is only available for GPRS STS meters.");

				return mav;
			}

			if (!cmdTCPTrigger(cmd, modem.getIpAddr())) {
				mav.addObject("rtnStr", "FAIL : Fail to create TCP socket.");
			} else {
				mav.addObject("rtnStr", "SUCCESS");
			}

		} catch (Exception e) {
			log.error(e, e);
			mav.addObject("rtnStr", "FAIL : Exception.");
			return mav;
		}

		return mav;
	}

	private Map<String, Object> executeZigBeeSTSByPass(String mcuSysId, String deviceSerial,
			List<java.lang.String> datas) {
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

	private String makeToken(String meterId, String initArrears) {
		String rtnStr = null;

		try {
			Meter meter = meterManager.getMeter(Integer.parseInt(meterId));

			// Credit 기준으로 작성
			String meterTypeCode = meter.getMeterType().getCode();
			String subClass = "4"; // default 4
			if ("1.3.1.1".equals(meterTypeCode)) {
				subClass = "4";
			} else if ("1.3.1.2".equals(meterTypeCode)) {
				subClass = "5";
			}

			// STS Number가 IhdId컬럼에 저장되어 있다
			String DSN = "";
			if (meter.getIhdId() != null && (meter.getIhdId().length() == 8 || meter.getIhdId().length() == 11)) {
				DSN = meter.getIhdId();
			} else {
				rtnStr = "FAIL : Cannot find STS Number.";
				return rtnStr;
			}

			Properties prop = new Properties();
			prop.load(getClass().getClassLoader().getResourceAsStream("command.properties"));
			String stsBaseUrl = prop.getProperty("sts.baseUrl");
			String SGC = prop.getProperty("sts.sgcNumber") == null ? "" : prop.getProperty("sts.sgcNumber");
			Double credit = 0.0;

			if (initArrears == null) {
				credit = prop.getProperty("prepay.init.credit") == null ? null
						: Double.parseDouble(prop.getProperty("prepay.init.credit"));
			} else {
				credit = Double.parseDouble(initArrears);

				if (credit == null || credit < 0) {
					return "FAIL : Invalid amount of arrears. ";
				}
			}

			// Prepay Initical Credit 및 STS접속 속성이 설정되지 않으면 에러 메시지 리턴
			if (credit == null || stsBaseUrl == null || SGC == null) {
				return "FAIL : Options for Initial Credit are not loaded. ";
			}

			String TCT = "02";
			String EA = "07";
			String tokenDate = DateTimeUtil.getDateString(new Date());
			String DOE = STSToken.getDOE(tokenDate);
			String MfrCode = "96";
			String TI = "01";
			Contract contract = meter.getContract();
			String KRN = "1";
			String idRecord = STSToken.getIdRecord(TCT, EA, SGC, KRN, DSN, DOE, MfrCode, TI);

			// value format : A decimal integer greater than 0.
			// base currency units : 0.01, R50.01 금액 충전시 value는 5001로 넣어야한다.
			Double currencyUnits = 0.01;
			Double chargedFormatCredit = Double.parseDouble(String.format("%.2f", credit));

			Double value = chargedFormatCredit / currencyUnits;
			String tokenValue = String.valueOf(Math.round(value));
			String sendURL = "";

			if (stsBaseUrl != null && !"".equals(stsBaseUrl)) {
				sendURL = stsBaseUrl.trim() + "VendCredit.ini?";
				sendURL += "meterId=" + idRecord + "&subclass=" + subClass + "&value=" + tokenValue;
				rtnStr = STSToken.makeToken(sendURL);
			}
		} catch (Exception e) {
			log.error(e, e);
			rtnStr = "FAIL : " + e;
			return rtnStr;
		}

		return rtnStr;
	}

	/**
	 * * cmdSetRFSetup (Zigbee STS 미터용)
	 * 
	 * 
	 * @param target
	 * @param channel
	 * @param panId
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdSetRFSetup")
	public ModelAndView cmdSetRFSetup(String target, String channel, String panId) {

		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		String modemType = (String) validateMap.get("modemType");
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String cmd = "cmdSetRFSetup";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		/** ZigBee STS METER */
		String mcuSysId = validateMap.get("mcuSysId").toString();
		Meter meter = (Meter) validateMap.get("meter");
		String mdsId = meter.getMdsId();

		List<java.lang.String> datas = new ArrayList<java.lang.String>();
		cmd = cmdZigbeeSTS + cmd;
		datas.add("cmd," + cmd);
		datas.add("meterId," + mdsId);
		datas.add("channel," + channel);
		datas.add("panId," + panId);

		Map<String, Object> map = new HashMap<String, Object>();
		map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
		String successMessage = "";
		String result = map.get("result").toString();

		if (result.equals("fail")) {
			mav.addObject("rtnStr", map.get("reason").toString());
			mav.addObject("rtnMsg", null);
		} else if (result.equals("success")) {
			successMessage += "Result : SUCCESS\n";

			mav.addObject("rtnStr", null);
			mav.addObject("rtnMsg", successMessage);
		}

		return mav;
	}

	/**
	 * cmdGetRFSetup (Zigbee STS 미터용)
	 * 
	 * 
	 * @param target
	 * @return
	 */
	@Transactional(value = "transactionManager", readOnly = false)
	@RequestMapping(value = "/gadget/device/command/cmdGetRFSetup")
	public ModelAndView cmdGetRFSetup(String target) {

		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> validateMap = commonSTSValidate(target);
		Boolean validateCheck = (Boolean) validateMap.get("result");
		String deviceSerial = (String) validateMap.get("deviceSerial");
		String cmd = "cmdGetRFSetup";

		if (!validateCheck) {
			mav.addObject("rtnStr", validateMap.get("errorMessage"));
			return mav;
		}

		/** ZigBee STS METER */
		String mcuSysId = validateMap.get("mcuSysId").toString();
		Meter meter = (Meter) validateMap.get("meter");
		String mdsId = meter.getMdsId();

		List<java.lang.String> datas = new ArrayList<java.lang.String>();
		cmd = cmdZigbeeSTS + cmd;
		datas.add("cmd," + cmd);
		datas.add("meterId," + mdsId);
		datas.add("modemType," + ModemType.ZigBee.toString());

		Map<String, Object> map = new HashMap<String, Object>();
		map = executeZigBeeSTSByPass(mcuSysId, deviceSerial, datas);
		String successMessage = "";
		String result = map.get("result").toString();

		if (result.equals("fail")) {
			mav.addObject("rtnStr", map.get("reason").toString());
			mav.addObject("rtnMsg", null);
		} else if (result.equals("success")) {
			successMessage += "Result : SUCCESS\n";
			successMessage += "Channel : " + map.get("channel").toString() + "\n";
			successMessage += "Pan ID : " + map.get("panId").toString() + "\n";

			mav.addObject("rtnStr", null);
			mav.addObject("rtnMsg", successMessage);
		}

		return mav;
	}

}