package com.aimir.fep.command.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for cmdNIBypassMeterInterface complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="cmdNIBypassMeterInterface">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modemId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="meterId" type="{http://www.w3.org/2001/XMLSchema}string" />  
 *         &lt;element name="requestType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="uArt" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmdNIBypassMeterInterface", propOrder = {        
        "modemId",
        "meterId",
        "requestType",
        "uArt"
})
public class CmdNIBypassMeterInterface {
    @XmlElement(name = "ModemId")
    protected String modemId;

    @XmlElement(name = "MeterId")
    protected String meterId;

    @XmlElement(name = "RequestType")
    protected String requestType;
    
    @XmlElement(name = "Uart")
    protected int uArt;
    
	/**
     * Get the value of the modemOd property.
     * @return String
     */
    public String getModemId() {
        return modemId;
    }

    /**
     * Set the value of the modemId property.
     * @param modemId
     */
    public void setModemId(String modemId) {
        this.modemId = modemId;
    }

	/**
     * Get the value of the meterId property.
     * @return String
     */
	public String getMeterId() {
		return meterId;
	}

    /**
     * Set the value of the meterId property.
     * @param modemId
     */
	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}
    
    /**
     * Get the value of the requestType property.
     * @return String
     */
    public String getRequestType() {
        return requestType;
    }

    /**
     * Set the value of the requestType property.
     * @param requestType
     */
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    /**
     * Get the value of the uArt property.
     * @return String
     */
	public int getuArt() {
		return uArt;
	}

    /**
     * Set the value of the uArt property.
     * @param requestType
     */
	public void setuArt(int uArt) {
		this.uArt = uArt;
	}
    
    
}
