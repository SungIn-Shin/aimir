package com.aimir.model.system;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import com.aimir.annotation.ColumnInfo;
import com.aimir.model.BaseObject;

/**
 * 웹서비스로 인터페이스 즉 웹서비스를 통해 상호간 데이터를 전달하는 경우 해당 이력 정보를 가지고 있는 클래스
 * 
 *
 * @author 김재식(kaze)
 *
 */

@Entity
@DiscriminatorValue("WebService")
public class WebServiceLog extends BaseObject {
	private static final long serialVersionUID = -8065602399960316511L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WEBSERVICELOG_SEQ")
	@SequenceGenerator(name = "WEBSERVICELOG_SEQ", sequenceName = "WEBSERVICELOG_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "OPENTIME", length = 200)
	@ColumnInfo(descr = "웹서비스 호출 시간")
	private String openTime;

	@Column(name = "SERVER_REQUESTE_MSG", length = 4000)
	@ColumnInfo(descr = "웹서비스 서버로 동작할 때 전달 받은 메세지")
	private String serverRequsteMsg;

	@Column(name = "CLIENT_REQUESTE_MSG", length = 4000)
	@ColumnInfo(descr = "웹서비스 클라이언트로 동작할 때 전달한 메세지")
	private String clientRequstedMsg;

	@Column(name = "SERVER_RESPONSE_MSG", length = 4000)
	@ColumnInfo(descr = "웹서비스 서버로 동작할 때 호출에 대한 응답")
	private String serverResponseMsg;

	@Column(name = "CLIENT_RESPONSE_MSG", length = 4000)
	@ColumnInfo(descr = "웹서비스 클라이언트로 동작할 때 호출에 대한 응답")
	private String clientResponseMsg;

	@Column(name = "WEB_SERVICE_TYPE", length = 200)
	@ColumnInfo(descr = "웹서비스 종류")
	private String webServiceType;

	@Column(name = "SEQ")
	@ColumnInfo(descr = "CustomerDailyBillingInfos와 CustomerBillingInfos는 많은 Info 데이터를 지니고 있으므로 이것들을 하나씩 꺼낸 경우의 시퀀스를 기록한다.")
	private Integer seq;

	@Column(name = "ISSENDED")
	@ColumnInfo(descr = "웹서비스 DCU 전송 여부")
	private boolean isSended;

	@Column(name = "RESULT")
	@ColumnInfo(descr = "웹서비스 DCU 전송 결과")
	private boolean isResult;

	/**
	 * @return the openTime
	 */
	public String getOpenTime() {
		return openTime;
	}

	/**
	 * @param openTime
	 *            the openTime to set
	 */
	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}

	/**
	 * @return the serverRequsteMsg
	 */
	public String getServerRequsteMsg() {
		return serverRequsteMsg;
	}

	/**
	 * @param serverRequsteMsg
	 *            the serverRequsteMsg to set
	 */
	public void setServerRequsteMsg(String serverRequsteMsg) {
		this.serverRequsteMsg = serverRequsteMsg;
	}

	/**
	 * @return the clientRequstedMsg
	 */
	public String getClientRequstedMsg() {
		return clientRequstedMsg;
	}

	/**
	 * @param clientRequstedMsg
	 *            the clientRequstedMsg to set
	 */
	public void setClientRequstedMsg(String clientRequstedMsg) {
		this.clientRequstedMsg = clientRequstedMsg;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the serverResponseMsg
	 */
	public String getServerResponseMsg() {
		return serverResponseMsg;
	}

	/**
	 * @param serverResponseMsg
	 *            the serverResponseMsg to set
	 */
	public void setServerResponseMsg(String serverResponseMsg) {
		this.serverResponseMsg = serverResponseMsg;
	}

	/**
	 * @return the clientResponseMsg
	 */
	public String getClientResponseMsg() {
		return clientResponseMsg;
	}

	/**
	 * @param clientResponseMsg
	 *            the clientResponseMsg to set
	 */
	public void setClientResponseMsg(String clientResponseMsg) {
		this.clientResponseMsg = clientResponseMsg;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.aimir.model.BaseObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.aimir.model.BaseObject#hashCode()
	 */
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WebServiceLog [id=" + id + ", openTime=" + openTime + ", serverRequsteMsg=" + serverRequsteMsg + ", clientRequstedMsg=" + clientRequstedMsg + ", serverResponseMsg=" + serverResponseMsg + ", clientResponseMsg=" + clientResponseMsg
				+ ", webServiceType=" + webServiceType + ", seq=" + seq + ", isSended=" + isSended + ", isResult=" + isResult + "]";
	}

	public String getWebServiceType() {
		return webServiceType;
	}

	public void setWebServiceType(String webServiceType) {
		this.webServiceType = webServiceType;
	}

	/**
	 * @return the seq
	 */
	public Integer getSeq() {
		return seq;
	}

	/**
	 * @param seq the seq to set
	 */
	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	/**
	 * @return the isSended
	 */
	public boolean isSended() {
		return isSended;
	}

	/**
	 * @param isSended the isSended to set
	 */
	public void setSended(boolean isSended) {
		this.isSended = isSended;
	}

	/**
	 * @return the isResult
	 */
	public boolean isResult() {
		return isResult;
	}

	/**
	 * @param isResult the isResult to set
	 */
	public void setResult(boolean isResult) {
		this.isResult = isResult;
	}

}
