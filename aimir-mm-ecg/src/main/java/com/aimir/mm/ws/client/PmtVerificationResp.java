package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.ResponseStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PmtVerificationResp", propOrder = { "responseStatus", "meter_number", "customer_name", "payment_verification_code", "amount_paid", "payment_date" })
public class PmtVerificationResp {

	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;
	
	@XmlElement(name = "customer_name")
	protected String customer_name;
	
	@XmlElement(name = "payment_verification_code")
	protected Long payment_verification_code;
	
	@XmlElement(name = "amount_paid")
	protected Double amount_paid;

	@XmlElement(name = "payment_date")
	protected String payment_date;
	
	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}
	
	public String getMeterNumber() {
		return meter_number;
	}
	
	public void setMeterNumber(String meter_number) {
		this.meter_number = meter_number;
	}
	
	public String getCustomerName() {
		return customer_name;
	}
	
	public void setCustomerName(String customer_name) {
		this.customer_name = customer_name;
	}
	
	public Long getPaymentVerificationCode() {
		return payment_verification_code;
	}
	
	public void setPaymentVerificationCode(Long payment_verification_code) {
		this.payment_verification_code = payment_verification_code;
	}
	
	public Double getAmountPaid() {
		return amount_paid;
	}
	
	public void setAmountPaid(Double amount_paid) {
		this.amount_paid = amount_paid;
	}

	public String getPaymentDate() {
		return payment_date;
	}
	
	public void setPaymentDate(String timestamp) {
		this.payment_date = timestamp;
	}
	
}
