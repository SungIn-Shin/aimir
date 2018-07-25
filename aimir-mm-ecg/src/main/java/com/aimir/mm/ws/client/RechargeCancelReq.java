package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.RequestAuthToken;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RechargeCancelReq", propOrder = { "requestAuthToken", "transaction_id", "cancel_reason" })
public class RechargeCancelReq {
	
	@XmlElement(name = "RequestAuthToken")
	protected RequestAuthToken requestAuthToken;
	
	@XmlElement(name = "transaction_id")
	protected String transaction_id;
	
	@XmlElement(name = "cancel_reason")
	protected String cancel_reason;
	
	public RequestAuthToken getRequestAuthToken() {
		return requestAuthToken;
	}

	public void setRequestAuthToken(RequestAuthToken requestAuthToken) {
		this.requestAuthToken = requestAuthToken;
	}
	
	public String getTransactionId() {
		return transaction_id;
	}
	
	public void setTransactionId(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	
	public String getCancelReason() {
		return cancel_reason;
	}
	
	public void setCancelReason(String cancel_reason) {
		this.cancel_reason = cancel_reason;
	}
	
}
