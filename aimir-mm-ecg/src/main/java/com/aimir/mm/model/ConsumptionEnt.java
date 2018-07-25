package com.aimir.mm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConsumptionEnt", propOrder = { "use_date", "energy_consumption" })

public class ConsumptionEnt {
	@XmlElement(name = "use_date")
	protected String use_date;

	@XmlElement(name = "energy_consumption")
	protected Double energy_consumption;

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
}
