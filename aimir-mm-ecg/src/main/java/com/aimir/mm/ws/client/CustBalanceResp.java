package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.ResponseStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustBalanceResp", propOrder = { "responseStatus", "meter_number", "customer_name", "balance_amount" })
public class CustBalanceResp {

	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;
	
	@XmlElement(name = "customer_name")
	protected String customer_name;
	
	@XmlElement(name = "balance_amount")
	protected Double balance_amount;
	
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
	
	public Double getBalanceAmount() {
		return balance_amount;
	}
	
	public void setBalanceAmount(Double balance_amount) {
		this.balance_amount = balance_amount;
	}
	
}
