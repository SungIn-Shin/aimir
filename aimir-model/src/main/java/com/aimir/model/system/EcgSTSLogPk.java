package com.aimir.model.system;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.aimir.annotation.ColumnInfo;
import com.aimir.model.BasePk;

/**
 *  <p>Copyright NuriTelecom Co.Ltd. since 2009</p>
 *  <pre>
 * 에너지 절감 목표를 위한 평균 사용량 AverageUsageBase Entity Class에 대한 Primary Key 정보
 * 복합키를 사용하기 때문에 XXXXPk 클래스로 별도 선언함.
 * </pre>
 * @author 박종성(elevas)
 */
@Embeddable
public class EcgSTSLogPk extends BasePk {

    private static final long serialVersionUID = -3777254222843823030L;

    @Column(name="CMD", nullable=false)
    private String cmd;
    
    @Column(name="METER_NUMBER", nullable=false)
	private String meterNumber;	

	@Column(name="CREATE_DATE", length=14, nullable=false)
	private String createDate;

	@Column(name="ASYNC_TR_ID", nullable=false)
	@ColumnInfo(name="비동기 내역의 트랜잭션 아이디")
    private long asyncTrId;
	
	@Column(name="SEQ", nullable=false)
	private int seq;
    
	
    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public long getAsyncTrId() {
        return asyncTrId;
    }

    public void setAsyncTrId(long asyncTrId) {
        this.asyncTrId = asyncTrId;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

}