package com.aimir.mm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlarmEnt", propOrder = {
    "meter_alarms",
    "occurrence_time"
})

public class AlarmEnt {
    @XmlElement(name = "meter_alarms")
	protected String meter_alarms;
	
	@XmlElement(name = "occurrence_time")
	protected String occurrence_time;
    
    public String getMeterAlarms() {
        return meter_alarms;
    }
    
    public void setMeterAlarms(String meter_alarms) {
        this.meter_alarms = meter_alarms;
    }
    
    public String getOccurrenceTime() {
        return occurrence_time;
    }
    
    public void setOccurrenceTime(String occurrence_time) {
        this.occurrence_time = occurrence_time;
    }
}
