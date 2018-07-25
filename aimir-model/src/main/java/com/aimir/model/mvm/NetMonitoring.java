package com.aimir.model.mvm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlTransient;

import com.aimir.annotation.ColumnInfo;
import com.aimir.annotation.ReferencedBy;
import com.aimir.constants.CommonConstants.PeakType;
import com.aimir.model.mvm.Season;

/**
 * NetMonitoring ( DCU, Modem, Meter의 모든 검침정보를 모니터링하기위한 모델)
 * 
 * @author JiWoong Park(wll27471297)
 *
 */
@Entity
@Table(name = "NETWORK_MONITORING")
public class NetMonitoring {
	
	@Id
	private int id;
	
	@Column(name="ZRU_A24")
	@ColumnInfo(descr="ZRU Active within 24 hours")
	private String zruA24;
	@Column(name="ZRU_NA24")
	@ColumnInfo(descr="ZRU No activity 24~48 hours")
	private String zruNA24;
	@Column(name="ZRU_NA48")
	@ColumnInfo(descr="ZRU No activity over 48 hours")
	private String zruNA48;
	@Column(name="ZRU_UNKNOWN")
	@ColumnInfo(descr="ZRU Unknown")
	private String zruUnknown;
	
	@Column(name="PLCIU_A24")
	@ColumnInfo(descr="PLCIU Active within 24 hours")
	private String plciuA24;
	@Column(name="PLCIU_NA24")
	@ColumnInfo(descr="PLCIU No activity 24~48 hours")
	private String plciuNA24;
	@Column(name="PLCIU_NA48")
	@ColumnInfo(descr="PLCIU No activity over 48 hours")
	private String plciuNA48;
	@Column(name="PLCIU_UNKNOWN")
	@ColumnInfo(descr="PLCIU Unknown")
	private String plciuUnknown;
	
	@Column(name="ZEUPLS_A24")
	@ColumnInfo(descr="ZEUPLS Active within 24 hours")
	private String zeuplsA24;
	@Column(name="ZEUPLS_NA24")
	@ColumnInfo(descr="ZEUPLS No activity 24~48 hours")
	private String zeuplsNA24;
	@Column(name="ZEUPLS_NA48")
	@ColumnInfo(descr="ZEUPLS No activity over 48 hours")
	private String zeuplsNA48;
	@Column(name="ZEUPLS_UNKNOWN")
	@ColumnInfo(descr="ZEUPLS Unknown")
	private String zeuplsUnknown;
	
	@Column(name="MMIU_A24")
	@ColumnInfo(descr="MMIU Active within 24 hours")
	private String mmiuA24;
	@Column(name="MMIU_NA24")
	@ColumnInfo(descr="MMIU No activity 24~48 hours")
	private String mmiuNA24;
	@Column(name="MMIU_NA48")
	@ColumnInfo(descr="MMIU No activity over 48 hours")
	private String mmiuNA48;
	@Column(name="MMIU_UNKNOWN")
	@ColumnInfo(descr="MMIU Unknown")
	private String mmiuUnknown;
	
	@Column(name="IEIU_A24")
	@ColumnInfo(descr="IEIU Active within 24 hours")
	private String ieiuA24;
	@Column(name="IEIU_NA24")
	@ColumnInfo(descr="IEIU No activity 24~48 hours")
	private String ieiuNA24;
	@Column(name="IEIU_NA48")
	@ColumnInfo(descr="IEIU No activity over 48 hours")
	private String ieiuNA48;
	@Column(name="IEIU_UNKNOWN")
	@ColumnInfo(descr="IEIU Unknown")
	private String ieiuUnknown;
	
	@Column(name="INDOOR_A24")
	@ColumnInfo(descr="INDOOR Active within 24 hours")
	private String indoorA24;
	@Column(name="INDOOR_NA24")
	@ColumnInfo(descr="INDOOR No activity 24~48 hours")
	private String indoorNA24;
	@Column(name="INDOOR_NA48")
	@ColumnInfo(descr="INDOOR No activity over 48 hours")
	private String indoorNA48;
	@Column(name="INDOOR_UNKNOWN")
	@ColumnInfo(descr="INDOOR Unknown")
	private String indoorUnknown;
	
	@Column(name="DCU_A24")
	@ColumnInfo(descr="DCU Active within 24 hours")
	private String dcuA24;
	@Column(name="DCU_NA24")
	@ColumnInfo(descr="DCU No activity 24~48 hours")
	private String dcuNA24;
	@Column(name="DCU_NA48")
	@ColumnInfo(descr="DCU No activity over 48 hours")
	private String dcuNA48;
	@Column(name="DCU_UNKNOWN")
	@ColumnInfo(descr="DCU Unknown")
	private String dcuUnknown;
	
	@Column(name="ENERGYMETER_A24")
	@ColumnInfo(descr="ENERGYMETER Active within 24 hours")
	private String energymeterA24;
	@Column(name="ENERGYMETER_NA24")
	@ColumnInfo(descr="ENERGYMETER No activity 24~48 hours")
	private String energymeterNA24;
	@Column(name="ENERGYMETER_NA48")
	@ColumnInfo(descr="ENERGYMETER No activity over 48 hours")
	private String energymeterNA48;
	@Column(name="ENERGYMETER_UNKNOWN")
	@ColumnInfo(descr="ENERGYMETER Unknown")
	private String energymeterUnknown;

	@Column(name="ZBREPEATER_A24")
	@ColumnInfo(descr="ZBREPEATER Active within 24 hours")
	private String zbrepeaterA24;
	@Column(name="ZBREPEATER_NA24")
	@ColumnInfo(descr="ZBREPEATER No activity 24~48 hours")
	private String zbrepeaterNA24;
	@Column(name="ZBREPEATER_NA48")
	@ColumnInfo(descr="ZBREPEATER No activity over 48 hours")
	private String zbrepeaterNA48;
	@Column(name="ZBREPEATER_UNKNOWN")
	@ColumnInfo(descr="ZBREPEATER Unknown")
	private String zbrepeaterUnknown;
	
	@Column(name="METER_SUCCESS")
	@ColumnInfo(descr="Meter Success Count.")
	private String meterSuccess;
	@Column(name="METER_TOTAL")
	@ColumnInfo(descr="Meter Total Count.")
	private String meterTotal;
	
	@Column(name="CONTRACT_SUCCESS")
	@ColumnInfo(descr="Contract Success Count.")
	private String contractSuccess;
	@Column(name="CONTRACT_TOTAL")
	@ColumnInfo(descr="Contract Total Count.")
	private String contractTotal;
	
	@Column(name="YYYYMMDDHHMMSS")
	@ColumnInfo(descr="Date")
	private String yyyymmddhhmmss;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMeterSuccess() {
		return meterSuccess;
	}

	public void setMeterSuccess(String meterSuccess) {
		this.meterSuccess = meterSuccess;
	}

	public String getMeterTotal() {
		return meterTotal;
	}

	public void setMeterTotal(String meterTotal) {
		this.meterTotal = meterTotal;
	}

	public String getContractSuccess() {
		return contractSuccess;
	}

	public void setContractSuccess(String contractSuccess) {
		this.contractSuccess = contractSuccess;
	}

	public String getContractTotal() {
		return contractTotal;
	}

	public void setContractTotal(String contractTotal) {
		this.contractTotal = contractTotal;
	}

	public String getYyyymmddhhmmss() {
		return yyyymmddhhmmss;
	}

	public void setYyyymmddhhmmss(String yyyymmddhhmmss) {
		this.yyyymmddhhmmss = yyyymmddhhmmss;
	}

	public String getZruA24() {
		return zruA24;
	}

	public void setZruA24(String zruA24) {
		this.zruA24 = zruA24;
	}

	public String getZruNA24() {
		return zruNA24;
	}

	public void setZruNA24(String zruNA24) {
		this.zruNA24 = zruNA24;
	}

	public String getZruNA48() {
		return zruNA48;
	}

	public void setZruNA48(String zruNA48) {
		this.zruNA48 = zruNA48;
	}

	public String getZruUnknown() {
		return zruUnknown;
	}

	public void setZruUnknown(String zruUnknown) {
		this.zruUnknown = zruUnknown;
	}

	public String getPlciuA24() {
		return plciuA24;
	}

	public void setPlciuA24(String plciuA24) {
		this.plciuA24 = plciuA24;
	}

	public String getPlciuNA24() {
		return plciuNA24;
	}

	public void setPlciuNA24(String plciuNA24) {
		this.plciuNA24 = plciuNA24;
	}

	public String getPlciuNA48() {
		return plciuNA48;
	}

	public void setPlciuNA48(String plciuNA48) {
		this.plciuNA48 = plciuNA48;
	}

	public String getPlciuUnknown() {
		return plciuUnknown;
	}

	public void setPlciuUnknown(String plciuUnknown) {
		this.plciuUnknown = plciuUnknown;
	}

	public String getZeuplsA24() {
		return zeuplsA24;
	}

	public void setZeuplsA24(String zeuplsA24) {
		this.zeuplsA24 = zeuplsA24;
	}

	public String getZeuplsNA24() {
		return zeuplsNA24;
	}

	public void setZeuplsNA24(String zeuplsNA24) {
		this.zeuplsNA24 = zeuplsNA24;
	}

	public String getZeuplsNA48() {
		return zeuplsNA48;
	}

	public void setZeuplsNA48(String zeuplsNA48) {
		this.zeuplsNA48 = zeuplsNA48;
	}

	public String getZeuplsUnknown() {
		return zeuplsUnknown;
	}

	public void setZeuplsUnknown(String zeuplsUnknown) {
		this.zeuplsUnknown = zeuplsUnknown;
	}

	public String getMmiuA24() {
		return mmiuA24;
	}

	public void setMmiuA24(String mmiuA24) {
		this.mmiuA24 = mmiuA24;
	}

	public String getMmiuNA24() {
		return mmiuNA24;
	}

	public void setMmiuNA24(String mmiuNA24) {
		this.mmiuNA24 = mmiuNA24;
	}

	public String getMmiuNA48() {
		return mmiuNA48;
	}

	public void setMmiuNA48(String mmiuNA48) {
		this.mmiuNA48 = mmiuNA48;
	}

	public String getMmiuUnknown() {
		return mmiuUnknown;
	}

	public void setMmiuUnknown(String mmiuUnknown) {
		this.mmiuUnknown = mmiuUnknown;
	}

	public String getIeiuA24() {
		return ieiuA24;
	}

	public void setIeiuA24(String ieiuA24) {
		this.ieiuA24 = ieiuA24;
	}

	public String getIeiuNA24() {
		return ieiuNA24;
	}

	public void setIeiuNA24(String ieiuNA24) {
		this.ieiuNA24 = ieiuNA24;
	}

	public String getIeiuNA48() {
		return ieiuNA48;
	}

	public void setIeiuNA48(String ieiuNA48) {
		this.ieiuNA48 = ieiuNA48;
	}

	public String getIeiuUnknown() {
		return ieiuUnknown;
	}

	public void setIeiuUnknown(String ieiuUnknown) {
		this.ieiuUnknown = ieiuUnknown;
	}

	public String getIndoorA24() {
		return indoorA24;
	}

	public void setIndoorA24(String indoorA24) {
		this.indoorA24 = indoorA24;
	}

	public String getIndoorNA24() {
		return indoorNA24;
	}

	public void setIndoorNA24(String indoorNA24) {
		this.indoorNA24 = indoorNA24;
	}

	public String getIndoorNA48() {
		return indoorNA48;
	}

	public void setIndoorNA48(String indoorNA48) {
		this.indoorNA48 = indoorNA48;
	}

	public String getIndoorUnknown() {
		return indoorUnknown;
	}

	public void setIndoorUnknown(String indoorUnknown) {
		this.indoorUnknown = indoorUnknown;
	}

	public String getDcuA24() {
		return dcuA24;
	}

	public void setDcuA24(String dcuA24) {
		this.dcuA24 = dcuA24;
	}

	public String getDcuNA24() {
		return dcuNA24;
	}

	public void setDcuNA24(String dcuNA24) {
		this.dcuNA24 = dcuNA24;
	}

	public String getDcuNA48() {
		return dcuNA48;
	}

	public void setDcuNA48(String dcuNA48) {
		this.dcuNA48 = dcuNA48;
	}

	public String getDcuUnknown() {
		return dcuUnknown;
	}

	public void setDcuUnknown(String dcuUnknown) {
		this.dcuUnknown = dcuUnknown;
	}

	public String getEnergymeterA24() {
		return energymeterA24;
	}

	public void setEnergymeterA24(String energymeterA24) {
		this.energymeterA24 = energymeterA24;
	}

	public String getEnergymeterNA24() {
		return energymeterNA24;
	}

	public void setEnergymeterNA24(String energymeterNA24) {
		this.energymeterNA24 = energymeterNA24;
	}

	public String getEnergymeterNA48() {
		return energymeterNA48;
	}

	public void setEnergymeterNA48(String energymeterNA48) {
		this.energymeterNA48 = energymeterNA48;
	}

	public String getEnergymeterUnknown() {
		return energymeterUnknown;
	}

	public void setEnergymeterUnknown(String energymeterUnknown) {
		this.energymeterUnknown = energymeterUnknown;
	}
	
	public String getZbrepeaterA24() {
		return zbrepeaterA24;
	}

	public void setZbrepeaterA24(String zbrepeaterA24) {
		this.zbrepeaterA24 = zbrepeaterA24;
	}

	public String getZbrepeaterNA24() {
		return zbrepeaterNA24;
	}

	public void setZbrepeaterNA24(String zbrepeaterNA24) {
		this.zbrepeaterNA24 = zbrepeaterNA24;
	}

	public String getZbrepeaterNA48() {
		return zbrepeaterNA48;
	}

	public void setZbrepeaterNA48(String zbrepeaterNA48) {
		this.zbrepeaterNA48 = zbrepeaterNA48;
	}

	public String getZbrepeaterUnknown() {
		return zbrepeaterUnknown;
	}

	public void setZbrepeaterUnknown(String zbrepeaterUnknown) {
		this.zbrepeaterUnknown = zbrepeaterUnknown;
	}
	
}
