package com.aimir.mm.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aimir.dao.device.EventAlertLogDao;
import com.aimir.dao.device.MeterDao;
import com.aimir.dao.mvm.BillingBlockTariffDao;
import com.aimir.dao.mvm.BillingDayEMDao;
import com.aimir.dao.mvm.BillingMonthEMDao;
import com.aimir.dao.system.ContractDao;
import com.aimir.dao.system.CustomerDao;
import com.aimir.dao.system.OperatorDao;
import com.aimir.dao.system.PrepaymentLogDao;
import com.aimir.mm.model.AlarmEnt;
import com.aimir.mm.model.ConsumptionEnt;
import com.aimir.mm.model.RechargeHistoryEnt;
import com.aimir.mm.model.RequestAuthToken;
import com.aimir.mm.model.ResponseStatus;
import com.aimir.mm.validator.MeterCheckReqParameterValidator;
import com.aimir.mm.validator.VendorCheckReqParameterValidator;
import com.aimir.mm.ws.client.BillHistoryResp;
import com.aimir.mm.ws.client.ConHistoryResp;
import com.aimir.mm.ws.client.CustBalanceResp;
import com.aimir.mm.ws.client.CustLastPurResp;
import com.aimir.mm.ws.client.CustRechargeResp;
import com.aimir.mm.ws.client.MtrAlarmsResp;
import com.aimir.mm.ws.client.PmtVerificationResp;
import com.aimir.mm.ws.client.RechargeCancelResp;
import com.aimir.mm.ws.client.RechargeHistoryResp;
import com.aimir.mm.ws.client.VendBalanceResp;
import com.aimir.mm.ws.client.VendRechargeResp;
import com.aimir.model.device.EventAlertLog;
import com.aimir.model.device.Meter;
import com.aimir.model.system.Operator;
import com.aimir.model.system.PrepaymentLog;
import com.aimir.util.Condition;
import com.aimir.util.Condition.Restriction;

/**
 * MobileMoney WebService
 *
 * @author designer, 2017-06-09
 */

@Service
@Transactional(value = "transactionManager")
public class MmService {

	private static Log log = LogFactory.getLog(MmService.class);
	private static String space = " ";

	@Autowired
    MeterDao meterDao;
	
	@Autowired
    ContractDao contractDao;
	
	@Autowired
    OperatorDao operatorDao;
	
	@Autowired
	PrepaymentLogDao prepaymentLogDao;
	
	@Autowired
    MmUserAuthentication mmAuth;
	
	@Autowired
	BillingBlockTariffDao billingBlockTariffDao;
	
	@Autowired
    BillingMonthEMDao billingMonthEMDao;
	
	@Autowired
    BillingDayEMDao billingDayEMDao;
	
	@Autowired
    CustomerDao customerDao;
	
	@Autowired
	EventAlertLogDao eventAlertLogDao;
	
	@Autowired
	PrepaymentManager prepaymentManager;
	
	@Autowired
	MeterCheckReqParameterValidator meterCheckReqParameterValidator;
	
	@Autowired
	VendorCheckReqParameterValidator vendorCheckReqParameterValidator;
	
	/**
	 * This method shall be used to check the current outstanding recharge balance of a customer.
	 * */
	public CustBalanceResp checkCustomerBalance(RequestAuthToken requestAuthToken, String meter_number) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		CustBalanceResp response = new CustBalanceResp();
		
		try {
			mmAuth.userAuthentication(requestAuthToken);
			meterCheckReqParameterValidator.validator(meter_number);			
			Meter meter = meterDao.findByCondition("mdsId", meter_number);

			responseStatus.setResponseStatus(true);
			responseStatus.setResponseCode(space);
			responseStatus.setResponseCodeDescription(space);
			response.setResponseStatus(responseStatus);
			
			response.setMeterNumber(meter_number);
			response.setCustomerName(meter.getCustomer().getName());
			response.setBalanceAmount(meter.getContract().getCurrentCredit());
		} catch (Exception e) {
			log.error(e, e);
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);

			return response;
		}

		return response;
	}

	/**
	 * This method shall be used to check the current outstanding quota balance of a vendor.
	 * */
	public VendBalanceResp checkVendorBalance(RequestAuthToken requestAuthToken, String vendor_id) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		VendBalanceResp response = new VendBalanceResp();

		try {
			mmAuth.userAuthentication(requestAuthToken);
			vendorCheckReqParameterValidator.validator(vendor_id);
			
			Operator operator = operatorDao.findByCondition("loginId", vendor_id);
			
			responseStatus.setResponseStatus(true);
			responseStatus.setResponseCode(space);
			responseStatus.setResponseCodeDescription(space);
			response.setResponseStatus(responseStatus);
			
			response.setVendorName(vendor_id);
			response.setBalanceAmount(operator.getDeposit());
		} catch (Exception e) {
			log.error(e, e);
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);

			return response;
		}

		return response;
	}

	/**
	 * This method shall be used to recharge customer meter and shall be on a non-quota basis.
	 * */
	public CustRechargeResp customerRecharge(RequestAuthToken requestAuthToken, String meter_number, Double recharge_amount) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		CustRechargeResp response = new CustRechargeResp();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			mmAuth.userAuthentication(requestAuthToken);
			meterCheckReqParameterValidator.validator(meter_number);
			resultMap = prepaymentManager.customerRecharge(requestAuthToken, meter_number, recharge_amount);
			
			boolean status = (boolean) resultMap.get("responseStatus");
			String responseCode = resultMap.get("responseCode").toString();
			String responseCodeDescription = resultMap.get("responseCodeDescription").toString();
			
			responseStatus.setResponseStatus(status);
			responseStatus.setResponseCode(responseCode);
			responseStatus.setResponseCodeDescription(responseCodeDescription);
			response.setResponseStatus(responseStatus);

			if (status) {
				response.setBalanceAmount(Double.parseDouble(resultMap.get("balanceAmount").toString()));
				response.setCustomerName(resultMap.get("customerName").toString());
				response.setMeterNumber(resultMap.get("meterNumber").toString());
				response.setTransactionId(resultMap.get("transactionId").toString());
				response.setTokenNumber(resultMap.get("tokenNumber").toString()); // OPF-362
			}
		} catch (Exception e) {
			log.error(e, e);
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);

			return response;
		}
		
		return response;
	}
	
	/**
	 * This method shall be used by a vendor to recharge customer meter and shall be on a quota basis.
	 * */
	public VendRechargeResp vendorRecharge(RequestAuthToken requestAuthToken, String meter_number, String vendor_id, Double recharge_amount) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		VendRechargeResp response = new VendRechargeResp();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			mmAuth.userAuthentication(requestAuthToken);
			meterCheckReqParameterValidator.validator(meter_number);
			vendorCheckReqParameterValidator.validator(vendor_id);
			
			Operator operator = operatorDao.getOperatorByLoginId(requestAuthToken.getUserName());
			Boolean isVendor = operator.getRole().getName().equals("vendor");
			
			if (!isVendor) {
				throw new Exception("This is not the vendor account");
			}
			
			resultMap = prepaymentManager.vendorRecharge(requestAuthToken, meter_number, vendor_id, recharge_amount);
			
			boolean status = (boolean) resultMap.get("responseStatus");
			String responseCode = resultMap.get("responseCode").toString();
			String responseCodeDescription = resultMap.get("responseCodeDescription").toString();
			
			responseStatus.setResponseStatus(status);
			responseStatus.setResponseCode(responseCode);
			responseStatus.setResponseCodeDescription(responseCodeDescription);
			response.setResponseStatus(responseStatus);			
			
			if (status) {
				response.setCustomerBalanceAmount(Double.parseDouble(resultMap.get("customerBalanceAmount").toString()));
				response.setCustomerName(resultMap.get("customerName").toString());
				response.setMeterNumber(resultMap.get("meterNumber").toString());
				response.setTransactionId(resultMap.get("transactionId").toString());
				response.setVendorBalanceAmount(Double.parseDouble(resultMap.get("vendorBalanceAmount").toString()));
				response.setVendorId(resultMap.get("vendorId").toString());
				response.setTokenNumber(resultMap.get("tokenNumber").toString()); // OPF-362
			}
		} catch (Exception e) {
			log.error(e, e);
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);

			return response;
		}
		
		return response;
	}


	/** 
	 * This method shall be used to verify if payment has been made by a particular customer.
	 */
	public PmtVerificationResp paymentVerification(RequestAuthToken requestAuthToken, String meter_number, String payment_verification_code) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		PmtVerificationResp response = new PmtVerificationResp();

		try {
			mmAuth.userAuthentication(requestAuthToken);
			meterCheckReqParameterValidator.validator(meter_number);
			
			List<PrepaymentLog> list = new ArrayList<PrepaymentLog>();
			Meter meter = meterDao.findByCondition("mdsId", meter_number);
			
			Set<Condition> condition = new HashSet<Condition>();
			condition = new HashSet<Condition>();
			condition.add(new Condition("id", new Object[] { Long.parseLong(payment_verification_code) }, null, Restriction.EQ));

			list = prepaymentLogDao.findByConditions(condition);

			responseStatus.setResponseStatus(true);
			responseStatus.setResponseCode(space);
			responseStatus.setResponseCodeDescription(space);
			response.setResponseStatus(responseStatus);
			response.setCustomerName(meter.getCustomer().getName());
			response.setMeterNumber(meter_number);
			response.setPaymentVerificationCode(Long.parseLong(payment_verification_code));

			String paymenDate = list.get(0).getLastTokenDate();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = sdf.parse(paymenDate);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			paymenDate = formatter.format(date);

			response.setPaymentDate(paymenDate);
			// response.setPaymentDate(new TIMESTAMP(paymenDate));
			
			response.setAmountPaid(list.get(0).getChargedCredit());
		} catch (Exception e) {
			log.error(e, e);
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);

			return response;
		}
		
		return response;
	}
	
	/**
	 * This method shall be used to retrieve the last purchase of a customer.
	 * */
	public CustLastPurResp customerLastPurchase(RequestAuthToken requestAuthToken, String meter_number) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		CustLastPurResp response = new CustLastPurResp();
		
		try {
			mmAuth.userAuthentication(requestAuthToken);
			meterCheckReqParameterValidator.validator(meter_number);

			List<PrepaymentLog> list = new ArrayList<PrepaymentLog>();
			Meter meter = meterDao.findByCondition("mdsId", meter_number);
			
			Set<Condition> condition = new HashSet<Condition>();
			condition = new HashSet<Condition>();
            condition.add(new Condition("contractId", new Object[]{meter.getContract().getId()}, null, Restriction.EQ));
            condition.add(new Condition("chargedCredit", null, null, Restriction.NOTNULL));
            condition.add(new Condition("chargedCredit", new Object[]{Double.parseDouble("0")}, null, Restriction.NE));            
            condition.add(new Condition("lastTokenDate", null, null, Restriction.ORDERBYDESC));
            
            list = prepaymentLogDao.findByConditions(condition);
			
            responseStatus.setResponseStatus(true);
			responseStatus.setResponseCode(space);
			responseStatus.setResponseCodeDescription(space);
			response.setResponseStatus(responseStatus);
			
			response.setCustomerName(list.get(0).getCustomer().getName());
			response.setLastPurchaseAmount(list.get(0).getChargedCredit());
			response.setLastPurchaseDate(list.get(0).getLastTokenDate());
			response.setMeterNumber(meter_number);
		} catch (Exception e) {
			log.error(e, e);
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);

			return response;
		}
		
		return response;
	}
		
	/**
	 * This method shall be used to retrieve the energy consumption history of a customer over a specified date range.
	 * */
	public ConHistoryResp consumptionHistory(RequestAuthToken requestAuthToken, String meter_number, String start_date, String end_date) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		ConHistoryResp response = new ConHistoryResp();
		Meter meter = null;

		try {
			mmAuth.userAuthentication(requestAuthToken);
			meterCheckReqParameterValidator.validator(meter_number);

			List<PrepaymentLog> list = new ArrayList<PrepaymentLog>();
			meter = meterDao.findByCondition("mdsId", meter_number);
			Double usedCost = 0.0;
			
			Set<Condition> condition = new HashSet<Condition>();
			condition = new HashSet<Condition>();
			condition.add(new Condition("contractId", new Object[]{meter.getContract().getId()}, null, Restriction.EQ));
			condition.add(new Condition("lastTokenDate", new Object[] { start_date, end_date }, null, Restriction.BETWEEN));
			condition.add(new Condition("lastTokenDate", null, null, Restriction.ORDERBYDESC));
			
            list = prepaymentLogDao.findByConditions(condition);
			
            responseStatus.setResponseStatus(true);
			responseStatus.setResponseCode(space);
			responseStatus.setResponseCodeDescription(space);
			response.setResponseStatus(responseStatus);
			response.setMeterNumber(meter_number);
			response.setCustomerName(meter.getCustomer().getName());
            
			List<ConsumptionEnt> consumptionEntList = new ArrayList<ConsumptionEnt>();
			for (int i = 0; i < list.size(); i++) {
				// 해당 기간동안 사용한 EnergyConsumption 날짜, 사용금액  (S)
				ConsumptionEnt consumptionEnt = new ConsumptionEnt();
				
				consumptionEnt.setUseDate(list.get(i).getLastTokenDate().toString());
				consumptionEnt.setEnergyConsumption(list.get(i).getUsedConsumption());
				consumptionEntList.add(consumptionEnt);
				// 해당 기간동안 사용한 EnergyConsumption 날짜, 사용금액  (E)
				
				// 해당 기간동안 사용한 총 금액 (cedi)
				usedCost += list.get(i).getUsedCost() == null ? 0.0 : list.get(i).getUsedCost();
			}
			 
			response.setAmountCharged(usedCost);
			response.setEnergyConsumptionResult(consumptionEntList);
		} catch (Exception e) {
			log.error(e, e);
			
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);

			return response;
		}
		
		return response;
	}

	/**
	 * This method shall be used to retrieve the consumption charge history of a customer on a monthly basis.
	 */
	public BillHistoryResp billingHistory(RequestAuthToken requestAuthToken, String meter_number, String date_period) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		BillHistoryResp response = new BillHistoryResp();

		try {
			mmAuth.userAuthentication(requestAuthToken);
			meterCheckReqParameterValidator.validator(meter_number);

			List<PrepaymentLog> list = new ArrayList<PrepaymentLog>();
			Meter meter = meterDao.findByCondition("mdsId", meter_number);
			Double usedCost = 0.0;

			Set<Condition> condition = new HashSet<Condition>();
			condition = new HashSet<Condition>();
			condition.add(new Condition("contractId", new Object[] { meter.getContract().getId() }, null, Restriction.EQ));
			condition.add(new Condition("lastTokenDate", new Object[] { date_period + "%" }, null, Restriction.LIKE));
			condition.add(new Condition("lastTokenDate", null, null, Restriction.ORDERBYDESC));
			list = prepaymentLogDao.findByConditions(condition);

			responseStatus.setResponseStatus(true);
			responseStatus.setResponseCode(space);
			responseStatus.setResponseCodeDescription(space);
			response.setResponseStatus(responseStatus);
			response.setMeterNumber(meter_number);
			response.setCustomerName(meter.getCustomer().getName());

			for (int i = 0; i < list.size(); i++) {
				// 해당 월 사용한 총 금액 (cedi)
				usedCost += list.get(i).getUsedCost() == null ? 0.0 : list.get(i).getUsedCost();
			}

			response.setConsumptionCharge(usedCost);
		} catch (Exception e) {
			log.error(e, e);

			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);

			return response;
		}

		return response;
	}
	
	/**
	 * This method shall be used to retrieve any available pull or push meter alarms of a customer over a specified date range.
	 * */
	public MtrAlarmsResp meterAlarms(RequestAuthToken requestAuthToken, String meter_number, String start_date, String end_date) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		List<MtrAlarmsResp> responseList = new ArrayList<MtrAlarmsResp>();
		MtrAlarmsResp response = new MtrAlarmsResp();

		try {
			mmAuth.userAuthentication(requestAuthToken);
			meterCheckReqParameterValidator.validator(meter_number);

			List<EventAlertLog> list = new ArrayList<EventAlertLog>();
			Meter meter = meterDao.findByCondition("mdsId", meter_number);
			Set<Condition> condition = new HashSet<Condition>();
			condition = new HashSet<Condition>();
			
            condition.add(new Condition("activatorId", new Object[]{meter_number}, null, Restriction.EQ));
            condition.add(new Condition("openTime", new Object[] { start_date, end_date }, null, Restriction.BETWEEN));
            condition.add(new Condition("openTime", null, null, Restriction.ORDERBYDESC));

            list = eventAlertLogDao.findByConditions(condition);

            responseStatus.setResponseStatus(true);
			responseStatus.setResponseCode(space);
			responseStatus.setResponseCodeDescription(space);
			response.setResponseStatus(responseStatus);
			response.setMeterNumber(meter_number);
			response.setCustomerName(meter.getCustomer().getName());
			
			List<AlarmEnt> alarmEntList = new ArrayList<AlarmEnt>();
			for (int i = 0; i < list.size(); i++) {
				AlarmEnt alarmEnt = new AlarmEnt();
				
				alarmEnt.setMeterAlarms(list.get(i).getMessage());
				alarmEnt.setOccurrenceTime(list.get(i).getOpenTime());
				alarmEntList.add(alarmEnt);
			}
			
			response.setAlarmResult(alarmEntList);
		} catch (Exception e) {
			log.error(e, e);
			
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);
			responseList.add(response);

			return response;
		}

		return response;
	}
	
	/**
	 * This method shall be used to retrieve the recharge history of a customer on a monthly basis.
	 * */
	public RechargeHistoryResp rechargeHistory(RequestAuthToken requestAuthToken, String meter_number, String date_period) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		List<RechargeHistoryResp> responseList = new ArrayList<RechargeHistoryResp>();
		RechargeHistoryResp response = new RechargeHistoryResp();
		
		try {
			mmAuth.userAuthentication(requestAuthToken);
			meterCheckReqParameterValidator.validator(meter_number);

			List<PrepaymentLog> list = new ArrayList<PrepaymentLog>();
			Meter meter = meterDao.findByCondition("mdsId", meter_number);
			Set<Condition> condition = new HashSet<Condition>();
			condition = new HashSet<Condition>();
			
            condition.add(new Condition("contractId", new Object[]{meter.getContract().getId()}, null, Restriction.EQ));
    		condition.add(new Condition("chargedCredit", null, null, Restriction.NOTNULL));
    		condition.add(new Condition("chargedCredit", new Object[]{Double.parseDouble("0")}, null, Restriction.NE));
    		condition.add(new Condition("lastTokenDate", new Object[] { date_period + "%" }, null, Restriction.LIKE));
    		condition.add(new Condition("lastTokenDate", null, null, Restriction.ORDERBYDESC));
    		
            list = prepaymentLogDao.findByConditions(condition);

            responseStatus.setResponseStatus(true);
    		responseStatus.setResponseCode(space);
    		responseStatus.setResponseCodeDescription(space);
    		
    		response.setResponseStatus(responseStatus);
			response.setMeterNumber(meter_number);
			
			List<RechargeHistoryEnt> rechargeHistoryEntList = new ArrayList<RechargeHistoryEnt>();
			for (int i = 0; i < list.size(); i++) {
				RechargeHistoryEnt rechargeHistoryEnt = new RechargeHistoryEnt();
				
				rechargeHistoryEnt.setTransactionId(list.get(i).getId().toString());
				rechargeHistoryEnt.setPurchaseType(list.get(i).getPayType().getName() == null ? "Cash" : list.get(i).getPayType().getName());
				rechargeHistoryEnt.setPaidArrers(list.get(i).getChargedArrears());
				rechargeHistoryEnt.setPreArrersAmount(list.get(i).getPreArrears());
				rechargeHistoryEnt.setArrersAmount(list.get(i).getArrears());
				rechargeHistoryEnt.setPreBalanceAmount(list.get(i).getPreBalance());
				rechargeHistoryEnt.setBalanceAmount(list.get(i).getBalance());
				rechargeHistoryEnt.setPurchaseDate(list.get(i).getLastTokenDate());
				rechargeHistoryEnt.setPurchaseAmount(list.get(i).getChargedCredit());
				rechargeHistoryEnt.setTokenNumber(list.get(i).getToken() == null ? "0" : list.get(i).getToken());
				rechargeHistoryEnt.setCancelDate(list.get(i).getCancelDate() == null ? space : list.get(i).getCancelDate());
				rechargeHistoryEnt.setCancelTokenNumber(list.get(i).getCancelToken() == null ? "0" : list.get(i).getCancelToken());
				rechargeHistoryEnt.setCancelReason(list.get(i).getCancelReason() == null ? space : list.get(i).getCancelReason());
				rechargeHistoryEntList.add(rechargeHistoryEnt);
			}
			
			response.setRechargeHistoryResult(rechargeHistoryEntList);
		} catch (Exception e) {
			log.error(e, e);
			
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);
			responseList.add(response);

			return response;
		}

		return response;
	}
	
	public RechargeCancelResp rechargeCancel(RequestAuthToken requestAuthToken, String transaction_id, String cancel_reason) throws Exception {
		ResponseStatus responseStatus = new ResponseStatus();
		RechargeCancelResp response = new RechargeCancelResp();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			mmAuth.userAuthentication(requestAuthToken);
			
			responseStatus.setResponseStatus(true);
			responseStatus.setResponseCode(space);
			responseStatus.setResponseCodeDescription(space);
			response.setResponseStatus(responseStatus);
			
			// paid type check (S)
			// 충전취소 transaction을 진행할 충전 타입이 'mobile'일 경우에만 충전취소를 진행한다.
			Set<Condition> condition = new HashSet<Condition>();
			List<PrepaymentLog> list = new ArrayList<PrepaymentLog>();
			
			condition = new HashSet<Condition>();
			condition.add(new Condition("id", new Object[] { Long.parseLong(transaction_id) }, null, Restriction.EQ));
			list = prepaymentLogDao.findByConditions(condition);
			String payType = list.get(0).getPayType().getName() == null ? "Cash" : list.get(0).getPayType().getName();
			
			log.info("Mobile Money Recharge Cancel - PayType [" + payType + "]");
			if (!payType.equals("Mobile")) {
				response.setCommandResult("fail");
				response.setCancelTokenNumber("0");
				log.info("Only the transaction charged with mobile money can be cancelled.");

				return response;
			}
			// paid type check (E)
			
			String loginId = requestAuthToken.getUserName(); // mobileappvs
			resultMap = prepaymentManager.rechargeCancel(Long.parseLong(transaction_id), loginId, cancel_reason);
			
			response.setCommandResult(resultMap.get("command_result").toString());
			response.setCancelTokenNumber(resultMap.get("cancel_token_number").toString() == null ? "0" : resultMap.get("cancel_token_number").toString());
		} catch (Exception e) {
			log.error(e, e);
			responseStatus.setResponseStatus(false);
			responseStatus.setResponseCode("0");
			responseStatus.setResponseCodeDescription(e.toString());
			response.setResponseStatus(responseStatus);

			return response;
		}

		return response;
	}
}
