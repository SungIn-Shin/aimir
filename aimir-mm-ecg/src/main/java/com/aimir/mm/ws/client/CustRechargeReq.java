package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.RequestAuthToken;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustRechargeReq", propOrder = { "requestAuthToken", "meter_number", "recharge_amount" })
public class CustRechargeReq {

	@XmlElement(name = "RequestAuthToken")
	protected RequestAuthToken requestAuthToken;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;
	
	@XmlElement(name = "recharge_amount")
	protected Double recharge_amount;
	
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
	
	public Double getRechargeAmount() {
		return recharge_amount;
	}
	
	public void setRechargeAmount(Double recharge_amount) {
		this.recharge_amount = recharge_amount;
	}
	
}
