package com.aimir.mm.ws.client;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://server.ws.mm.aimir.com/", name = "MmWS")
@XmlSeeAlso({ObjectFactory.class})
public interface MmWS {

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "CustBalanceReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.CustBalanceReq")
	@WebMethod
	@ResponseWrapper(localName = "CustBalanceResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.CustBalanceResp")
	public CustBalanceResp checkCustomerBalance(
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "meter_number", targetNamespace = "") java.lang.String meter_number) throws Exception;

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "VendBalanceReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.VendBalanceReq")
	@WebMethod
	@ResponseWrapper(localName = "VendBalanceResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.VendBalanceResp")
	public VendBalanceResp checkVendorBalance(
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "vendor_id", targetNamespace = "") java.lang.String vendor_id) throws Exception;

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "CustRechargeReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.CustRechargeReq")
	@WebMethod
	@ResponseWrapper(localName = "CustRechargeResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.CustRechargeResp")
	public CustRechargeResp customerRecharge(
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "meter_number", targetNamespace = "") java.lang.String meter_number,
			@WebParam(name = "recharge_amount", targetNamespace = "") java.lang.Double recharge_amount) throws Exception;

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "VendRechargeReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.VendRechargeReq")
	@WebMethod
	@ResponseWrapper(localName = "VendRechargeResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.VendRechargeResp")
	public VendRechargeResp vendorRecharge(
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "meter_number", targetNamespace = "") java.lang.String meter_number,
			@WebParam(name = "vendor_id", targetNamespace = "") java.lang.String vendor_id,
			@WebParam(name = "recharge_amount", targetNamespace = "") java.lang.Double recharge_amount) throws Exception;
	
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "PmtVerificationReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.PmtVerificationReq")
	@WebMethod
	@ResponseWrapper(localName = "PmtVerificationResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.PmtVerificationResp")
	public PmtVerificationResp paymentVerification (
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "meter_number", targetNamespace = "") java.lang.String meter_number,
			@WebParam(name = "payment_verification_code", targetNamespace = "") java.lang.String payment_verification_code) throws Exception;
	
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "CustLastPurReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.CustLastPurReq")
	@WebMethod
	@ResponseWrapper(localName = "CustLastPurResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.CustLastPurResp")
	public CustLastPurResp customerLastPurchase(
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "meter_number", targetNamespace = "") java.lang.String meter_number) throws Exception;
	
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "ConHistoryReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.ConHistoryReq")
	@WebMethod
	@ResponseWrapper(localName = "ConHistoryResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.ConHistoryResp")
	public ConHistoryResp consumptionHistory (
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "meter_number", targetNamespace = "") java.lang.String meter_number,
			@WebParam(name = "start_date", targetNamespace = "") java.lang.String start_date,
			@WebParam(name = "end_date", targetNamespace = "") java.lang.String end_date) throws Exception;
		
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "BillHistoryReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.BillHistoryReq")
	@WebMethod
	@ResponseWrapper(localName = "BillHistoryResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.BillHistoryResp")
	public BillHistoryResp billingHistory (
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "meter_number", targetNamespace = "") java.lang.String meter_number,
			@WebParam(name = "date_period", targetNamespace = "") java.lang.String date_period) throws Exception;
	
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "MtrAlarmsReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.MtrAlarmsReq")
	@WebMethod
	@ResponseWrapper(localName = "MtrAlarmsResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.MtrAlarmsResp")
	public MtrAlarmsResp meterAlarms (
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "meter_number", targetNamespace = "") java.lang.String meter_number,
			@WebParam(name = "start_date", targetNamespace = "") java.lang.String start_date,
			@WebParam(name = "end_date", targetNamespace = "") java.lang.String end_date) throws Exception;

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "RechargeHistoryReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.RechargeHistoryReq")
	@WebMethod
	@ResponseWrapper(localName = "RechargeHistoryResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.RechargeHistoryResp")
	public RechargeHistoryResp rechargeHistory (
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "meter_number", targetNamespace = "") java.lang.String meter_number,
			@WebParam(name = "date_period", targetNamespace = "") java.lang.String date_period) throws Exception;

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "RechargeCancelReq", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.RechargeCancelReq")
	@WebMethod
	@ResponseWrapper(localName = "RechargeCancelResp", targetNamespace = "http://server.ws.mm.aimir.com/", className = "com.aimir.mm.ws.client.RechargeCancelResp")
	public RechargeCancelResp rechargeCancel (
			@WebParam(name = "RequestAuthToken", targetNamespace = "") com.aimir.mm.model.RequestAuthToken RequestAuthToken,
			@WebParam(name = "transaction_id", targetNamespace = "") java.lang.String transaction_id,
			@WebParam(name = "cancel_reason", targetNamespace = "") java.lang.String cancel_reason) throws Exception;
}
