package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.ResponseStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VendBalanceResp", propOrder = { "responseStatus", "vendor_name", "balance_amount" })
public class VendBalanceResp {

	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;
	
	@XmlElement(name = "vendor_name")
	protected String vendor_name;
	
	@XmlElement(name = "balance_amount")
	protected Double balance_amount;
	
	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}
	
	public String getVendorName() {
		return vendor_name;
	}
	
	public void setVendorName(String vendor_name) {
		this.vendor_name = vendor_name;
	}
	
	public Double getBalanceAmount() {
		return balance_amount;
	}
	
	public void setBalanceAmount(Double balance_amount) {
		this.balance_amount = balance_amount;
	}
	
}
