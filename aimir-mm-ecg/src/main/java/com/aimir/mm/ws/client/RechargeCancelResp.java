package com.aimir.mm.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.ResponseStatus;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RechargeCancelResp", propOrder = { "responseStatus", "command_result", "cancel_token_number" })

public class RechargeCancelResp {
	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;
	
	@XmlElement(name = "command_result")
	protected String command_result;

	@XmlElement(name = "cancel_token_number")
	protected String cancel_token_number;
	
	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getCommandResult() {
		return command_result;
	}

	public void setCommandResult(String command_result) {
		this.command_result = command_result;
	}

	public String getCancelTokenNumber() {
		return cancel_token_number;
	}

	public void setCancelTokenNumber(String cancel_token_number) {
		this.cancel_token_number = cancel_token_number;
	}

}
