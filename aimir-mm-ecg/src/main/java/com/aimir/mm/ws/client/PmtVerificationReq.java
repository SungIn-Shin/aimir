package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.RequestAuthToken;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PmtVerificationReq", propOrder = { "requestAuthToken", "meter_number", "payment_verification_code" })
public class PmtVerificationReq {

	@XmlElement(name = "RequestAuthToken")
	protected RequestAuthToken requestAuthToken;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;
	
	@XmlElement(name = "payment_verification_code")
	protected String payment_verification_code;

	public RequestAuthToken getRequestAuthToken() {
		return requestAuthToken;
	}

	public void setRequestAuthToken(RequestAuthToken requestAuthToken) {
		this.requestAuthToken = requestAuthToken;
	}
	
	public String getMeterNumber() {
		return meter_number;
	}
	
	public void setMeterNumber(String meter_number) {
		this.meter_number = meter_number;
	}
	
	public String getPaymentVerificationCode() {
		return payment_verification_code;
	}

	public void setPaymentVerificationCode(String payment_verification_code) {
		this.payment_verification_code = payment_verification_code;
	}

}
