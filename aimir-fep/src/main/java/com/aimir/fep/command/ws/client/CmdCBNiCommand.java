package com.aimir.fep.command.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for cmdMeteringInterval complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="cmdCBNiCommand">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modemId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="retry" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmdCBNiCommand", propOrder = {
        "modemId",
        "requestType",
        "attributeParam"
})

public class CmdCBNiCommand {
	 @XmlElement(name = "ModemId")
	 protected String modemId;
	 
	 @XmlElement(name = "RequestType")
	 protected String requestType;
	 
	 @XmlElement(name = "AttributeParam")
	 protected String attributeParam;

	public String getModemId() {
		return modemId;
	}

	public void setModemId(String modemId) {
		this.modemId = modemId;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getAttributeParam() {
		return attributeParam;
	}

	public void setAttributeParam(String attributeParam) {
		this.attributeParam = attributeParam;
	}
	 
}
