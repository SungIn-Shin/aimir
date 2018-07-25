package com.aimir.model.system;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.aimir.annotation.ColumnInfo;
import com.aimir.annotation.ReferencedBy;
import com.aimir.audit.IAuditable;
import com.aimir.model.BaseObject;

import net.sf.json.JSONString;

/**
 * STS 미터를 사용하는 계약과 관련된 정보들을 관리하기 위한 부가 테이블.  
 * Contract의 id(contract_id)를 식별자로 한다. 따라서 각 계약별 row는 1개만 가질수 있다.(상황에 따라 변경가능)    
 * 2017.10.15 기준 Initial Token과 Tariff 전송 관리를 목적으로 사용한다.
 * 
 * @author SeJin Han
 *
 */

/*@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stscontract", propOrder = {
		
})*/

@Entity
@Table(name="STSCONTRACT")
/*@org.hibernate.annotations.Table(appliesTo = "STSCONTRACT", 
indexes={
    @org.hibernate.annotations.Index(name="IDX_STSCONTRACT_01", columnNames={"CONTRACT_ID"}),
    @org.hibernate.annotations.Index(name="IDX_STSCONTRACT_02", columnNames={"INIT_TOKEN_DATE"}),
    @org.hibernate.annotations.Index(name="IDX_STSCONTRACT_03", columnNames={"CURRENT_TARIFF_DATE"}),
    @org.hibernate.annotations.Index(name="IDX_STSCONTRACT_04", columnNames={"FUTURE_TARIFF_DATE"})
}) 나중에 운영 후 주요 검색조건이 마련되면 인덱스 설정*/
public class StsContract extends BaseObject implements JSONString, IAuditable{

	private static final long serialVersionUID = 2238398092334026806L;
	
	@Id
	@TableGenerator(name="STSCON_GEN", table="KEY_GEN_STSCON", allocationSize=1) 
    @GeneratedValue(strategy=GenerationType.TABLE, generator="STSCON_GEN")
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "contract_id")
    @ColumnInfo(name="계약")
	@ReferencedBy(name="contractNumber")
	private Contract contract;
	
	@Column(name="contract_id", nullable=true, updatable=false, insertable=false)
	private Integer contractId;
	
	@Column(name="CONTRACT_NUMBER", unique=true)
    private String contractNumber;
	
	@Column(name="MDEV_ID")
	private String mdevId;
	
    @Column(name="STS_NUMBER")
    private String stsNumber;
    
    //----- 초기 토큰 관리 (OPF-253) ------------------|
    @Column(name="INIT_TOKEN")
    private String initToken;
	
    @Column(name="INIT_TOKEN_DATE", length=14)
    @ColumnInfo(descr="초기 토큰을 전송 성공한 시간")
    private String initTokenDate;   
    //-------------------------------------------------|
    
    //----- 현재 적용될 타리프 관리 -------------------|
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CURRENTTARIFFINDEX_ID")
	private TariffType currentTariffType;	
	
	@Column(name="CURRENTTARIFFINDEX_ID", nullable=true, updatable=false, insertable=false)
	private Integer currentTariffIndexId;
	
	@Column(name="CURRENT_TARIFF", length=8)
    @ColumnInfo(descr="현재 타리프의 시행날짜")
    private String currentTariff;  
	
	@Column(name="CURRENT_TARIFF_DATE", length=14)
    @ColumnInfo(descr="현재 타리프를 전송 성공한 시간")
    private String currentTariffDate;  
	//-------------------------------------------------|
	
	//----- 추후 변경될 타리프 관리 -------------------|  
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FUTURETARIFFINDEX_ID")
	private TariffType futureTariffType;	
	
	@Column(name="FUTURETARIFFINDEX_ID", nullable=true, updatable=false, insertable=false)
	private Integer futureTariffIndexId;
	
	@Column(name="FUTURE_TARIFF", length=8)
    @ColumnInfo(descr="다음 타리프의 시행날짜")
    private String futureTariff;  
	
	@Column(name="FUTURE_TARIFF_DATE", length=14)
    @ColumnInfo(descr="다음 타리프를 전송 성공한 시간")
    private String futureTariffDate;  
	//-------------------------------------------------|
	
	@Column(name="VALIDTARGET")
	@ColumnInfo(descr="필요시 Task 대상에서 제외할 수 있도록 구분할 옵션")
	private Boolean validTarget;
	
	@Column(name="STSCONTRACT_DATE", length=14)
    @ColumnInfo(descr="STSContract 생성일자")
    private String stsContractDate;  
	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public Integer getContractId() {
		return contractId;
	}

	public void setContractId(Integer contractId) {
		this.contractId = contractId;
	}

	public String getContractNumber() {
		return contractNumber;
	}

	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	public String getMdevId() {
		return mdevId;
	}

	public void setMdevId(String mdevId) {
		this.mdevId = mdevId;
	}

	public String getStsNumber() {
		return stsNumber;
	}

	public void setStsNumber(String stsNumber) {
		this.stsNumber = stsNumber;
	}

	public String getInitToken() {
		return initToken;
	}

	public void setInitToken(String initToken) {
		this.initToken = initToken;
	}

	public String getInitTokenDate() {
		return initTokenDate;
	}

	public void setInitTokenDate(String initTokenDate) {
		this.initTokenDate = initTokenDate;
	}

	public TariffType getCurrentTariffType() {
		return currentTariffType;
	}

	public void setCurrentTariffType(TariffType currentTariffType) {
		this.currentTariffType = currentTariffType;
	}

	public Integer getCurrentTariffIndexId() {
		return currentTariffIndexId;
	}

	public void setCurrentTariffIndexId(Integer currentTariffIndexId) {
		this.currentTariffIndexId = currentTariffIndexId;
	}

	public String getCurrentTariff() {
		return currentTariff;
	}

	public void setCurrentTariff(String currentTariff) {
		this.currentTariff = currentTariff;
	}

	public String getCurrentTariffDate() {
		return currentTariffDate;
	}

	public void setCurrentTariffDate(String currentTariffDate) {
		this.currentTariffDate = currentTariffDate;
	}

	public TariffType getFutureTariffType() {
		return futureTariffType;
	}

	public void setFutureTariffType(TariffType futureTariffType) {
		this.futureTariffType = futureTariffType;
	}

	public Integer getFutureTariffIndexId() {
		return futureTariffIndexId;
	}

	public void setFutureTariffIndexId(Integer futureTariffIndexId) {
		this.futureTariffIndexId = futureTariffIndexId;
	}

	public String getFutureTariff() {
		return futureTariff;
	}

	public void setFutureTariff(String futureTariff) {
		this.futureTariff = futureTariff;
	}

	public String getFutureTariffDate() {
		return futureTariffDate;
	}

	public void setFutureTariffDate(String futureTariffDate) {
		this.futureTariffDate = futureTariffDate;
	}

	public Boolean getValidTarget() {
		return validTarget;
	}

	public void setValidTarget(Boolean validTarget) {
		this.validTarget = validTarget;
	}
	
	public String getStsContractDate() {
		return stsContractDate;
	}

	public void setStsContractDate(String stsContractDate) {
		this.stsContractDate = stsContractDate;
	}
	

	@Override
	public String getInstanceName() {
		return getContractNumber();
	}
	
	@Override
	public String toString() {
		return "STS_Contract "+toJSONString();
	}

	@Override
	public String toJSONString() {
		String retValue = "";
		
		retValue = "{"	  
				+ "id:'" + this.id				
				+ "',contractId:'" + ((this.contractId==null)? "null":this.contractId)
				+ "',contractNumber:'" + ((this.contractNumber==null)?"null":this.contractNumber)
				+ "',mdevId:'" + ((this.mdevId==null)?"null":this.mdevId)
				+ "',stsNumber:'" + ((this.stsNumber==null)?"null":this.stsNumber)
				+ "',initToken:'" + ((this.initToken==null)?"null":this.initToken)
				+ "',initTokenDate:'" + ((this.initTokenDate==null)?"null":this.initTokenDate)
				+ "',currentTariffIndexId:'" + ((this.currentTariffIndexId==null)?"null":this.currentTariffIndexId)
				+ "',currentTariff:'" + ((this.currentTariff==null)?"null":this.currentTariff)
				+ "',currentTariffDate:'" + ((this.currentTariffDate==null)?"null":this.currentTariffDate)
				+ "',futureTariffIndexId:'" + ((this.futureTariffIndexId==null)?"null":this.futureTariffIndexId)
				+ "',futuretTariff:'" + ((this.futureTariff==null)?"null":this.futureTariff)
				+ "',futuretTariffDate:'" + ((this.futureTariffDate==null)?"null":this.futureTariffDate)
				+ "',validTarget:'" +  ((this.validTarget==null)?"null":this.validTarget.toString() )
				+ "'}";
		return retValue;
	}


	@Override
	public boolean equals(Object o) {
		//  Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		//  Auto-generated method stub
		return 0;
	}

	
}
