package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.ResponseStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustLastPurResp", propOrder = { "responseStatus", "meter_number", "customer_name", "last_purchase_amount", "last_purchase_date" })
public class CustLastPurResp {

	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;
	
	@XmlElement(name = "customer_name")
	protected String customer_name;
	
	@XmlElement(name = "last_purchase_amount")
	protected Double last_purchase_amount;
	
	@XmlElement(name = "last_purchase_date")
	protected String last_purchase_date;
	
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

	public Double getLastPurchaseAmount() {
		return last_purchase_amount;
	}
	
	public void setLastPurchaseAmount(Double last_purchase_amount) {
		this.last_purchase_amount = last_purchase_amount;
	}
	
	public String getLastPurchaseDate() {
		return last_purchase_date;
	}
	
	public void setLastPurchaseDate(String last_purchase_date) {
		this.last_purchase_date = last_purchase_date;
	}
	
}
