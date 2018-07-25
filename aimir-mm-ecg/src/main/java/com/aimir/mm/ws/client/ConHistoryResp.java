package com.aimir.mm.ws.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aimir.mm.model.ConsumptionEnt;
import com.aimir.mm.model.ResponseStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConHistoryResp", propOrder = { "responseStatus", "meter_number", "customer_name", "use_date",
		"energy_consumption", "amount_charged", "energyConsumptionResult" })
public class ConHistoryResp {

	@XmlElement(name = "ResponseStatus")
	protected ResponseStatus responseStatus;

	@XmlElement(name = "meter_number")
	protected String meter_number;

	@XmlElement(name = "customer_name")
	protected String customer_name;

	@XmlElement(name = "use_date")
	protected String use_date;
	
	@XmlElement(name = "energy_consumption")
	protected Double energy_consumption;

	@XmlElement(name = "amount_charged")
	protected Double amount_charged;

	@XmlElement(name = "energyConsumptionResult")
	protected List<ConsumptionEnt> energyConsumptionResult;

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

	public String getUseDate() {
		return use_date;
	}

	public void setUseDate(String use_date) {
		this.use_date = use_date;
	}

	public Double getEnergyConsumption() {
		return energy_consumption;
	}

	public void setEnergyConsumption(Double energy_consumption) {
		this.energy_consumption = energy_consumption;
	}

	public Double getAmountCharged() {
		return amount_charged;
	}

	public void setAmountCharged(Double amount_charged) {
		this.amount_charged = amount_charged;
	}

	public List<ConsumptionEnt> getEnergyConsumptionResult() {
		if (energyConsumptionResult == null) {
			energyConsumptionResult = new ArrayList<ConsumptionEnt>();
		}
		return this.energyConsumptionResult;
	}

	public void setEnergyConsumptionResult(List<ConsumptionEnt> energyConsumptionResult) {
		this.energyConsumptionResult = energyConsumptionResult;
	}
}
