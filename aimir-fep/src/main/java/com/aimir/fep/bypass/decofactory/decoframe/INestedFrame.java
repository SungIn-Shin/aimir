/**
 * (@)# INestedFrame.java
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

import com.aimir.fep.bypass.decofactory.consts.DlmsConstants.XDLMS_APDU;
import com.aimir.fep.bypass.decofactory.consts.HdlcConstants.HdlcObjectType;
import com.aimir.fep.bypass.decofactory.protocolfactory.BypassFrameFactory.Procedure;

/**
 * @author simhanger
 *
 */
public interface INestedFrame {

	public byte[] encode(HdlcObjectType getReq, Procedure procedure, HashMap<String, Object> param, String command);

	public boolean decode(byte[] rawFrame, Procedure procedure, String command);

	public Object getResultData();

	public void setResultData(Object resultData);

	public int getType();

	public void setType(int type);
	
	public String getMeterId();
	
	public void setMeterId(String meterId);

	public String toByteString();
	
	public void setMeterRSCount(int[] rsCount);
	
	public int[] getMeterRSCount();

	public void setDlmsApdu(XDLMS_APDU dlmsApdu);

	public XDLMS_APDU getDlmsApdu();
	/**
	 * 기존 Decode 방식이 아닌 다른 방식으로 처리해야할경우 사용
	 * @param getResponseAssamblyDataParsing
	 * @param dataBlockArrayOfGetRes
	 * @return
	 */
	public Object customDecode(Procedure procedure, byte[] data);
		
	public void setMeterModelName(String meterModel);
	
	public boolean isProtocalHLS();

}