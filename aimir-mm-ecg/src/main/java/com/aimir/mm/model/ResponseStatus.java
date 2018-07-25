package com.aimir.mm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseStatus", propOrder = { "response_status", "response_code", "response_code_description" })
public class ResponseStatus {

	@XmlElement(name = "response_status")
	private Boolean response_status;
	
	@XmlElement(name = "response_code")
	private String response_code;
	
	@XmlElement(name = "response_code_description")
	private String response_code_description;
	
	public Boolean getResponseStatus() {
        return response_status;
    }
    
    public void setResponseStatus(Boolean response_status) {
        this.response_status = response_status;
    }
    
    public String getResponseCode() {
        return response_code;
    }
    
    public void setResponseCode(String response_code) {
        this.response_code = response_code;
    }
	
    public String getResponseCodeDescription() {
        return response_code_description;
    }
    
    public void setResponseCodeDescription(String response_code_description) {
        this.response_code_description = response_code_description;
    }
	
}
