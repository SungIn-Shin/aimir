package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.ResponseStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BillHistoryResp", propOrder = { "responseStatus", "meter_number", "customer_name",
		"consumption_charge" })
public class BillHistoryResp {

	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;

	@XmlElement(name = "meter_number")
	protected String meter_number;

	@XmlElement(name = "customer_name")
	protected String customer_name;

	@XmlElement(name = "consumption_charge")
	protected Double consumption_charge;

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

	public Double getConsumptionCharge() {
		return consumption_charge;
	}

	public void setConsumptionCharge(Double consumption_charge) {
		this.consumption_charge = consumption_charge;
	}
	
}
