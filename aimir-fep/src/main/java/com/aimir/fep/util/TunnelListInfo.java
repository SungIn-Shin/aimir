package com.aimir.fep.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tunnelListInfo", propOrder = {
    "sensorId",
    "msgTimeout",
    "tunnelTimeout",
    "portNumber",
    "createTime",
    "lastTimestamp",
    "sendPacket",
    "sendBytes",
    "recvPackets",
    "recvBytes"
})
public class TunnelListInfo implements java.io.Serializable {

	private String sensorId;
	
	private int msgTimeout;
	
	private int tunnelTimeout;
	
	private int portNumber;
	
	private String createTime;
	
	private String lastTimestamp;
	
	private String sendPacket;
	
	private String sendBytes;
	
	private String recvPackets;
	
	private String recvBytes;

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public int getMsgTimeout() {
		return msgTimeout;
	}

	public void setMsgTimeout(int msgTimeout) {
		this.msgTimeout = msgTimeout;
	}

	public int getTunnelTimeout() {
		return tunnelTimeout;
	}

	public void setTunnelTimeout(int tunnelTimeout) {
		this.tunnelTimeout = tunnelTimeout;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(String lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public String getSendPacket() {
		return sendPacket;
	}

	public void setSendPacket(String sendPacket) {
		this.sendPacket = sendPacket;
	}

	public String getSendBytes() {
		return sendBytes;
	}

	public void setSendBytes(String sendBytes) {
		this.sendBytes = sendBytes;
	}

	public String getRecvPackets() {
		return recvPackets;
	}

	public void setRecvPackets(String recvPackets) {
		this.recvPackets = recvPackets;
	}

	public String getRecvBytes() {
		return recvBytes;
	}

	public void setRecvBytes(String recvBytes) {
		this.recvBytes = recvBytes;
	}

	@Override
	public String toString() {
		return "TunnelListInfo [sensorId=" + sensorId + ", msgTimeout=" + msgTimeout + ", tunnelTimeout="
				+ tunnelTimeout + ", portNumber=" + portNumber + ", createTime=" + createTime + ", lastTimestamp="
				+ lastTimestamp + ", sendPacket=" + sendPacket + ", sendBytes=" + sendBytes + ", recvPackets="
				+ recvPackets + ", recvBytes=" + recvBytes + "]";
	}
	
	
	
	
}
