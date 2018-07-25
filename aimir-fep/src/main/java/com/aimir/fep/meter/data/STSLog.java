package com.aimir.fep.meter.data;

/**
 * STS Token History
 *
 */
public class STSLog implements java.io.Serializable {

	private static final long serialVersionUID = -2516768444585321744L;

    private String tokenDate;
    private String writeDate;
    private Double lastCredit;
    private Double remainCredit;     
    private String tokenTid;
    private String tokenNumber;

	public String getTokenDate() {
		return tokenDate;
	}
	public void setTokenDate(String tokenDate) {
		this.tokenDate = tokenDate;
	}
	public String getWriteDate() {
		return writeDate;
	}
	public void setWriteDate(String writeDate) {
		this.writeDate = writeDate;
	}
	public Double getLastCredit() {
		return lastCredit;
	}
	public void setLastCredit(Double lastCredit) {
		this.lastCredit = lastCredit;
	}
	public Double getRemainCredit() {
		return remainCredit;
	}
	public void setRemainCredit(Double remainCredit) {
		this.remainCredit = remainCredit;
	}
	public String getTokenTid() {
		return tokenTid;
	}
	public void setTokenTid(String tokenTid) {
		this.tokenTid = tokenTid;
	}
	public String getTokenNumber() {
		return tokenNumber;
	}
	public void setTokenNumber(String tokenNumber) {
		this.tokenNumber = tokenNumber;
	}
}
