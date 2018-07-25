/**
 * (@)# GD_DLMSFrame.java
 *
 * 2016. 4. 15.
 *
 * Copyright (c) 2013 NURITELECOM, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of 
 * NURITELECOM, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with NURITELECOM, Inc.
 *
 * For more information on this product, please see
 * http://www.nuritelecom.co.kr
 *
 */
package com.aimir.fep.bypass.decofactory.decoframe;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aimir.fep.bypass.decofactory.consts.DlmsConstants.XDLMS_APDU;
import com.aimir.fep.bypass.decofactory.consts.HdlcConstants.HdlcObjectType;
import com.aimir.fep.bypass.decofactory.protocolfactory.BypassFrameFactory.Procedure;
import com.aimir.fep.util.Hex;

/**
 * @author simhanger
 *
 */
public class GD_DLMSFrame implements INestedFrame {
	private static Logger logger = LoggerFactory.getLogger(GD_DLMSFrame.class);

	private byte[] gdDLMSFrame = null;
	private HdlcObjectType controlType;
	public Object resultData;

	@Override
	public byte[] encode(HdlcObjectType hdlcType, Procedure procedure, HashMap<String, Object> param, String command) {
		logger.debug("## Excute GD_DLMSFrame Encoding [" + hdlcType.name() + "]");

		switch (hdlcType) {
		case SNRM:
			gdDLMSFrame = new byte[] {};
			break;
		case AARQ:
			gdDLMSFrame = new byte[] {};
			break;
		case ACTION_REQ:
			gdDLMSFrame = new byte[] {};
			break;
		case DISC:
			gdDLMSFrame = new byte[] {};
			break;
		default:
			break;
		}

		return gdDLMSFrame;
	}

	@Override
	public boolean decode(byte[] frame, Procedure procedure, String command) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toByteString() {
		return Hex.decode(gdDLMSFrame);
	}

	@Override
	public int getType() {
		return HdlcObjectType.getItem(controlType);
	}

	@Override
	public void setType(int type) {
		controlType = HdlcObjectType.getItem(type);
	}

	@Override
	public Object getResultData() {
		return resultData;
	}

	@Override
	public void setResultData(Object resultData) {
		this.resultData = resultData;
	}

	@Override
	public Object customDecode(Procedure procedure, byte[] data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMeterId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMeterId(String meterId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMeterRSCount(int[] rsCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] getMeterRSCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDlmsApdu(XDLMS_APDU dlmsApdu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public XDLMS_APDU getDlmsApdu() {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void setMeterModelName(String meterModel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isProtocalHLS() {
		// TODO Auto-generated method stub
		return false;
	}


}
