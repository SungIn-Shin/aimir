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
 * DLMS STS 미터를 사용하는 계약과 관련된 정보들을 관리하기 위한 부가 테이블.  
 * Contract의 id(contract_id)를 식별자로 한다. 따라서 각 계약별 row는 1개만 가질수 있다.(상황에 따라 변경가능)    
 * 2018.06.05 기준 , Tariff 전송 관리를 목적으로 사용한다.
 * 
 * @author SH LIM
 *
 */

@Entity
@Table(name="STS_DLMS_CONTRACT")
public class StsDlmsContract extends BaseObject implements JSONString, IAuditable{

	private static final long serialVersionUID = -6182097412223012799L;
	
	@Id
	@TableGenerator(name = "STS_DLMS_CON_GEN", table = "KEY_GEN_STS_DLMS_CON", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "STS_DLMS_CON_GEN")
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "contract_id")
    @ColumnInfo(name="계약")
	@ReferencedBy(name="contractNumber")
	private Contract contract;
	
	@Column(name="CONTRACT_ID", nullable=false, updatable=false, insertable=false)
	private Integer contractId;
	
	@Column(name="CONTRACT_NUMBER", nullable=false, unique=true)
    private String contractNumber;
	
	@Column(name="STS_DLMS_CONTRACT_DATE", nullable=false, length=14)
    @ColumnInfo(descr="STS DLMS Contract 생성일자")
    private String stsDlmsContractDate;
	
	@Column(name="MDEV_ID", nullable=false)
	private String mdevId;
	
    @Column(name="WRITE_DATE", nullable=false, length=14)
    @ColumnInfo(descr="데이터 기록 날짜")
    private String writeDate;
    
    @Column(name="CMD", nullable=true)
	private String cmd;

    @Column(name="TARIFF_INDEX_ID", nullable=true, updatable=false, insertable=false)
	private Integer tariffIndexId;
    
    @Column(name="CMD_TRANSFER_DATE", nullable=true, length=14)
    @ColumnInfo(descr="CMD 전송 성공시 날짜 기록")
    private String cmdTransferDate;
    
    @Column(name="CMD_SWITCH_TIME", nullable=true, length=14)
    @ColumnInfo(descr="SWITCH TIME 전송 성공시 SWITCH TIME 기록")
    private String cmdSwitchTime;
    
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
	
	public String getStsDlmsContractDate() {
		return stsDlmsContractDate;
	}

	public void setStsDlmsContractDate(String stsDlmsContractDate) {
		this.stsDlmsContractDate = stsDlmsContractDate;
	}	
	
	public String getMdevId() {
		return mdevId;
	}

	public void setMdevId(String mdevId) {
		this.mdevId = mdevId;
	}
	
	public String getWriteDate() {
		return writeDate;
	}

	public void setWriteDate(String writeDate) {
		this.writeDate = writeDate;
	}
	
	public Integer getTariffIndexId() {
		return tariffIndexId;
	}

	public void setTariffIndexId(Integer tariffIndexId) {
		this.tariffIndexId = tariffIndexId;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	
	public String getCmdTransferDate() {
		return cmdTransferDate;
	}

	public void setCmdTransferDate(String cmdTransferDate) {
		this.cmdTransferDate = cmdTransferDate;
	}
	
	public String getCmdSwitchTime() {
		return cmdSwitchTime;
	}

	public void setCmdSwitchTime(String cmdSwitchTime) {
		this.cmdSwitchTime = cmdSwitchTime;
	}

	@Override
	public String getInstanceName() {
		return getContractNumber();
	}
	
	@Override
	public String toString() {
		return "STS_DLMS_Contract " + toJSONString();
	}

	@Override
	public String toJSONString() {
		String retValue = "";
		
		retValue = "{"	  
				+ "id:'" + this.id				
				+ "',contractId:'" + this.contractId
				+ "',contractNumber:'" + this.contractNumber
				+ "',stsDlmsContractDate:'" + this.stsDlmsContractDate
				+ "',mdevId:'" + this.mdevId
				+ "',writeDate:'" + this.writeDate
				+ "',tariffIndexId:'" + this.tariffIndexId
				+ "',cmd:'" + this.cmd
				+ "',cmdTransferDate:'" + ((this.cmdTransferDate==null)?"null":this.cmdTransferDate)
				+ "',cmdSwitchTime:'" + ((this.cmdSwitchTime==null)?"null":this.cmdSwitchTime)
				+ "'}";
		
		return retValue;
	}

	@Override
	public boolean equals(Object o) {
		// Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		// Auto-generated method stub
		return 0;
	}

}
