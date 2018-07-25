package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.ResponseStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VendRechargeResp", propOrder = { "responseStatus", "transaction_id", "vendor_id", "meter_number", "customer_name", "customer_balance_amount", "vendor_balance_amount", "token_number" })

public class VendRechargeResp {
	
	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;
	
	@XmlElement(name = "transaction_id")
	protected String transaction_id;
	
	@XmlElement(name = "vendor_id")
	protected String vendor_id;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;
	
	@XmlElement(name = "customer_name")
	protected String customer_name;
	
	@XmlElement(name = "customer_balance_amount")
	protected Double customer_balance_amount;
	
	@XmlElement(name = "vendor_balance_amount")
	protected Double vendor_balance_amount;
	
	@XmlElement(name = "token_number")
	protected String token_number;
	
	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}
	
	public String getTransactionId() {
		return transaction_id;
	}
	
	public void setTransactionId(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	
	public String getVendorId() {
		return vendor_id;
	}
	
	public void setVendorId(String vendor_id) {
		this.vendor_id = vendor_id;
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
	
	public Double getCustomerBalanceAmount() {
		return customer_balance_amount;
	}
	
	public void setCustomerBalanceAmount(Double customer_balance_amount) {
		this.customer_balance_amount = customer_balance_amount;
	}

	public Double getVendorBalanceAmount() {
		return vendor_balance_amount;
	}
	
	public void setVendorBalanceAmount(Double vendor_balance_amount) {
		this.vendor_balance_amount = vendor_balance_amount;
	}
	
	public String getTokenNumber() {
		return token_number;
	}
	
	public void setTokenNumber(String token_number) {
		this.token_number = token_number;
	}

}
