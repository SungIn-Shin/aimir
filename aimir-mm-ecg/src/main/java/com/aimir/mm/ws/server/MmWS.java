package com.aimir.mm.ws.server;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aimir.mm.model.RequestAuthToken;
import com.aimir.mm.service.MmService;
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

/**
 * MobileMoney WebService
 *
 * @author designer, 2017-06-09
 */

@WebService(serviceName="MmWS")
@SOAPBinding(style=Style.DOCUMENT, use=Use.LITERAL, parameterStyle=ParameterStyle.WRAPPED)
@Service(value="mmWS")
public class MmWS {
	 protected static Log log = LogFactory.getLog(MmWS.class);
	
	@Autowired
	protected MmService mmService;
	
	@WebMethod
	public @WebResult(name = "CustBalanceResp") CustBalanceResp checkCustomerBalance(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "meter_number") String meter_number) throws Exception {
		
		CustBalanceResp response = new CustBalanceResp();
		response = mmService.checkCustomerBalance(requestAuthToken, meter_number);

		return response;
	}
	
	@WebMethod
	public @WebResult(name = "VendBalanceResp") VendBalanceResp checkVendorBalance(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "vendor_id") String vendor_id) throws Exception {

		VendBalanceResp response = new VendBalanceResp();
		response = mmService.checkVendorBalance(requestAuthToken, vendor_id);

		return response;
	}
	
	@WebMethod
	public @WebResult(name = "CustRechargeResp") CustRechargeResp customerRecharge(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "meter_number") String meter_number,
			@WebParam(name = "recharge_amount") Double recharge_amount) throws Exception {
		
		CustRechargeResp response = new CustRechargeResp();
		response = mmService.customerRecharge(requestAuthToken, meter_number, recharge_amount);
		
		return response;
	}

	@WebMethod
	public @WebResult(name = "VendRechargeResp") VendRechargeResp vendorRecharge(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "meter_number") String meter_number,
			@WebParam(name = "vendor_id") String vendor_id,
			@WebParam(name = "recharge_amount") Double recharge_amount) throws Exception {
		
		VendRechargeResp response = new VendRechargeResp();
		response = mmService.vendorRecharge(requestAuthToken, meter_number, vendor_id, recharge_amount);

		return response;
	}
	
	@WebMethod
	public @WebResult(name = "PmtVerificationResp") PmtVerificationResp paymentVerification(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "meter_number") String meter_number,
			@WebParam(name = "payment_verification_code") String payment_verification_code) throws Exception {
		
		PmtVerificationResp response = new PmtVerificationResp();
		response = mmService.paymentVerification(requestAuthToken, meter_number, payment_verification_code);

		return response;
	}
	
	@WebMethod
	public @WebResult(name = "CustLastPurResp") CustLastPurResp customerLastPurchase(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "meter_number") String meter_number) throws Exception {
		
		CustLastPurResp response = new CustLastPurResp();
		response = mmService.customerLastPurchase(requestAuthToken, meter_number);

		return response;
	}
	
	@WebMethod
	public @WebResult(name = "ConHistoryResp") ConHistoryResp consumptionHistory(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "meter_number") String meter_number, 
			@WebParam(name = "start_date") String start_date,
			@WebParam(name = "end_date") String end_date) throws Exception {

		ConHistoryResp response = new ConHistoryResp();
		response = mmService.consumptionHistory(requestAuthToken, meter_number, start_date, end_date);

		return response;
	}
	
	@WebMethod
	public @WebResult(name = "BillHistoryResp") BillHistoryResp billingHistory(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "meter_number") String meter_number, 
			@WebParam(name = "date_period") String date_period) throws Exception {

		BillHistoryResp response = new BillHistoryResp();
		response = mmService.billingHistory(requestAuthToken, meter_number, date_period);

		return response;
	}

	@WebMethod
	public @WebResult(name = "MtrAlarmsResp") MtrAlarmsResp meterAlarms(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "meter_number") String meter_number, 
			@WebParam(name = "start_date") String start_date,
			@WebParam(name = "end_date") String end_date) throws Exception {

		MtrAlarmsResp response = new MtrAlarmsResp();
		response = mmService.meterAlarms(requestAuthToken, meter_number, start_date, end_date);

		return response;
	}
	
	@WebMethod
	public @WebResult(name = "RechargeHistoryResp") RechargeHistoryResp rechargeHistory(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "meter_number") String meter_number, 
			@WebParam(name = "date_period") String date_period) throws Exception {

		RechargeHistoryResp response = new RechargeHistoryResp();
		response = mmService.rechargeHistory(requestAuthToken, meter_number, date_period);

		return response;
	}
	
	@WebMethod
	public @WebResult(name = "RechargeCancelResp") RechargeCancelResp rechargeCancel(
			@WebParam(name = "RequestAuthToken") RequestAuthToken requestAuthToken,
			@WebParam(name = "transaction_id") String transaction_id, 
			@WebParam(name = "cancel_reason") String cancel_reason) throws Exception {

		RechargeCancelResp response = new RechargeCancelResp();
		response = mmService.rechargeCancel(requestAuthToken, transaction_id, cancel_reason);

		return response;
	}
}
