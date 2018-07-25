package com.aimir.mm.ws.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.AlarmEnt;
import com.aimir.mm.model.ResponseStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MtrAlarmsResp", propOrder = { "responseStatus", "meter_number", "customer_name", "alarmResult" })
public class MtrAlarmsResp {

	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;
	
	@XmlElement(name = "customer_name")
	protected String customer_name;
	
	@XmlElement(name = "alarmResult")
	protected List<AlarmEnt> alarmResult;
	
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
	
	public List<AlarmEnt> getAlarmResult() {
		if (alarmResult == null) {
			alarmResult = new ArrayList<AlarmEnt>();
		}
		return this.alarmResult;
	}

	public void setAlarmResult(List<AlarmEnt> alarmResult) {
		this.alarmResult = alarmResult;
	}
}
