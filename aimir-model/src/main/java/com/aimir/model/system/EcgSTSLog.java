package com.aimir.model.system;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.aimir.annotation.ColumnInfo;
import com.aimir.model.BaseObject;
/**
 * <p>Copyright NuriTelecom Co.Ltd. since 2016</p>
 * <pre>
 * 선불 STS 연동 로그
 * </pre>
 * @author 박종성(elevas)
 */
@Entity
@Table(name = "ECG_STS_LOG")
public class EcgSTSLog extends BaseObject {

	private static final long serialVersionUID = 5724116943119871870L;

	@EmbeddedId public EcgSTSLogPk id;
    
    @ColumnInfo(name="결제모드 0:Disable 1:Manual Off, 2:Manual On, 3:Prepaid")
    private Integer payMode;
    
    @ColumnInfo(name="처리유무")
    private Integer result;
    
    @ColumnInfo(name="실패 사유")
    private String failReason;
    
    @Column(length = 14)
    @ColumnInfo(name="처리일시")
    private String resultDate;
    
    @ColumnInfo(name="tokenDate")
    private String tokenDate;
    
    @ColumnInfo(name="token")
    private String token;
    
    @ColumnInfo(name="chargedCredit")
    private Double chargedCredit;
    
    @Column(length = 14)
    @ColumnInfo(name="조회일시")
    private String getDate;
    
    @ColumnInfo(name="EmergencyCreditMode")
    private Integer emergencyCreditMode;
    
    @ColumnInfo(name="EmergencyCreditDay")
    private Integer emergencyCreditDay;
    
    @ColumnInfo(name="emergencyCreditAmount")
    private Double emergencyCreditAmount;
    
    @ColumnInfo(name="Tariff 모드")
    private Integer tariffMode;
    
    @ColumnInfo(name="Tariff 종류 0:Residential, 1:Non-residential")
    private Integer tariffKind;
    
    @ColumnInfo(name="Tariff 개수")
    private Integer tariffCount;
    
    @ColumnInfo(name="cond_limit1", descr="Consumption limit for condRate1 (kWh)")
    private String condLimit1;
    
    @ColumnInfo(name="cond_limit2", descr="Consumption limit for condRate2 (kWh)")
    private String condLimit2;
    
    @ColumnInfo(name="consumption", descr="Threshold, Supply Size")
    private String consumption;
    
    @ColumnInfo(name="fixedRate", descr="Service Charge")
    private String fixedRate;

    @ColumnInfo(name="varRate")
    private String varRate;
    
    @ColumnInfo(name="condRate1", descr="Gov Subsidy, Old Subsidy")
    private String condRate1;
    
    @ColumnInfo(name="condRate2", descr="Utility Relief")
    private String condRate2;
    
    @Column(length = 8)
    @ColumnInfo(name="Tariff 적용시기")
    private String tariffDate;
    
    @ColumnInfo(name="잔액 기준일")
    private String remainingCreditDate;
    
    @ColumnInfo(name="현재 잔액")
    private Double remainingCredit;
    
    @ColumnInfo(name="NETCHARGE_YYYYMM")
    private String netChargeYyyymm;
    
    @ColumnInfo(name="월 사용량")
    private Integer netChargeMonthConsumption;
    
    @ColumnInfo(name="월 요금")
    private Double netChargeMonthCost; 
    
    @ColumnInfo(name="NETCHARGE_YYYYMMDD")
    private String netChargeYyyymmdd;
    
    @ColumnInfo(name="일 사용량")
    private Integer netChargeDayConsumption;
    
    @ColumnInfo(name="일 요금")
    private Double netChargeDayCost;
    
    @ColumnInfo(name="fcMode")
    private Integer fcMode;
    
    @ColumnInfo(name="friendlyDate")
    private String friendlyDate;
    
    @ColumnInfo(name="friendlyDayType")
    private String friendlyDayType;
    
    @ColumnInfo(name="friendlyFromHHMM")
    private String friendlyFromHHMM;
    
    @ColumnInfo(name="friendlyEndHHMM")
    private String friendlyEndHHMM;
    
    @ColumnInfo(name="friendlyCreditAmount")
    private Double friendlyCreditAmount;
    
    @ColumnInfo(name="stsNumber")
    private String stsNumber;
    
    @ColumnInfo(name="kct1")
    private String kct1;
    
    @ColumnInfo(name="kct2")
    private String kct2;
    
    @ColumnInfo(name="channel")
    private Integer channel;
    
    @ColumnInfo(name="pan_id")
    private Integer panId;
    
    // NGM-101
    @ColumnInfo(name="activeEnergyCharge", descr="전기 사용량")
    @Column(name="ACTIVE_ENERGY_CHARGE")
    private String activeEnergyCharge;
    
    @ColumnInfo(name="govLey", descr="Goverment Levy")
    @Column(name="GOV_LEVY")
    private String govLey;
    
    @ColumnInfo(name="streetLightLevy", descr="Public Levy")
    @Column(name="STREET_LIGHT_LEVY")
    private String streetLightLevy;
    
    @ColumnInfo(name="vat", descr="부가가치세(상업용)")
    @Column(name="VAT")
    private String vat;
    
    @ColumnInfo(name="lifeLineSubsidy", descr="기본사용 공제(가정용)")
    @Column(name="LIFELINE_SUBSIDY")
    private String lifeLineSubsidy;
    
    @ColumnInfo(name="switchTime")
    @Column(name="SWITCH_TIME")
    private String switchTime;
    
    
    public EcgSTSLog() {
        id = new EcgSTSLogPk();
    }
    
    public EcgSTSLogPk getId() {
        return id;
    }

    public void setId(EcgSTSLogPk id) {
        this.id = id;
    }

    public void setCmd(String cmd) {
        id.setCmd(cmd);
    }
    
    public String getCmd() {
        return id.getCmd();
    }
    
    public void setMeterNumber(String meterNumber) {
        id.setMeterNumber(meterNumber);
    }
    
    public void setCreateDate(String createDate) {
        id.setCreateDate(createDate);
    }
    
    public String getCreateDate() {
        return id.getCreateDate();
    }
    
    public void setSeq(int seq) {
        id.setSeq(seq);
    }
    
    public int getSeq() {
        return id.getSeq();
    }
    
    public int getPayMode() {
        return payMode;
    }

    public void setPayMode(Integer payMode) {
        this.payMode = payMode;
    }

    public int getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getResultDate() {
        return resultDate;
    }

    public void setResultDate(String resultDate) {
        this.resultDate = resultDate;
    }

    public String getTokenDate() {
        return tokenDate;
    }

    public void setTokenDate(String tokenDate) {
        this.tokenDate = tokenDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public double getChargedCredit() {
        return chargedCredit;
    }

    public void setChargedCredit(Double chargedCredit) {
        this.chargedCredit = chargedCredit;
    }

    public String getGetDate() {
        return getDate;
    }

    public void setGetDate(String getDate) {
        this.getDate = getDate;
    }

    public int getEmergencyCreditMode() {
        return emergencyCreditMode;
    }

    public void setEmergencyCreditMode(Integer emergencyCreditMode) {
        this.emergencyCreditMode = emergencyCreditMode;
    }

    public int getEmergencyCreditDay() {
        return emergencyCreditDay;
    }

    public void setEmergencyCreditDay(Integer emergencyCreditDay) {
        this.emergencyCreditDay = emergencyCreditDay;
    }
    
    public double getEmergencyCreditAmount() {
        return emergencyCreditAmount;
    }

    public void setEmergencyCreditAmount(double emergencyCreditAmount) {
        this.emergencyCreditAmount = emergencyCreditAmount;
    }

    public int getTariffMode() {
        return tariffMode;
    }

    public void setTariffMode(Integer tariffMode) {
        this.tariffMode = tariffMode;
    }

    public int getTariffKind() {
        return tariffKind;
    }

    public void setTariffKind(Integer tariffKind) {
        this.tariffKind = tariffKind;
    }

    public int getTariffCount() {
        return tariffCount;
    }

    public void setTariffCount(Integer tariffCount) {
        this.tariffCount = tariffCount;
    }

    public String getTariffDate() {
        return tariffDate;
    }

    public void setTariffDate(String tariffDate) {
        this.tariffDate = tariffDate;
    }
    
    public String getConsumption() {
		return consumption;
	}

	public void setConsumption(String consumption) {
		this.consumption = consumption;
	}

    public String getCondLimit1() {
		return condLimit1;
	}

	public void setCondLimit1(String condLimit1) {
		this.condLimit1 = condLimit1;
	}

	public String getCondLimit2() {
		return condLimit2;
	}

	public void setCondLimit2(String condLimit2) {
		this.condLimit2 = condLimit2;
	}

	public String getFixedRate() {
		return fixedRate;
	}

	public void setFixedRate(String fixedRate) {
		this.fixedRate = fixedRate;
	}

	public String getVarRate() {
		return varRate;
	}

	public void setVarRate(String varRate) {
		this.varRate = varRate;
	}

	public String getCondRate1() {
		return condRate1;
	}

	public void setCondRate1(String condRate1) {
		this.condRate1 = condRate1;
	}

	public String getCondRate2() {
		return condRate2;
	}

	public void setCondRate2(String condRate2) {
		this.condRate2 = condRate2;
	}
	
	@Override
    public boolean equals(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public long getAsyncTrId() {
        return id.getAsyncTrId();
    }

    public void setAsyncTrId(long asyncTrId) {
        id.setAsyncTrId(asyncTrId);
    }

    public String getRemainingCreditDate() {
        return remainingCreditDate;
    }

    public void setRemainingCreditDate(String remainingCreditDate) {
        this.remainingCreditDate = remainingCreditDate;
    }

    public Double getRemainingCredit() {
        return remainingCredit;
    }

    public void setRemainingCredit(Double remainingCredit) {
        this.remainingCredit = remainingCredit;
    }

    public String getNetChargeYyyymm() {
        return netChargeYyyymm;
    }

    public void setNetChargeYyyymm(String netChargeYyyymm) {
        this.netChargeYyyymm = netChargeYyyymm;
    }

    public double getNetChargeMonthConsumption() {
        return netChargeMonthConsumption;
    }

    public void setNetChargeMonthConsumption(Integer netChargeMonthConsumption) {
        this.netChargeMonthConsumption = netChargeMonthConsumption;
    }

    public double getNetChargeMonthCost() {
        return netChargeMonthCost;
    }

    public void setNetChargeMonthCost(Double netChargeMonthCost) {
        this.netChargeMonthCost = netChargeMonthCost;
    }

    public String getNetChargeYyyymmdd() {
        return netChargeYyyymmdd;
    }

    public void setNetChargeYyyymmdd(String netChargeYyyymmdd) {
        this.netChargeYyyymmdd = netChargeYyyymmdd;
    }

    public double getNetChargeDayConsumption() {
        return netChargeDayConsumption;
    }

    public void setNetChargeDayConsumption(Integer netChargeDayConsumption) {
        this.netChargeDayConsumption = netChargeDayConsumption;
    }

    public double getNetChargeDayCost() {
        return netChargeDayCost;
    }

    public void setNetChargeDayCost(Double netChargeDayCost) {
        this.netChargeDayCost = netChargeDayCost;
    }

    public int getFcMode() {
        return fcMode;
    }

    public void setFcMode(Integer fcMode) {
        this.fcMode = fcMode;
    }

    public String getFriendlyDate() {
        return friendlyDate;
    }

    public void setFriendlyDate(String friendlyDate) {
        this.friendlyDate = friendlyDate;
    }

    public String getFriendlyDayType() {
        return friendlyDayType;
    }

    public void setFriendlyDayType(String friendlyDayType) {
        this.friendlyDayType = friendlyDayType;
    }

    public String getFriendlyFromHHMM() {
        return friendlyFromHHMM;
    }

    public void setFriendlyFromHHMM(String friendlyFromHHMM) {
        this.friendlyFromHHMM = friendlyFromHHMM;
    }

    public String getFriendlyEndHHMM() {
        return friendlyEndHHMM;
    }

    public void setFriendlyEndHHMM(String friendlyEndHHMM) {
        this.friendlyEndHHMM = friendlyEndHHMM;
    }
    
    public double getFriendlyCreditAmount() {
        return friendlyCreditAmount;
    }

    public void setFriendlyCreditAmount(Double friendlyCreditAmount) {
        this.friendlyCreditAmount = friendlyCreditAmount;
    }
    
    public String getStsNumber() {
		return stsNumber;
	}

	public void setStsNumber(String stsNumber) {
		this.stsNumber = stsNumber;
	}

	public String getKct1() {
		return kct1;
	}

	public void setKct1(String kct1) {
		this.kct1 = kct1;
	}

	public String getKct2() {
		return kct2;
	}

	public void setKct2(String kct2) {
		this.kct2 = kct2;
	}

	public Integer getChannel() {
		return channel;
	}

	public void setChannel(Integer channel) {
		this.channel = channel;
	}

	public Integer getPanId() {
		return panId;
	}

	public void setPanId(Integer panId) {
		this.panId = panId;
	}
	
	public String getActiveEnergyCharge() {
		return activeEnergyCharge;
	}

	public void setActiveEnergyCharge(String activeEnergyCharge) {
		this.activeEnergyCharge = activeEnergyCharge;
	}
	
	public String getGovLey() {
		return govLey;
	}

	public void setGovLey(String govLey) {
		this.govLey = govLey;
	}
	
	public String getStreetLightLevy() {
		return streetLightLevy;
	}

	public void setStreetLightLevy(String streetLightLevy) {
		this.streetLightLevy = streetLightLevy;
	}
	
	public String getVat() {
		return vat;
	}

	public void setVat(String vat) {
		this.vat = vat;
	}
	
	public String getLifeLineSubsidy() {
		return lifeLineSubsidy;
	}

	public void setLifeLineSubsidy(String lifeLineSubsidy) {
		this.lifeLineSubsidy = lifeLineSubsidy;
	}
	
	public String getSwitchTime() {
		return switchTime;
	}

	public void setSwitchTime(String switchTime) {
		this.switchTime = switchTime;
	}

	@Override
    public String toString() {
        return "EcgSTSLog " + toJSONString();
    }

    public String toJSONString() {

        StringBuffer str = new StringBuffer();
        
        str.append("{"
            + "',cmd:'" + id.getCmd()
            + "', meterNumber:'" + this.id.getMeterNumber()
            + "', payMode:'" + this.payMode
            + "', createDate:'" + this.id.getCreateDate()
            + "', seq:'" + this.id.getSeq()
            + "', result:'" + this.result
            + "', failReason:'" + this.failReason
            + "', resultDate:'" + this.resultDate
            + "', tokenDate:'" + this.tokenDate
            + "', token:'" + this.token
            + "', chargedCredit:'" + this.chargedCredit
            + "', getDate:'" + this.getDate
            + "', emergencyCreditMode:'" + this.emergencyCreditMode
            + "', emergencyCreditDay:'" + this.emergencyCreditDay
            + "', emergencyCreditAmount:'" + this.emergencyCreditAmount
            + "', tariffMode:'" + this.tariffMode
            + "', tariffKind:'" + this.tariffKind
            + "', tariffCount:'" + this.tariffCount
            + "', tariffDate:'" + this.tariffDate
            + "', condLimit1:'" + this.condLimit1
            + "', condLimit2:'" + this.condLimit2
            + "', consumption:'" + this.consumption
            + "', fixedRate:'" + this.fixedRate
            + "', varRate:'" + this.varRate
            + "', condRate1:'" + this.condRate1
            + "', condRate2:'" + this.condRate2
            + "', asyncTrId:'" + id.getAsyncTrId()
            + "', remainingCreditDate:'" + this.remainingCreditDate
            + "', remainingCredit:'" + this.remainingCredit
            + "', netChargeYyyymm:'" + this.netChargeYyyymm
            + "', netChargeMonthConsumption:'" + this.netChargeMonthConsumption
            + "', netChargeMonthCost:'" + this.netChargeMonthCost
            + "', netChargeYyyymmdd:'" + this.netChargeYyyymmdd
            + "', netChargDayConsumption:'" + this.netChargeDayConsumption
            + "', netChargeDayCost:'" + this.netChargeDayCost
            + "', friendlyDate:'" + this.friendlyDate
            + "', friendlyDayType:'" + this.friendlyDayType
            + "', fcMode:'" + this.fcMode
            + "', friendlyFromHHMM:'" + this.friendlyFromHHMM
            + "', friendlyEndHHMM:'" + this.friendlyEndHHMM
            + "', friendlyCreditAmount:'" + this.friendlyCreditAmount
            + "', stsNumber:'" + this.stsNumber
            + "', kct1:'" + this.kct1
            + "', kct2:'" + this.kct2
            + "', channel:'" + this.channel
            + "', panId:'" + this.panId
            + "', activeEnergyCharge:'" + this.activeEnergyCharge
            + "', govLey:'" + this.govLey
            + "', streetLightLevy:'" + this.streetLightLevy
            + "', vat:'" + this.vat
            + "', lifeLineSubsidy:'" + this.lifeLineSubsidy
            + "', switchTime:'" + this.switchTime
            + "'}");
        
        return str.toString();
    }
}
