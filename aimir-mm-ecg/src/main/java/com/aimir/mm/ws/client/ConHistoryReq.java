package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.RequestAuthToken;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConHistoryReq", propOrder = { "requestAuthToken", "meter_number", "start_date", "end_date" })
public class ConHistoryReq {

	@XmlElement(name = "RequestAuthToken")
	protected RequestAuthToken requestAuthToken;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;
	
	@XmlElement(name = "start_date")
	protected String start_date;
	
	@XmlElement(name = "end_date")
	protected String end_date;
	
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
	
	public String getStartDate() {
		return start_date;
	}
	
	public void setStartDate(String start_date) {
		this.start_date = start_date;
	}
	
	public String getEndDate() {
		return end_date;
	}
	
	public void setEndDate(String end_date) {
		this.end_date = end_date;
	}
	
}
