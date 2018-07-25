package com.aimir.mm.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import com.aimir.constants.CommonConstants.McuType;
import com.aimir.constants.CommonConstants.OperatorType;
import com.aimir.constants.CommonConstants.TR_OPTION;
import com.aimir.constants.CommonConstants.TR_STATE;
import com.aimir.dao.device.AsyncCommandLogDao;
import com.aimir.dao.device.AsyncCommandParamDao;
import com.aimir.dao.device.MeterDao;
import com.aimir.dao.device.ModemDao;
import com.aimir.dao.prepayment.VendorCasherDao;
import com.aimir.dao.system.CodeDao;
import com.aimir.dao.system.ContractChangeLogDao;
import com.aimir.dao.system.ContractDao;
import com.aimir.dao.system.DepositHistoryDao;
import com.aimir.dao.system.EcgSTSLogDao;
import com.aimir.dao.system.OperatorDao;
import com.aimir.dao.system.PrepaymentLogDao;
import com.aimir.dao.system.SupplierDao;
import com.aimir.mm.model.RequestAuthToken;
import com.aimir.model.device.AsyncCommandLog;
import com.aimir.model.device.AsyncCommandParam;
import com.aimir.model.device.Meter;
import com.aimir.model.prepayment.DepositHistory;
import com.aimir.model.prepayment.VendorCasher;
import com.aimir.model.system.Code;
import com.aimir.model.system.Contract;
import com.aimir.model.system.ContractChangeLog;
import com.aimir.model.system.EcgSTSLog;
import com.aimir.model.system.Operator;
import com.aimir.model.system.PrepaymentLog;
import com.aimir.model.system.Supplier;
import com.aimir.util.DateTimeUtil;
import com.aimir.util.DecimalUtil;
import com.aimir.util.STSToken;
import com.aimir.util.StringUtil;
import com.aimir.util.TimeLocaleUtil;
import com.aimir.util.TimeUtil;

@Service(value = "prepaymentManager")
public class PrepaymentManagerImpl implements PrepaymentManager{
	
	protected static Log log = LogFactory.getLog(PrepaymentManagerImpl.class);
	protected static String space = " ";
	
	@Autowired
	CodeDao codeDao;
	
	@Autowired
	SupplierDao supplierDao;
	
	@Autowired
	MeterDao meterDao;
	
	@Autowired
	ModemDao modemDao;
	
	@Autowired
	OperatorDao operatorDao;
	
    @Autowired
    ContractDao contractDao;
    
    @Autowired
    VendorCasherDao vendorCasherDao;
    
    @Autowired
    DepositHistoryDao depositHistoryDao;
    
    @Autowired
    PrepaymentLogDao prepaymentLogDao;
    
    @Autowired
    ContractChangeLogDao contractChangeLogDao;

    @Resource(name="transactionManager")
    HibernateTransactionManager transactionManager;
    
    @Autowired
    EcgSTSLogDao stslogDao;
    
    @Autowired
    AsyncCommandLogDao asyncCommandLogDao;
    
    @Autowired
    AsyncCommandParamDao asyncCommandParamDao;
    
	public Map<String, Object> customerRecharge(RequestAuthToken requestAuthToken, String meter_number, Double recharge_amount) {

		return customerBalanceChargingECG(requestAuthToken, meter_number, recharge_amount);
	}

    public Map<String,Object> vendorRecharge(RequestAuthToken requestAuthToken, String meter_number, String vendor_id, Double recharge_amount) {
    	
    	return vendorBalanceChargingECG(requestAuthToken, meter_number, vendor_id, recharge_amount);
    }
    
    private Map<String, Object> customerBalanceChargingECG(RequestAuthToken requestAuthToken, String meter_number, Double recharge_amount) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String,Object> smsInfo = new HashMap<String,Object>();
		TransactionStatus txStatus = null;
		
		try {
			Operator operator = operatorDao.getOperatorByLoginId(requestAuthToken.getUserName());
			Meter meter = meterDao.findByCondition("mdsId", meter_number);

			String casherId = meter.getCustomer().getLoginId();
			Integer contractId = meter.getContract().getId();
			String contractNumber = meter.getContract().getContractNumber();

			Double contractDemand = meter.getContract().getContractDemand();
			Integer operatorId = operator.getId();
			Double contractPrice = meter.getContract().getContractPrice();
			Integer payTypeId = codeDao.getCodeByName("Mobile").getId();

			Double paidAmount = recharge_amount;
			Double chargedCredit = recharge_amount;
			txStatus = transactionManager.getTransaction(null);
			Contract contract = null;
            
			// select Contract
			contract = contractDao.get(contractId);

			if (contract == null) {
				transactionManager.commit(txStatus);
				
				resultMap.put("responseStatus", false);
				resultMap.put("responseCode", "0");
				resultMap.put("responseCodeDescription", "fail : Invalid contract Info");
				return resultMap;
			}
            
			Operator updateOperator = operatorDao.get(operatorId);

			// OmniPower STS 모댈일 경우 토큰 발행
			Boolean isSTS = false;
			// String token = null;
			String token = "0"; // OPF-362
			if (meter != null && meter.getModel() != null) {
				String modelName = meter.getModel().getName();
				if ("OmniPower STS".equals(modelName)) {
					isSTS = true;
					/*
					 * Electricity : 0 Water : 1 Gas : 2 Time : 3 Credit(EM) : 4
					 * Credit(WM) : 5 Credit(GM) : 6 Credit(TIME) : 7
					 */
					
					// Credit 기준으로 작성
					String meterTypeCode = meter.getMeterType().getCode();
					String subClass = "4"; // default 4
					if ("1.3.1.1".equals(meterTypeCode)) {			// EnergyMeter
						subClass = "4";
					} else if ("1.3.1.2".equals(meterTypeCode)) {	// WaterMeter
						subClass = "5";
    				}
    				
					// value format : A decimal integer greater than 0.
					// base currency units : 0.01, R50.01 금액 충전시 value는 5001로 넣어야한다.
    				Double currencyUnits = 0.01;
    				Double chargedFormatCredit = Double.parseDouble( String.format( "%.2f" , chargedCredit ) );

    				Double value = chargedFormatCredit/currencyUnits;
    				String tokenValue = String.valueOf(Math.round(value));
    				
    				Properties prop = new Properties();
    				prop.load(getClass().getClassLoader().getResourceAsStream("config/command.properties"));
    				
					String stsBaseUrl = prop.getProperty("sts.baseUrl");
					String SGC = prop.getProperty("sts.sgcNumber") == null ? "" : prop.getProperty("sts.sgcNumber");
					String ipAddr = prop.getProperty("GG.sms.ipAddr") == null ? "" : prop.getProperty("GG.sms.ipAddr").trim();
    				String port = prop.getProperty("GG.sms.port") == null ? "" : prop.getProperty("GG.sms.port").trim();
    				String smsClassPath = prop.getProperty("smsClassPath");

					// STS Number를 IhdId에 저장한다.
					String DSN = "";
					if (meter.getIhdId() != null && (meter.getIhdId().length() == 8 || meter.getIhdId().length() == 11)) {
						DSN = meter.getIhdId();
					} else {
						transactionManager.rollback(txStatus);
						
						resultMap.put("responseStatus", false);
						resultMap.put("responseCode", "0");
						resultMap.put("responseCodeDescription", "fail : Invalid contract Info");
						return resultMap;
					}

					if (!"".equals(ipAddr) && !"".equals(port)) {
						smsInfo.put("modemId", meter.getModemId());
						smsInfo.put("ipAddr", ipAddr);
						smsInfo.put("port", port);
						smsInfo.put("smsClassPath", smsClassPath);
						smsInfo.put("prop", prop);
						int seq = new Random().nextInt(100) & 0xFF;
						String smsMsg = mmMsg((byte) seq, "244.0.0", ipAddr.replaceAll("\\.", ","), port);
						smsInfo.put("smsMsg", smsMsg);
					}
					
					String TCT = "02";
					String EA = "07";
					String tokenDate = DateTimeUtil.getDateString(new Date());
					String DOE = STSToken.getDOE(tokenDate);
					String MfrCode = "96";
					String TI = "01";
					String KRN = "1";
					String idRecord = STSToken.getIdRecord(TCT, EA, SGC, KRN, DSN, DOE, MfrCode, TI);

					// subclass=1이 clearCredit.
					if (stsBaseUrl != null && !"".equals(stsBaseUrl)) {
						String sendURL = stsBaseUrl.trim() + "VendCredit.ini?";
						sendURL += "meterId=" + idRecord + "&subclass=" + subClass + "&value=" + tokenValue;
						token = STSToken.makeToken(sendURL);
						log.info("Meter Serial Number [" + meter.getMdsId().toString() + " Token Number[" + token + "]");
					}

					// token : 20 digit
					if (token == null || token.length() != 20) {
						transactionManager.rollback(txStatus);
						
						resultMap.put("responseStatus", false);
						resultMap.put("responseCode", "0");
						resultMap.put("responseCodeDescription", "fail : Invalid contract Info");
						return resultMap;
					} else {
						smsInfo.put("token", token);
						
						// 비동기 내역 저장
						List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
						Map<String, String> paramMap = new HashMap<String, String>();
						// paramMap.put("token", token);
						paramMap.put("string", token);
						paramList.add(paramMap);

						String cmd = "cmdSetSTSToken";
						long trId = System.currentTimeMillis();
						saveAsyncCommandList(meter.getModem().getDeviceSerial(), trId, cmd, paramList,
								DateTimeUtil.getDateString(new Date()));

						String createDate = DateTimeUtil.getDateString(new Date());
						if (contract != null) {
							createEcgStsLog(contractNumber, cmd, createDate, tokenDate, token, 100, trId,
									DateTimeUtil.getDateString(new Date()));
						}
					}
				}
			}
    		
			String lastChargeDate = StringUtil.nullToBlank(contract.getLastTokenDate());
			Double preCredit = StringUtil.nullToDoubleZero(contract.getCurrentCredit());
			Double currentCredit = new BigDecimal(StringUtil.nullToZero(contract.getCurrentCredit()))
					.add(new BigDecimal(StringUtil.nullToZero(chargedCredit))).doubleValue();
    		
            Boolean isCutOff = false;    // 차단여부

            if(meter != null && meter.getModel() != null && currentCredit > 0d && meter.getMeterStatusCodeId() != null) {
	            // CommandOperationUtil 생성 후 개발 부분 Start
            	Code code = codeDao.get(meter.getMeterStatusCodeId());
            	 isCutOff = code.getCode().equals("1.3.3.4");
            }
            // CommandOperationUtil 생성 후 개발 부분 End
            
            Integer lastChargeCnt = new Integer(StringUtil.nullToZero(contract.getLastChargeCnt())) + 1;
            Code keyCode = null;
            // insert ContractChangeLog
            addContractChangeLog(contract, operator, "lastTokenDate", contract.getLastTokenDate(), DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"),null);
            addContractChangeLog(contract, operator, "chargedCredit", contract.getChargedCredit(), paidAmount,null);
            addContractChangeLog(contract, operator, "currentCredit", contract.getCurrentCredit(), currentCredit.toString(),null);
            addContractChangeLog(contract, operator, "lastChargeCnt", contract.getLastChargeCnt(), lastChargeCnt.toString(),null);

            // chargeCredit 안에 포함 init Credit이 포함되어 결제 되므로 contract에서는 contract Price를 null로 바꾼다.
            if(contractPrice != null && contractPrice != 0d) {
            	contract.setContractPrice(null);
            }
            
            contract.setLastTokenDate(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
            contract.setChargedCredit(paidAmount);
            contract.setCurrentCredit(currentCredit);
            contract.setLastChargeCnt(lastChargeCnt);
            contract.setCashPoint(operator.getCashPoint());

			PrepaymentLog prepaymentLog = new PrepaymentLog();
			Long transactionId = Long.parseLong(Integer.toString(contract.getId()) + Long.toString(System.currentTimeMillis())); 
			prepaymentLog.setId(transactionId);
			
			Double preArrears = contract.getCurrentArrears();
			prepaymentLog.setArrears(preArrears);
			prepaymentLog.setPreArrears(preArrears);
			prepaymentLog.setChargedArrears(0d);

            // 미터가 교체되고 처음 결제 되는 경우 로그에 미터 교체 비용관련 항목이 추가된다.
            Integer daysFromCharge;
            if(lastChargeDate != null  && !lastChargeDate.equals("") ) {
            	daysFromCharge = TimeUtil.getDayDuration(lastChargeDate, DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
            } else {
            	daysFromCharge = 0;
            }

            VendorCasher vendorCasher = vendorCasherDao.getByVendorCasherId(casherId, operator);
            Code keyType = codeDao.get(payTypeId);
            
            if(token != null && !"".equals(token)) {
            	prepaymentLog.setToken(token);
            }
            
			prepaymentLog.setVendorCasher(vendorCasher);
			prepaymentLog.setDaysFromCharge(daysFromCharge);
			prepaymentLog.setCustomer(contract.getCustomer());
			prepaymentLog.setContract(contract);
			prepaymentLog.setKeyType(keyCode);
			prepaymentLog.setChargedCredit(chargedCredit);
			prepaymentLog.setLastTokenDate(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
			prepaymentLog.setOperator(operator);
			Integer emergencyYn = null;

			if (contract.getEmergencyCreditAvailable() != null) {
				emergencyYn = (contract.getEmergencyCreditAvailable()) ? 1 : 0;
			}
			
			prepaymentLog.setEmergencyCreditAvailable(emergencyYn);
			prepaymentLog.setPowerLimit(contractDemand);
			prepaymentLog.setPreBalance(preCredit);
			prepaymentLog.setBalance(currentCredit);
			prepaymentLog.setLocation(contract.getLocation());
			prepaymentLog.setTariffIndex(contract.getTariffIndex());
			prepaymentLog.setPayType(keyType);

			prepaymentLogDao.add(prepaymentLog);
            log.info("prepaymentLog has been added");
            
			DepositHistory dh = new DepositHistory();
			dh.setOperator(updateOperator);
			dh.setContract(contract);
			dh.setCustomer(contract.getCustomer());
			dh.setMeter(meter);
			dh.setChangeDate(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
			dh.setChargeCredit(paidAmount);
			dh.setDeposit(updateOperator.getDeposit());
			dh.setPrepaymentLog(prepaymentLog);
			
			depositHistoryDao.add(dh);    
			
			// operator update
			operatorDao.update(updateOperator);
			log.info("operator update is completed");

			// update Contract
			contractDao.update(contract);

			log.info("contractInfo has been updated");
			log.info("depositHistory has been added");
			transactionManager.commit(txStatus);
			
			smsInfo.put("isSTS", isSTS);
			smsInfo.put("contract", contract);
			smsInfo.put("chargedCredit", chargedCredit);
			smsInfo.put("preCredit", preCredit);
			smsInfo.put("isCutOff", isCutOff);

			resultMap.put("responseStatus", true);
			resultMap.put("responseCode", space);
			resultMap.put("responseCodeDescription", space);

			// resultMap.put("transactionId", String.valueOf(prepaymentLog.getId()));
			resultMap.put("transactionId", transactionId);
			
			resultMap.put("customerName", meter.getCustomer().getName());
			resultMap.put("meterNumber", meter_number);

			resultMap.put("balanceAmount", currentCredit);
			resultMap.put("tokenNumber", token); // OPF-362
			
			mobileMoneySendSMS(smsInfo);

			return resultMap;

		} catch (Exception e) {
			log.error(e, e);

			if (txStatus != null && !txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}

			resultMap.put("responseStatus", false);
			resultMap.put("responseCode", "0");
			resultMap.put("responseCodeDescription", e.toString());
			return resultMap;
		}
    }

    private Map<String, Object> vendorBalanceChargingECG(RequestAuthToken requestAuthToken, String meter_number, String vendor_id, Double recharge_amount) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String,Object> smsInfo = new HashMap<String,Object>();
		TransactionStatus txStatus = null;

		try {
			Operator operator = operatorDao.getOperatorByLoginId(requestAuthToken.getUserName());
			Meter meter = meterDao.findByCondition("mdsId", meter_number);

			Boolean isVendor = operator.getRole().getName().equals("vendor");
			String casherId = meter.getCustomer().getLoginId();
			Integer contractId = meter.getContract().getId();
			String contractNumber = meter.getContract().getContractNumber();

			Double contractDemand = meter.getContract().getContractDemand();
			Integer operatorId = operator.getId();
			Double contractPrice = meter.getContract().getContractPrice();
			Integer payTypeId = codeDao.getCodeByName("Mobile").getId();

			Double paidAmount = recharge_amount;
			Double chargedCredit = recharge_amount;
			txStatus = transactionManager.getTransaction(null);
			Contract contract = null;
            
			// select Contract
			contract = contractDao.get(contractId);

			if (contract == null) {
				transactionManager.commit(txStatus);
				
				resultMap.put("responseStatus", false);
				resultMap.put("responseCode", "0");
				resultMap.put("responseCodeDescription", "fail : Invalid contract Info");
				return resultMap;
			}
            
			Operator updateOperator = operatorDao.get(operatorId);
			Double currentDeposit = updateOperator.getDeposit();

			if (isVendor && (currentDeposit == null || currentDeposit < paidAmount)) {
				// 잔고 부족
				transactionManager.rollback(txStatus);
				
				resultMap.put("responseStatus", false);
				resultMap.put("responseCode", "0");
				resultMap.put("responseCodeDescription", "fail : Invalid contract Info");
				return resultMap;
			}

			// OmniPower STS 모댈일 경우 토큰 발행
			Boolean isSTS = false;
			// String token = null;
			String token = "0"; // OPF-362
			if (meter != null && meter.getModel() != null) {
				String modelName = meter.getModel().getName();
				if ("OmniPower STS".equals(modelName)) {
					isSTS = true;
					/*
					 * Electricity : 0 Water : 1 Gas : 2 Time : 3 Credit(EM) : 4
					 * Credit(WM) : 5 Credit(GM) : 6 Credit(TIME) : 7
					 */
					
					// Credit 기준으로 작성
					String meterTypeCode = meter.getMeterType().getCode();
					String subClass = "4"; // default 4
					if ("1.3.1.1".equals(meterTypeCode)) {
						subClass = "4";
					} else if ("1.3.1.2".equals(meterTypeCode)) {
						subClass = "5";
    				}
    				
					// value format : A decimal integer greater than 0.
					// base currency units : 0.01, R50.01 금액 충전시 value는 5001로 넣어야한다.
    				Double currencyUnits = 0.01;
    				Double chargedFormatCredit = Double.parseDouble( String.format( "%.2f" , chargedCredit ) );

    				Double value = chargedFormatCredit/currencyUnits;
    				String tokenValue = String.valueOf(Math.round(value));
    				
    				Properties prop = new Properties();
					prop.load(getClass().getClassLoader().getResourceAsStream("config/command.properties"));
					String stsBaseUrl = prop.getProperty("sts.baseUrl");
					String SGC = prop.getProperty("sts.sgcNumber") == null ? "" : prop.getProperty("sts.sgcNumber");
					String ipAddr = prop.getProperty("GG.sms.ipAddr") == null ? "" : prop.getProperty("GG.sms.ipAddr").trim();
    				String port = prop.getProperty("GG.sms.port") == null ? "" : prop.getProperty("GG.sms.port").trim();
    				String smsClassPath = prop.getProperty("smsClassPath");

					// STS Number를 IhdId에 저장한다.
					String DSN = "";
					if (meter.getIhdId() != null && (meter.getIhdId().length() == 8 || meter.getIhdId().length() == 11)) {
						DSN = meter.getIhdId();
					} else {
						transactionManager.rollback(txStatus);
						
						resultMap.put("responseStatus", false);
						resultMap.put("responseCode", "0");
						resultMap.put("responseCodeDescription", "fail : Invalid contract Info");
						return resultMap;
					}
					
					if (!"".equals(ipAddr) && !"".equals(port)) {
    					smsInfo.put("modemId", meter.getModemId());
    					smsInfo.put("ipAddr", ipAddr);
    					smsInfo.put("port", port);
    					smsInfo.put("smsClassPath", smsClassPath);
    					smsInfo.put("prop", prop);
    					int seq = new Random().nextInt(100) & 0xFF;
    					String smsMsg = mmMsg((byte) seq, "244.0.0", ipAddr.replaceAll("\\.", ","), port);
    					smsInfo.put("smsMsg", smsMsg);
    				}

					String TCT = "02";
					String EA = "07";
					String tokenDate = DateTimeUtil.getDateString(new Date());
					String DOE = STSToken.getDOE(tokenDate);
					String MfrCode = "96";
					String TI = "01";
					String KRN = "1";
					String idRecord = STSToken.getIdRecord(TCT, EA, SGC, KRN, DSN, DOE, MfrCode, TI);

					// subclass=1이 clearCredit.
					if (stsBaseUrl != null && !"".equals(stsBaseUrl)) {
						String sendURL = stsBaseUrl.trim() + "VendCredit.ini?";
						sendURL += "meterId=" + idRecord + "&subclass=" + subClass + "&value=" + tokenValue;
						token = STSToken.makeToken(sendURL);
						log.info("Meter Serial Number [" + meter.getMdsId().toString() + " Token Number[" + token + "]");
					}

					// token : 20 digit
					if (token == null || token.length() != 20) {
						transactionManager.rollback(txStatus);
						
						resultMap.put("responseStatus", false);
						resultMap.put("responseCode", "0");
						resultMap.put("responseCodeDescription", "fail : Invalid contract Info");
						return resultMap;
					} else {
						smsInfo.put("token", token);
						
						// 비동기 내역 저장
						List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
						Map<String, String> paramMap = new HashMap<String, String>();
						// paramMap.put("token", token);
						paramMap.put("string", token);
						paramList.add(paramMap);

						String cmd = "cmdSetSTSToken";
						long trId = System.currentTimeMillis();
						saveAsyncCommandList(meter.getModem().getDeviceSerial(), trId, cmd, paramList,
								DateTimeUtil.getDateString(new Date()));

						String createDate = DateTimeUtil.getDateString(new Date());
						if (contract != null) {
							createEcgStsLog(contractNumber, cmd, createDate, tokenDate, token, 100, trId,
									DateTimeUtil.getDateString(new Date()));
						}
					}
				}
			}
    		
			String lastChargeDate = StringUtil.nullToBlank(contract.getLastTokenDate());
			Double preCredit = StringUtil.nullToDoubleZero(contract.getCurrentCredit());
			Double currentCredit = new BigDecimal(StringUtil.nullToZero(contract.getCurrentCredit()))
					.add(new BigDecimal(StringUtil.nullToZero(chargedCredit))).doubleValue();
    		
            Boolean isCutOff = false;    // 차단여부

            if(meter != null && meter.getModel() != null && currentCredit > 0d && meter.getMeterStatusCodeId() != null) {
	            // CommandOperationUtil 생성 후 개발 부분 Start
            	Code code = codeDao.get(meter.getMeterStatusCodeId());
            	 isCutOff = code.getCode().equals("1.3.3.4");
            }
            // CommandOperationUtil 생성 후 개발 부분 End
            
            Integer lastChargeCnt = new Integer(StringUtil.nullToZero(contract.getLastChargeCnt())) + 1;
            Code keyCode = null;
            // insert ContractChangeLog
            addContractChangeLog(contract, operator, "lastTokenDate", contract.getLastTokenDate(), DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"),null);
            addContractChangeLog(contract, operator, "chargedCredit", contract.getChargedCredit(), paidAmount,null);
            addContractChangeLog(contract, operator, "currentCredit", contract.getCurrentCredit(), currentCredit.toString(),null);
            addContractChangeLog(contract, operator, "lastChargeCnt", contract.getLastChargeCnt(), lastChargeCnt.toString(),null);

            // chargeCredit 안에 포함 init Credit이 포함되어 결제 되므로 contract에서는 contract Price를 null로 바꾼다.
            if(contractPrice != null && contractPrice != 0d) {
            	contract.setContractPrice(null);
            }
            
            contract.setLastTokenDate(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
            contract.setChargedCredit(paidAmount);
            contract.setCurrentCredit(currentCredit);
            contract.setLastChargeCnt(lastChargeCnt);
            contract.setCashPoint(operator.getCashPoint());

			PrepaymentLog prepaymentLog = new PrepaymentLog();
			Long transactionId = Long.parseLong(Integer.toString(contract.getId()) + Long.toString(System.currentTimeMillis())); 
			prepaymentLog.setId(transactionId);

			Double preArrears = contract.getCurrentArrears();
			prepaymentLog.setArrears(preArrears);
			prepaymentLog.setPreArrears(preArrears);
			prepaymentLog.setChargedArrears(0d);

            // 미터가 교체되고 처음 결제 되는 경우 로그에 미터 교체 비용관련 항목이 추가된다.
            Integer daysFromCharge;
            if(lastChargeDate != null  && !lastChargeDate.equals("") ) {
            	daysFromCharge = TimeUtil.getDayDuration(lastChargeDate, DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
            } else {
            	daysFromCharge = 0;
            }

            //VendorCasher vendorCasher = vendorCasherDao.findByCondition("vendorId", Integer.parseInt(vendor_id));
            VendorCasher vendorCasher = vendorCasherDao.getByVendorCasherId(casherId, operator);
            Code keyType = codeDao.get(payTypeId);
            
            if(token != null && !"".equals(token)) {
            	prepaymentLog.setToken(token);
            }
            
			prepaymentLog.setVendorCasher(vendorCasher);
			prepaymentLog.setDaysFromCharge(daysFromCharge);
			prepaymentLog.setCustomer(contract.getCustomer());
			prepaymentLog.setContract(contract);
			prepaymentLog.setKeyType(keyCode);
			prepaymentLog.setChargedCredit(chargedCredit);
			prepaymentLog.setLastTokenDate(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
			prepaymentLog.setOperator(operator);
			Integer emergencyYn = null;

			if (contract.getEmergencyCreditAvailable() != null) {
				emergencyYn = (contract.getEmergencyCreditAvailable()) ? 1 : 0;
			}
			
			prepaymentLog.setEmergencyCreditAvailable(emergencyYn);
			prepaymentLog.setPowerLimit(contractDemand);
			prepaymentLog.setPreBalance(preCredit);
			prepaymentLog.setBalance(currentCredit);
			prepaymentLog.setLocation(contract.getLocation());
			prepaymentLog.setTariffIndex(contract.getTariffIndex());
			prepaymentLog.setPayType(keyType);

			prepaymentLogDao.add(prepaymentLog);
            log.info("prepaymentLog has been added");
            
			DepositHistory dh = new DepositHistory();
			dh.setOperator(updateOperator);
			dh.setContract(contract);
			dh.setCustomer(contract.getCustomer());
			dh.setMeter(meter);
			dh.setChangeDate(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
			dh.setChargeCredit(paidAmount);
			dh.setDeposit(updateOperator.getDeposit());
			dh.setPrepaymentLog(prepaymentLog);
			
			depositHistoryDao.add(dh);    
			
			// operator update
			if (isVendor) {
				updateOperator.setDeposit(currentDeposit - paidAmount);
			}
			operatorDao.update(updateOperator);
			log.info("operator update is completed");

			// update Contract
			contractDao.update(contract);

			log.info("contractInfo has been updated");
			log.info("depositHistory has been added");
			transactionManager.commit(txStatus);

			smsInfo.put("isSTS", isSTS);
        	smsInfo.put("contract", contract);
        	smsInfo.put("chargedCredit", chargedCredit);
        	smsInfo.put("preCredit", preCredit);
        	smsInfo.put("isCutOff", isCutOff);
			
			resultMap.put("responseStatus", true);
			resultMap.put("responseCode", space);
			resultMap.put("responseCodeDescription", space);
			
			// resultMap.put("transactionId", String.valueOf(prepaymentLog.getId()));
			resultMap.put("transactionId", transactionId);
			
			resultMap.put("customerName", meter.getCustomer().getName());
			resultMap.put("meterNumber", meter_number);

			if (isVendor) {
				resultMap.put("customerBalanceAmount", currentCredit);
				resultMap.put("vendorBalanceAmount", currentDeposit - paidAmount);
				
				resultMap.put("vendorId", vendor_id);
				resultMap.put("tokenNumber", token); // OPF-362
			} else {
				resultMap.put("balanceAmount", currentCredit);
				resultMap.put("tokenNumber", token); // OPF-362
			}
			
			mobileMoneySendSMS(smsInfo);
			
			return resultMap;

		} catch (Exception e) {
			log.error(e, e);

			if (txStatus != null && !txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}

			resultMap.put("responseStatus", false);
			resultMap.put("responseCode", "0");
			resultMap.put("responseCodeDescription", e.toString());
			return resultMap;
		}
    }
    
	private void mobileMoneySendSMS(Map<String, Object> smsInfo) {
		try {
			Contract contract = (Contract) smsInfo.get("contract");
			Double amount = (Double) smsInfo.get("chargedCredit");
			Double preCurrentCredit = (Double) smsInfo.get("preCredit");
			Boolean isValid = (Boolean) smsInfo.get("isCutOff");
			String text = null;
			String mobileNo = null;
			
			Integer contractId = contract.getId();
			Map<String, Object> condition = new HashMap<String, Object>();
			condition.put("prepayCreditId", codeDao.getCodeIdByCode("2.2.1"));
			condition.put("emergencyICreditId", codeDao.getCodeIdByCode("2.2.2"));
			condition.put("smsYn", true);
			condition.put("contractId", contractId);
			List<Map<String, Object>> contractInfo = contractDao.getContractSMSYN(condition);
			
			if (contractInfo.size() > 0) {
				mobileNo = contractInfo.get(0).get("MOBILENO").toString().replace("-", "");
				Supplier supplier = supplierDao.get(contract.getSupplierId());
				DecimalFormat cdf = DecimalUtil.getDecimalFormat(supplier.getCd());
				Double renewCurrentCredit = preCurrentCredit + amount;

				if (isValid) {
					text = "Your balance is now available. The power is supplied again. \n"
							+ "Supply Type: " + contractInfo.get(0).get("SERVICETYPE") + "\n "
							+ "Charge Amount: " + amount.toString() + "\n "
						    + "Current Credit: " + cdf.format(renewCurrentCredit).toString();
				} else {
					String date = TimeLocaleUtil.getLocaleDate(TimeUtil.getCurrentDay(),
							supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter());

					text = "Customer: " + contractInfo.get(0).get("CUSTOMERNAME") + "\n"
							+ "Meter :" + contractInfo.get(0).get("METERID") + "\n" + date + " " +  
							"Charge Amount :" + amount.toString() + "\n" + date + " " + 
							"Current Credit: "+ cdf.format(renewCurrentCredit).toString();
				}
			}
			
			if (text != null && mobileNo != null) {
				CommonSMSSend(text, mobileNo, contractId, smsInfo);
			}
			
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	private void CommonSMSSend(String text, String mobileNo, Integer contractId, Map<String, Object> smsInfo) throws Exception {
		/*Boolean isSTS = (Boolean) smsInfo.get("isSTS");
		Boolean triggerUse = false;
		TCPTriggerClient tcpTriggerClient = new TCPTriggerClient();
		
		try {
			Properties prop = new Properties();
			prop.load(getClass().getClassLoader().getResourceAsStream("config/fmp.properties"));
			
			if (isSTS) {
				triggerUse = Boolean.parseBoolean(prop.getProperty("tcp.trigger.use"));
				text += "\n Token Number: " + smsInfo.get("token").toString();
				
				if (triggerUse == true) {
					String ipAddr = smsInfo.get("ipAddr").toString() != null ? smsInfo.get("ipAddr").toString() : null;
					
					if (!tcpTriggerClient.cmdTCPTrigger("cmdSetSTSToken", ipAddr)) {
						log.error("[cmdTCPTrigger] Fail to establish a TCP connection with modem");
						text += "\n * Notice: Please enter the token number on your meter.";
					}
				} else {
					log.info("No use TCP Trigger");
				}
			} 
			
			if (contractId != null) {
				log.info("SEND SMS - Phone Number[" + mobileNo + "]");
				log.info("#################### SMS TEXT ####################");
				log.info(text);
				log.info("##############################################");
				
				String smsClassPath = prop.getProperty("smsClassPath");
				log.info("smsClassPath : " + smsClassPath);
				
				SendSMS obj = (SendSMS) Class.forName(smsClassPath).newInstance();
				Method m = obj.getClass().getDeclaredMethod("send", String.class, String.class, Properties.class);
				String messageId = (String) m.invoke(obj, mobileNo, text, prop);
				log.info("FINISHED SMS");

				if (!"".equals(messageId)) {
					log.info("contractId [ " + contractId + "],	SMS messageId [" + messageId + "]");
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		}*/
	}
	
    private void saveAsyncCommandList(String deviceSerial, long trId, String cmd, List<Map<String, String>> paramList, String currentTime) throws Exception {
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
		asyncCommandLogDao.add(asyncCommandLog);
		Integer num = 0;
		if (paramList != null && paramList.size() > 0) {
			//parameter가 존재할 경우.
			Integer maxNum = asyncCommandParamDao.getMaxNum(deviceSerial, trId);
			if (maxNum != null)
				num = maxNum + 1;

			for (int i = 0; i < paramList.size(); i++) {
				Map<String,String> param = paramList.get(i);
				Iterator<String> iter = param.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();

					AsyncCommandParam asyncCommandParam = new AsyncCommandParam();
					asyncCommandParam.setMcuId(deviceSerial);
					asyncCommandParam.setNum(num);
					asyncCommandParam.setParamType(key);
					asyncCommandParam.setParamValue((String) param.get(key));
					asyncCommandParam.setTrId(trId);

					asyncCommandParamDao.add(asyncCommandParam);
					num += 1;
				}
			}
		}
	}
    
    private void createEcgStsLog(String meterNumber, String cmd, String createDate, String tokenDate, String token, Integer result, 
            long asyncTrId, String asyncCreateDate) {
		TransactionStatus txStatus = null;
		try {
			txStatus = transactionManager.getTransaction(null);
			
            EcgSTSLog stslog = new EcgSTSLog();
            stslog.setCmd(cmd);
            stslog.setAsyncTrId(asyncTrId);
            stslog.setMeterNumber(meterNumber);
            stslog.setCreateDate(createDate);
            stslog.setGetDate(asyncCreateDate);
            stslog.setResult(result);
            stslog.setTokenDate(tokenDate);
            stslog.setToken(token);
            stslog.setSeq(0);
            stslog.setResultDate(stslog.getCreateDate());
            stslogDao.add(stslog);
        	transactionManager.commit(txStatus);
		} catch (Exception e) {
			log.error(e, e);
			if (txStatus != null)
				transactionManager.rollback(txStatus);
		}
    }
	
    /**
     * method name : addContractChangeLog
     * method Desc : ContractChangeLog 에 데이터 insert
     *
     * @param contract
     * @param field
     * @param beforeValue
     * @param afterValue
     */
    private void addContractChangeLog(Contract contract, Operator operator, String field, Object beforeValue, Object afterValue, String type) {

        // ContractChangeLog Insert
        ContractChangeLog contractChangeLog = new ContractChangeLog();

        contractChangeLog.setContract(contract);
        contractChangeLog.setCustomer(contract.getCustomer());
        contractChangeLog.setStartDatetime(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));
        contractChangeLog.setChangeField(field);

        if (beforeValue == null) {
            contractChangeLog.setBeforeValue(null);
        } else {
        	if(type != null) {
        		contractChangeLog.setBeforeValue("["+type+"] "+StringUtil.nullToBlank(beforeValue));
        	} else {
        		contractChangeLog.setBeforeValue(StringUtil.nullToBlank(beforeValue));
        	}
        }

        if (afterValue == null) {
            contractChangeLog.setAfterValue(null);
        } else {
        	if(type != null) {
        		contractChangeLog.setAfterValue("["+type+"] "+StringUtil.nullToBlank(afterValue));
        	} else {
        		contractChangeLog.setAfterValue(StringUtil.nullToBlank(afterValue));
        	}
            
        }

        contractChangeLog.setOperator(operator);
        contractChangeLog.setWriteDatetime(DateTimeUtil.getCurrentDateTimeByFormat("yyyyMMddHHmmss"));

        contractChangeLogDao.add(contractChangeLog);
    }
    
    private String mmMsg(byte seq, String oid, String ip, String port) {
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
    
    public Map<String, Object> rechargeCancel(Long id, String loginId, String reason) {
		Map<String, Object> returnData = new HashMap<String, Object>();
		Boolean isSTS = false;
		Contract contract = null;
		Double totalAmount = 0d;
		String cancel_token_number = "0";
		Properties prop = new Properties();
		TransactionStatus txStatus = null;
		
		try {
			prop.load(getClass().getClassLoader().getResourceAsStream("config/command.properties"));
			String stsBaseUrl = prop.getProperty("sts.baseUrl");
			String SGC = prop.getProperty("sts.sgcNumber") == null ? "" : prop.getProperty("sts.sgcNumber");
			String isPartpaymentStr = prop.getProperty("partpayment.use");
			Boolean isPartpayment = (isPartpaymentStr == null || "".equals(isPartpaymentStr)) ? false : Boolean.parseBoolean(isPartpaymentStr);
			txStatus = transactionManager.getTransaction(null);
			PrepaymentLog prepayLog = prepaymentLogDao.get(id);
			Boolean isCancelled = prepayLog.getIsCanceled() == null ? false : prepayLog.getIsCanceled();

			// Cancel된 내역인지 다시 한번 확인
			if (!isCancelled) {
				Integer contractId = prepayLog.getContractId();
				contract = contractDao.get(contractId);
				Meter meter = contract.getMeter();

				// STS미터인 지 판변
				if (prepayLog.getToken() != null && meter.getModel() != null && "OmniPower STS".equals(meter.getModel().getName())) {
					Double chargedCredit = prepayLog.getChargedCredit() == null ? 0d : prepayLog.getChargedCredit();
					if (chargedCredit > 0) {
						isSTS = true;
						/*
						 * Electricity : 0 Water : 1 Gas : 2 Time : 3 Credit(EM)
						 * : 4 Credit(WM) : 5 Credit(GM) : 6 Credit(TIME) : 7
						 */

						String meterTypeCode = meter.getMeterType().getCode();
						String subClass = "4"; // default 4
						if ("1.3.1.1".equals(meterTypeCode)) {
							subClass = "4";
						} else if ("1.3.1.2".equals(meterTypeCode)) {
							subClass = "5";
						}
						// value format : A decimal integer greater than 0.
						// base currency units : 0.01, R50.01 금액 충전시 value는 5001로 넣어야한다.
						Double currencyUnits = 0.01;
						chargedCredit = Double.parseDouble(String.format("%.2f", chargedCredit));

						// for minus token * -1
						Double value = (-1 * chargedCredit) / currencyUnits;
						String tokenValue = String.valueOf(Math.round(value));

						// STS Number는 IhdId컬럼에 저장되어있다.
						String DSN = "";
						if (meter.getIhdId() != null && (meter.getIhdId().length() == 8 || meter.getIhdId().length() == 11)) {
							DSN = meter.getIhdId();
						} else {
							transactionManager.commit(txStatus);
							
							returnData.put("command_result", "fail");
							returnData.put("cancel_token_number", "0");
							log.info("Cannot find STS Number.");
							return returnData;
						}

						String TCT = "02";
						String EA = "07";
						String tokenDate = DateTimeUtil.getDateString(new Date());
						String DOE = STSToken.getDOE(tokenDate);
						String MfrCode = "96";
						String TI = "01";
						String KRN = "1";
						String idRecord = STSToken.getIdRecord(TCT, EA, SGC, KRN, DSN, DOE, MfrCode, TI);
						String cancelToken = null;
						// subclass=1이 clearCredit.
						if (stsBaseUrl != null && !"".equals(stsBaseUrl)) {
							String sendURL = stsBaseUrl.trim() + "VendCredit.ini?";
							sendURL += "meterId=" + idRecord + "&subclass=" + subClass + "&value=" + tokenValue;
							cancelToken = STSToken.makeToken(sendURL);
						}

						// token : 20 digit
						if (cancelToken == null || cancelToken.length() != 20) {
							transactionManager.commit(txStatus);
							
							returnData.put("command_result", "fail");
							returnData.put("cancel_token_number", "0");
							log.info("Cannot make Cancel Token.");
							return returnData;
						} else { 
							// 비동기 내역 저장 - SKIP
							// mobile app 상에서 충전 취소 시, TCP Trigger/ cmdSetSTSToken 명령 전송 및 Async Command를 지원하지않는다.
							// 가상화폐가 고객에게 성공적으로 반환되었을 때, Cancel Token이 실제 미터에 적용되도록 처리하는 로직은 Mobile Money 측에서 구현되어야 한다.
							
//							 List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
//							 Map<String, String> paramMap = new HashMap<String, String>();
//							 paramMap.put("string", cancelToken);
//							 paramList.add(paramMap);

							String cmd = "cmdSetSTSToken";
							long trId = System.currentTimeMillis();
							String createDate = DateTimeUtil.getDateString(new Date());
							prepayLog.setCancelToken(cancelToken);
							cancel_token_number = cancelToken;
//							 saveAsyncCommandList(meter.getModem().getDeviceSerial(), trId, cmd, paramList, DateTimeUtil.getDateString(new Date()));
							
							if (contract != null) {
								createEcgStsLog(contract.getContractNumber(), cmd, createDate, tokenDate, cancelToken, 100, trId, DateTimeUtil.getDateString(new Date()));
							}
							
//							smsInfo.put("isSTS", isSTS);
//							smsInfo.put("modemId", meter.getModemId());
//							smsInfo.put("prop", prop);
//							smsInfo.put("ipAddr", ipAddr);
//							smsInfo.put("port", port);
//							smsInfo.put("smsClassPath", smsClassPath);
//							int seq = new Random().nextInt(100) & 0xFF;
//							String smsMsg = cmdMsg((byte) seq, "244.0.0", ipAddr.replaceAll("\\.", ","), port);
//							smsInfo.put("smsMsg", smsMsg);
						}
					}

				}

				prepayLog.setIsCanceled(true);
				prepayLog.setCancelDate(TimeUtil.getCurrentTime());
				if (!reason.isEmpty()) {
					prepayLog.setCancelReason(reason);
				}
				prepaymentLogDao.update(prepayLog);

				Operator operator = operatorDao.getOperatorByLoginId(loginId);
				Operator commitedVendor = prepayLog.getOperator();

				Double currrentArrears = contract.getCurrentArrears() == null ? 0d : contract.getCurrentArrears();
				Double chargedArrears = prepayLog.getChargedArrears() == null ? 0d : prepayLog.getChargedArrears();
				Double currentCredit = contract.getCurrentCredit() == null ? 0d : contract.getCurrentCredit();
				Double chargedCredit = prepayLog.getChargedCredit() == null ? 0d : prepayLog.getChargedCredit();

				Double arrears = (Double) ObjectUtils.defaultIfNull(currrentArrears + chargedArrears, null);
				Double balance = (Double) ObjectUtils.defaultIfNull(currentCredit - chargedCredit, null);
				totalAmount += StringUtil.nullToDoubleZero(prepayLog.getChargedArrears()) + StringUtil.nullToDoubleZero(prepayLog.getChargedCredit());

				if (commitedVendor.getRole().getName().equals("vendor")) {
					commitedVendor = prepayLog.getOperator();
					commitedVendor.setDeposit(commitedVendor.getDeposit() + totalAmount);
					operatorDao.update(commitedVendor);
				}

				Double preArrears = contract.getCurrentArrears();
				Double preBalance = contract.getCurrentCredit();
				Double preChargedCredit = contract.getChargedCredit();

				addContractChangeLog(contract, operator, "currentCredit", preBalance, balance, null);
				addContractChangeLog(contract, operator, "currentArrears", preArrears, arrears, null);
				addContractChangeLog(contract, operator, "chargedCredit", preChargedCredit, -totalAmount, null);

				// 분할납부사용중이면서 해당 로그에 arrears를 charge했던 로그를 취소하는 경우
				if (isPartpayment) {
					if (prepayLog.getChargedArrears() > 0) {
						// reset Logic
						Double preFirstArrears = contract.getFirstArrears();
						Integer preContractCount = contract.getArrearsContractCount();
						Integer prePaymentCount = contract.getArrearsPaymentCount();

						Integer tempContractCount = preContractCount == null ? 0 : preContractCount;
						Integer tempPaymentCount = prePaymentCount == null ? 0 : prePaymentCount;
						Integer newContractCount = tempContractCount - tempPaymentCount + 1;

						contract.setFirstArrears(arrears);
						contract.setArrearsContractCount(newContractCount);
						contract.setArrearsPaymentCount(0);

						addContractChangeLog(contract, operator, "firstArrears", preFirstArrears, arrears, null);
						addContractChangeLog(contract, operator, "arrearsContractCount", preContractCount, newContractCount, null);
						addContractChangeLog(contract, operator, "arrearsPaymentCount", prePaymentCount, 0, null);
					}
				}

				// 분할납부가 끝난고객의 경우
				contract.setCurrentCredit(balance);
				contract.setCurrentArrears(arrears);
				// 수정 전 소스의 프로세스대로 수정했으나 본래 모델에 정의된 본래 사용의도와 동일하지 않음.
				contract.setChargedCredit(-totalAmount);
				contractDao.update(contract);
			} else {
				
				returnData.put("command_result", "fail");
				returnData.put("cancel_token_number", "0");
				log.info("This transaction has already been cancelled.");
				return returnData;
			}
		} catch (Exception e) {
			log.error(e, e);

			if (txStatus != null && !txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}

			returnData.put("command_result", "fail");
			returnData.put("cancel_token_number", "0");
			return returnData;
		}
		
		returnData.put("command_result", "success");
		returnData.put("cancel_token_number", cancel_token_number);

		return returnData;
	}

}
