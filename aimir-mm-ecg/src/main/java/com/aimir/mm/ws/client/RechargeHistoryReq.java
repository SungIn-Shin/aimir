package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.RequestAuthToken;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RechargeHistoryReq", propOrder = { "requestAuthToken", "meter_number", "date_period" })
public class RechargeHistoryReq {
	
	@XmlElement(name = "RequestAuthToken")
	protected RequestAuthToken requestAuthToken;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;
	
	@XmlElement(name = "date_period")
	protected String date_period;
	
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
	
	public String getDatePeriod() {
		return date_period;
	}
	
	public void setDatePeriod(String date_period) {
		this.date_period = date_period;
	}
	
}
