package com.aimir.mm.ws.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.RechargeHistoryEnt;
import com.aimir.mm.model.ResponseStatus;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RechargeHistoryResp", propOrder = { "responseStatus", "meter_number", "rechargeHistoryEnt", "rechargeHistoryResult" })

public class RechargeHistoryResp {
	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;
	
	@XmlElement(name = "meter_number")
	protected String meter_number;

	@XmlElement(name = "RechargeHistoryEnt")
	protected RechargeHistoryEnt rechargeHistoryEnt;
	
	@XmlElement(name = "RechargeHistoryResult")
	protected List<RechargeHistoryEnt> rechargeHistoryResult;
	
	
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

	public RechargeHistoryEnt getRechargeHistoryEnt() {
		return rechargeHistoryEnt;
	}

	public void setRechargeHistoryEnt(RechargeHistoryEnt rechargeHistoryEnt) {
		this.rechargeHistoryEnt = rechargeHistoryEnt;
	}
	
	public List<RechargeHistoryEnt> getRechargeHistoryResult() {
		if (rechargeHistoryResult == null) {
			rechargeHistoryResult = new ArrayList<RechargeHistoryEnt>();
		}
		return this.rechargeHistoryResult;
	}

	public void setRechargeHistoryResult(List<RechargeHistoryEnt> rechargeHistoryResult) {
		this.rechargeHistoryResult = rechargeHistoryResult;
	}
	
}
