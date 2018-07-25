package com.aimir.model.device;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.aimir.annotation.ColumnInfo;
import com.aimir.annotation.Scope;

/**
 * <p>Copyright NuriTelecom Co.Ltd. since 2009</p>
 * 
 * <p> Modem Meter Interface Unit </p>
 * 
 * @author YeonKyoung Park(goodjob)
 *
 */
@Entity
@DiscriminatorValue("MMIU")
public class MMIU extends Modem {

    private static final long serialVersionUID = -8856182359061451639L;

    @ColumnInfo(name="", view=@Scope(create=false, read=true, update=false), descr="")
    @Column(name="ERROR_STATUS", length=10)
    private Integer errorStatus;
    
    @ColumnInfo(name="", view=@Scope(read=true, update=true, devicecontrol=true), descr="Metering Schedule Day (Mask 4 Bytes)")
    @Column(name="METERING_DAY", length=255)
    private String meteringDay;

    @ColumnInfo(name="", view=@Scope(read=true, update=true, devicecontrol=true), descr="Metering Schedule Hour (Mask 12 Bytes)")
    @Column(name="METERING_HOUR", length=255)
    private String meteringHour;
    
    @ColumnInfo(name="", view=@Scope(read=true, update=true, devicecontrol=true),descr="모뎀이 올려주는 lp데이터가 오늘부터 몇일 전의 데이터인지 정의")
    @Column(name="LP_CHOICE", length=2)
    private Integer lpChoice;

    @ColumnInfo(name="APN address")
    @Column(name="APN_ADDRESS")
    private String apnAddress;
    
    @ColumnInfo(name="APN id")
    @Column(name="APN_ID")
    private String apnId;
    
    @ColumnInfo(name="APN password")
    @Column(name="APN_PASSWORD")
    private String apnPassword;
    
    @ColumnInfo(name="Reset Interval")
    @Column(name="RESET_INTERVAL")
    private Integer resetInterval;
    
    @ColumnInfo(name="Metering Interval")
    @Column(name="METERING_INTERVAL")
    private Integer meteringInterval;
    
    @ColumnInfo(name="ipv6Address", descr="IPv6 Modem Ipv6 Address")
    @Column(name="IPV6_ADDRESS", length=64)
    private String ipv6Address;

    public Integer getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(Integer errorStatus) {
        this.errorStatus = errorStatus;
    }    

	public String getMeteringDay() {
		return meteringDay;
	}

	public void setMeteringDay(String meteringDay) {
		this.meteringDay = meteringDay;
	}

	public String getMeteringHour() {
		return meteringHour;
	}

	public void setMeteringHour(String meteringHour) {
		this.meteringHour = meteringHour;
	}
	
	public Integer getLpChoice() {
		return lpChoice;
	}

	public void setLpChoice(Integer lpChoice) {
		this.lpChoice = lpChoice;
	}

    public String getApnAddress() {
        return apnAddress;
    }

    public void setApnAddress(String apnAddress) {
        this.apnAddress = apnAddress;
    }

    public String getApnId() {
        return apnId;
    }

    public void setApnId(String apnId) {
        this.apnId = apnId;
    }

    public String getApnPassword() {
        return apnPassword;
    }

    public void setApnPassword(String apnPassword) {
        this.apnPassword = apnPassword;
    }

    public Integer getResetInterval() {
        return resetInterval;
    }

    public void setResetInterval(Integer resetInterval) {
        this.resetInterval = resetInterval;
    }

    public Integer getMeteringInterval() {
        return meteringInterval;
    }

    public void setMeteringInterval(Integer meteringInterval) {
        this.meteringInterval = meteringInterval;
    } 
    
    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

}