package com.aimir.mm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestAuthToken", propOrder = { "user_name", "password" })
public class RequestAuthToken {
	
	@XmlElement(name = "user_name")
	private String user_name;
	
	@XmlElement(name = "password")
	private String password;

    public String getUserName() {
        return user_name;
    }
    
    public void setUserName(String user_name) {
        this.user_name = user_name;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
	
}
