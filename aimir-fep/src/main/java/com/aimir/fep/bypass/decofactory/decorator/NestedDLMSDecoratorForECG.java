/**
 * (@)# NestedDLMSDecoratorForECG.java
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
package com.aimir.fep.bypass.decofactory.decorator;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsmpp.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aimir.fep.bypass.decofactory.consts.DlmsConstants.XDLMS_APDU;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.AARE;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.AARQ;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.AARQ_LLS;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.ASSOCIATION_LN;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.ActionRequest;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.ActionResponse;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.ActionResult;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.DLMSCommonDataType;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.DataAccessResult;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.DemandPeriodAttributes;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.DlmsPiece;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.FriendlyTimeAttributes;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.FriendlyWeekAttributes;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.GetDataResult;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.GetRequest;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.GetResponse;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.ImageTransfer;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.ImageTransferAttributes;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.ImageTransferMethods;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.MeterBillingCycleAttributes;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.MeterFWInfoAttributes;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.MeterParamSetMethods;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.MeterRelayMethods;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.SetRequest;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.SetResponse;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.TOUAttributes;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.TOUInfoBlockType;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.TokenCreditEventAttributes;
import com.aimir.fep.bypass.decofactory.consts.HLSAuthForECG;
import com.aimir.fep.bypass.decofactory.consts.HLSAuthForECG.HLSSecurity;
import com.aimir.fep.bypass.decofactory.consts.HdlcConstants.HdlcObjectType;
import com.aimir.fep.bypass.decofactory.decoframe.INestedFrame;
import com.aimir.fep.bypass.decofactory.protocolfactory.BypassFrameFactory.Procedure;
import com.aimir.fep.bypass.dlms.enums.DataType;
import com.aimir.fep.bypass.dlms.enums.ObjectType;
import com.aimir.fep.command.conf.DLMSMeta.CONTROL_STATE;
import com.aimir.fep.command.mbean.CommandGW;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.Hex;
import com.aimir.util.DateTimeUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;

/**
 * @author simhanger
 *
 */
public class NestedDLMSDecoratorForECG extends NestFrameDecorator {
	private static Logger logger = LoggerFactory.getLogger(NestedDLMSDecoratorForECG.class);

	private List<HashMap<String, Object>> channelList = new ArrayList<HashMap<String, Object>>();
	//private List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
	private byte[] lpRawData; // Load Profile 정보를 수집하기위한 Procedure에서 사용.

	private byte[] gdDLMSFrame = null;
	/**
	 * ActionReq 시 필요한 정보.
	 */
	private byte[] aareAuthenticationValue = null; // AARE로 받은 S to C
	//static private byte[] aareRespondingAPtitle = null; // AARE로 받은 Server System Title. Action Response Validation시 필요. 
	private byte[] aareRespondingAPtitle = null; // AARE로 받은 Server System Title. Action Response Validation시 필요.
	static private byte[] aareAssociationResult = null; //LLS일 경우 사용
	
	private byte[] rawCipheringData = null;;
	
	/**
	 * 호출할때마다 1씩 증가시킨값
	 * 
	 * @return
	 */
//	private static int hdlcInvoCounter = 0;
//	public static byte[] getInvoCounter() {
//		return DataUtil.get4ByteToInt(++hdlcInvoCounter);
//	}
	private int hdlcInvoCounter = 0;
	public byte[] getInvoCounter() {
		return DataUtil.get4ByteToInt(++hdlcInvoCounter);
	}

	/*
	 * REQUEST_INVOKE_ID_AND_PRIORITY를 호출할때마다 1씩 증가시킨값. 
	 * 동일한 트렌젝션으로 묶일경우는 증가시키지 않는다.
	 */
	private int priorityCounter = 64;

	public byte[] getPriorityByteValue() {
		return getPriorityByteValue(false);
	}

	public byte[] getPriorityByteValue(boolean hasMorTransaction) {
		byte[] result = new byte[1];

		if (hasMorTransaction) {
			result[0] = DataUtil.getByteToInt(priorityCounter);
		} else {
			result[0] = DataUtil.getByteToInt(priorityCounter++);
		}

		return result;
	}

	/**
	 * @param nestedFrame
	 */
	public NestedDLMSDecoratorForECG(INestedFrame nestedFrame) {
		super(nestedFrame);
	}

	@SuppressWarnings("unchecked")
	@Override
	public byte[] encode(HdlcObjectType hdlcType, Procedure procedure, HashMap<String, Object> param, String command) {
		logger.debug("## Excute NestedDLMSDecorator Encoding [{}]", hdlcType.name() +" | procedure["+procedure+"] | isHLS["+isProtocalHLS()+"]");
		gdDLMSFrame = new byte[] {};
		String obisCode = null;
		String[] obisCodeArr = null;
		byte[] obisCodeByte = new byte[] {};
		String classId = null;
		String attributeNo = null;
		String value = null;
		JSONArray jsonArr = null;
		Map<String, Object> map = null;
		String dataType = null;
		try {
			switch (hdlcType) {
			/*
			 *   공통 프로시져 
			 */
			case SNRM:
				byte[] snrmFrame = null;
				
				/*
				 * For SORIA Kaifa Meter custom
				 */
				if(command.equals("cmdSORIAGetMeterKey")){
					snrmFrame = DataUtil.readByteString("818014050207EE060207EE070400000001080400000001");
				} else if(command.equals("cmdSORIASetMeterSerial")){
						snrmFrame = DataUtil.readByteString("818014050207EE060207EE070400000001080400000001");						
				}else{
					if(isProtocalHLS()) {
						snrmFrame = new byte[] { (byte) 0x81, // Format identifier
								(byte) 0x80, // Group identifier
								(byte) 0x14, // Length 20
								(byte) 0x05, // maximun length - transmit
								(byte) 0x02, // Length 2
								(byte) 0x00, (byte) 0xf0, // 240 bytes
								(byte) 0x06, // maximum length - receive
								(byte) 0x02, // Length 2
								(byte) 0x00, (byte) 0xf0, // 240 bytes
								(byte) 0x07, // window size - transmit
								(byte) 0x04, // Length 4
								(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // 1
								(byte) 0x08, // window size - receive
								(byte) 0x04, // Length 4
								(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 // 1
						};
					} else {
						snrmFrame = new byte[] { (byte) 0x81, // Format identifier
								(byte) 0x80, // Group identifier
								(byte) 0x14, // Length 20
								(byte) 0x05, // maximun length - transmit
								(byte) 0x02, // Length 2
								(byte) 0x04, (byte) 0x00, // 240 bytes
								(byte) 0x06, // maximum length - receive
								(byte) 0x02, // Length 2
								(byte) 0x04, (byte) 0x00, // 240 bytes
								(byte) 0x07, // window size - transmit
								(byte) 0x04, // Length 4
								(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // 1
								(byte) 0x08, // window size - receive
								(byte) 0x04, // Length 4
								(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07 // 7
						};
					}
					
				}

				gdDLMSFrame = DataUtil.append(super.encode(hdlcType, null, null, command), snrmFrame);

				break;
			case AARQ:
				gdDLMSFrame = DataUtil.append(super.encode(hdlcType, null, null, command), gdDLMSFrame);

				byte[] aarqResult = null;
				
				/*
				 * For TVWS Kaifa Meter custom
				 */
				if(isProtocalHLS()) {
					if(command.equals("cmdSORIAGetMeterKey")){
						aarqResult = DataUtil.readByteString("E6E600601DA109060760857405080101BE10040E01000000065F1F040000181CFFFF");
					}else if(command.equals("cmdSORIASetMeterSerial")){
						aarqResult = DataUtil.readByteString("E6E600601DA109060760857405080101BE10040E01000000065F1F040000181CFFFF");
					}else{
						// AARQ Info
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, AARQ.AARQ_LLC.getValue());
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, AARQ.APPLICATION.getValue());
						aarqResult = new byte[] {};
						aarqResult = DataUtil.append(aarqResult, new byte[1]); // application & application length 2바이트 제외
						aarqResult = DataUtil.append(aarqResult, AARQ.APPLICATION_CONTEXT_NAME.getValue());
						aarqResult = DataUtil.append(aarqResult, AARQ.CALLING_AP_TITLE.getValue());
						aarqResult = DataUtil.append(aarqResult, AARQ.SENDER_ACSE_REQUIREMENTS.getValue());
						aarqResult = DataUtil.append(aarqResult, AARQ.MECHANISM_NAME.getValue());
						aarqResult = DataUtil.append(aarqResult, AARQ.CALLING_AUTHENTICATION_VALUE.getValue());
						aarqResult = DataUtil.append(aarqResult, AARQ.USER_INFORMATION.getValue());
						aarqResult[0] = DataUtil.getByteToInt(aarqResult.length - 1); // application length : -1은 length
						
					}
				} else {
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, AARQ.AARQ_LLC.getValue());
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, AARQ.APPLICATION.getValue());
					
					aarqResult = new byte[] {};
					aarqResult = DataUtil.append(aarqResult, new byte[1]); // application & application length 2바이트 제외
					aarqResult = DataUtil.append(aarqResult, AARQ_LLS.APPLICATION_CONTEXT_NAME.getValue());
					aarqResult = DataUtil.append(aarqResult, AARQ_LLS.SENDER_ACSE_REQUIREMENTS.getValue());
					aarqResult = DataUtil.append(aarqResult, AARQ_LLS.MECHANISM_NAME.getValue());
					aarqResult = DataUtil.append(aarqResult, AARQ_LLS.CALLING_AUTHENTICATION_VALUE.getValue()); 
					aarqResult = DataUtil.append(aarqResult, new byte[]{(byte)0x33, (byte)0x33, (byte)0x33, (byte)0x33, (byte)0x33, (byte)0x33, (byte)0x33, (byte)0x33}); //meter password
					aarqResult = DataUtil.append(aarqResult, AARQ_LLS.USER_INFORMATION.getValue());
					aarqResult[0] = DataUtil.getByteToInt(aarqResult.length - 1);
					
				}
				
				gdDLMSFrame = DataUtil.append(gdDLMSFrame, aarqResult);
				break;
			case ACTION_REQ:
				gdDLMSFrame = DataUtil.append(super.encode(hdlcType, null, null, command), gdDLMSFrame);

				switch (procedure) {
				/*
				 *  공통 프로시저
				 */
				case HDLC_ASSOCIATION_LN:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_LLC.getByteValue());
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST.getByteValue());
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.NORMAL.getByteValue());
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ASSOCIATION_LN.CLASS_ASSOCIATION_LN.getByteValue());
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ASSOCIATION_LN.CURRENT_ASSOCIATION_LN.getByteValue());
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ASSOCIATION_LN.REPLY_TO_HLS_AUTHENTICATION.getByteValue());

					gdDLMSFrame = DataUtil.append(gdDLMSFrame, new byte[] { 0x01, 0x09, 0x11 }); // param, octet-string, length 17
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, HLSSecurity.AUTHENTICATION.getValue());
					byte[] aReqIC = getInvoCounter();
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, aReqIC);
					
					HLSAuthForECG auth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION, getMeterId());
					byte[] tagValue = auth.getTagValue(aReqIC, DlmsPiece.CLIENT_SYSTEM_TITLE.getBytes(), aareAuthenticationValue);
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, tagValue);
					break;
				/*
				 *  Image Transfer 관련 프로시져 
				 */
				case ACTION_IMAGE_TRANSFER_INIT:
					/*
					 *  data ::= structure
						{
							image_identifier: octet-string,
							image_size: double-long-unsigned
						}
					 */
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_LLC.getByteValue());
					byte[] actImageTransferInitPlainText = new byte[] {};
					
					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, ActionRequest.ACTION_REQUEST.getByteValue());
					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, ActionRequest.NORMAL.getByteValue());
					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, ActionRequest.ACTION_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, ImageTransfer.CLASS_ID.getByteValue());
					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, ImageTransfer.OBIS_CODE.getByteValue());
					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, ImageTransferMethods.IMAGE_TRANSFER_INITIATE.getByteValue());
					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, ImageTransfer.OPTION_USE.getByteValue());

					byte[] image_identifier = ((String) param.get("image_identifier")).getBytes(); // F/W파일명
					if(((String) param.get("image_identifier")).contains("WASION")) {
						image_identifier = DataUtil.append(image_identifier, DataUtil.get4ByteToInt(3));
					}
					
					byte[] structureA = new byte[4];
					structureA[0] = 0x02;
					structureA[1] = 0x02;
					structureA[2] = 0x09;
					structureA[3] = DataUtil.getByteToInt(image_identifier.length);

					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, structureA);
					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, image_identifier);

					byte[] structureB = new byte[] { 0x06 };
					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, structureB);
					actImageTransferInitPlainText = DataUtil.append(actImageTransferInitPlainText, DataUtil.get4ByteToInt(Integer.parseInt((String) param.get("image_size"))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG actionImageTransferInitReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getImageTransferInitReqValue = actionImageTransferInitReqAuth.getActionEncryptionGlobalCiphering(getInvoCounter(), actImageTransferInitPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageTransferInitReqValue);
					}else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, actImageTransferInitPlainText);
					}
					break;
				case ACTION_IMAGE_BLOCK_TRANSFER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_LLC.getByteValue());
					byte[] actImageBlockTransferPlainText = new byte[] {};
					
					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, ActionRequest.ACTION_REQUEST.getByteValue());
					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, ActionRequest.NORMAL.getByteValue());
					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, ActionRequest.ACTION_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, ImageTransfer.CLASS_ID.getByteValue());
					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, ImageTransfer.OBIS_CODE.getByteValue());
					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, ImageTransferMethods.IMAGE_BLOCK_TRANSFER.getByteValue());
					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, ImageTransfer.OPTION_USE.getByteValue());

					byte[] imagePartA = new byte[3];
					imagePartA[0] = 0x02;
					imagePartA[1] = 0x02;
					imagePartA[2] = 0x06;
					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, imagePartA);
					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("image_block_number")))));

					byte[] imageBlock = (byte[]) param.get("image_block_value");

					/*
					 * Block Length 구하기
					 */
					byte[] imagePartB = null;
					if (128 <= imageBlock.length) {
						imagePartB = new byte[3];
						imagePartB[0] = 0x09;
						imagePartB[1] = (byte) 0x81;
						imagePartB[2] = DataUtil.getByteToInt(imageBlock.length);
					} else {
						imagePartB = new byte[2];
						imagePartB[0] = 0x09;
						imagePartB[1] = DataUtil.getByteToInt(imageBlock.length);
					}

					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, imagePartB);
					actImageBlockTransferPlainText = DataUtil.append(actImageBlockTransferPlainText, imageBlock);
					
					if(isProtocalHLS()) {
						HLSAuthForECG actionImageBlockTransferReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getImageBlockTransferReqValue = actionImageBlockTransferReqAuth.getActionEncryptionGlobalCiphering(getInvoCounter(), actImageBlockTransferPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageBlockTransferReqValue);
					}else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, actImageBlockTransferPlainText);
					}
					break;
				case ACTION_IMAGE_VERIFY:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_LLC.getByteValue());
					byte[] actImageVerifyPlainText = new byte[] {};
					
					actImageVerifyPlainText = DataUtil.append(actImageVerifyPlainText, ActionRequest.ACTION_REQUEST.getByteValue());
					actImageVerifyPlainText = DataUtil.append(actImageVerifyPlainText, ActionRequest.NORMAL.getByteValue());
					actImageVerifyPlainText = DataUtil.append(actImageVerifyPlainText, ActionRequest.ACTION_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					actImageVerifyPlainText = DataUtil.append(actImageVerifyPlainText, ImageTransfer.CLASS_ID.getByteValue());
					actImageVerifyPlainText = DataUtil.append(actImageVerifyPlainText, ImageTransfer.OBIS_CODE.getByteValue());
					actImageVerifyPlainText = DataUtil.append(actImageVerifyPlainText, ImageTransferMethods.IMAGE_VERIFY.getByteValue());
					actImageVerifyPlainText = DataUtil.append(actImageVerifyPlainText, ImageTransfer.OPTION_USE.getByteValue());

					byte[] verifyPartA = new byte[2];
					verifyPartA[0] = 0x0F;
					verifyPartA[1] = Byte.valueOf(String.valueOf(param.get("image_verify_data")));
					actImageVerifyPlainText = DataUtil.append(actImageVerifyPlainText, verifyPartA);
					
					if(isProtocalHLS()) {
						HLSAuthForECG actionImageVerifyReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getImageVerifyReqValue = actionImageVerifyReqAuth.getActionEncryptionGlobalCiphering(getInvoCounter(), actImageVerifyPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageVerifyReqValue);
					}else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, actImageVerifyPlainText);
					}
					break;
				case ACTION_IMAGE_ACTIVATE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_LLC.getByteValue());
					byte[] actImageActivatePlainText = new byte[] {};
					
					actImageActivatePlainText = DataUtil.append(actImageActivatePlainText, ActionRequest.ACTION_REQUEST.getByteValue());
					actImageActivatePlainText = DataUtil.append(actImageActivatePlainText, ActionRequest.NORMAL.getByteValue());
					actImageActivatePlainText = DataUtil.append(actImageActivatePlainText, ActionRequest.ACTION_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					actImageActivatePlainText = DataUtil.append(actImageActivatePlainText, ImageTransfer.CLASS_ID.getByteValue());
					actImageActivatePlainText = DataUtil.append(actImageActivatePlainText, ImageTransfer.OBIS_CODE.getByteValue());
					actImageActivatePlainText = DataUtil.append(actImageActivatePlainText, ImageTransferMethods.IMAGE_ACTIVATE.getByteValue());
					actImageActivatePlainText = DataUtil.append(actImageActivatePlainText, ImageTransfer.OPTION_USE.getByteValue());

					byte[] activatePartA = new byte[2];
					activatePartA[0] = 0x0F;
					activatePartA[1] = Byte.valueOf(String.valueOf(param.get("image_activate_data")));
					actImageActivatePlainText = DataUtil.append(actImageActivatePlainText, activatePartA);
					
					if(isProtocalHLS()) {
						HLSAuthForECG actionImageActivateReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getImageActivateReqValue = actionImageActivateReqAuth.getActionEncryptionGlobalCiphering(getInvoCounter(), actImageActivatePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageActivateReqValue);
					}else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, actImageActivatePlainText);
					}
					break;
				/*
				 *  Meter Alarm Reset용 프로시져 
				 */
				case ACTION_METER_ALARM_RESET:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_LLC.getByteValue());
					byte[] actAlarmResetPlainText = new byte[] {};
					
					actAlarmResetPlainText = DataUtil.append(actAlarmResetPlainText, ActionRequest.ACTION_REQUEST.getByteValue());
					actAlarmResetPlainText = DataUtil.append(actAlarmResetPlainText, ActionRequest.NORMAL.getByteValue());
					actAlarmResetPlainText = DataUtil.append(actAlarmResetPlainText, ActionRequest.ACTION_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					actAlarmResetPlainText = DataUtil.append(actAlarmResetPlainText, MeterParamSetMethods.CLASS_ID.getByteValue());
					actAlarmResetPlainText = DataUtil.append(actAlarmResetPlainText, MeterParamSetMethods.OBIS_CODE.getByteValue());
					actAlarmResetPlainText = DataUtil.append(actAlarmResetPlainText, MeterParamSetMethods.METER_ALARM_RESET.getByteValue());
					actAlarmResetPlainText = DataUtil.append(actAlarmResetPlainText, MeterParamSetMethods.OPTION_USE.getByteValue());

					actAlarmResetPlainText = DataUtil.append(actAlarmResetPlainText, new byte[] { 0x12 }); // UINT16(0x12, 2), long-unsigned 2byte
					actAlarmResetPlainText = DataUtil.append(actAlarmResetPlainText, MeterParamSetMethods.RESET_ALARM.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG actionAlarmResetReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getAlarmResetReqValue = actionAlarmResetReqAuth.getActionEncryptionGlobalCiphering(getInvoCounter(), actAlarmResetPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getAlarmResetReqValue);
					}else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, actAlarmResetPlainText);
					}
					break;
					
				case ACTION_DISCONNECT_CONTROL:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_LLC.getByteValue());
					byte[] actDisconnectControlPlainText = new byte[] {};
					
					actDisconnectControlPlainText = DataUtil.append(actDisconnectControlPlainText, ActionRequest.ACTION_REQUEST.getByteValue());
					actDisconnectControlPlainText = DataUtil.append(actDisconnectControlPlainText, ActionRequest.NORMAL.getByteValue());
					actDisconnectControlPlainText = DataUtil.append(actDisconnectControlPlainText, ActionRequest.ACTION_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					actDisconnectControlPlainText = DataUtil.append(actDisconnectControlPlainText, MeterRelayMethods.CLASS_ID.getByteValue());
					actDisconnectControlPlainText = DataUtil.append(actDisconnectControlPlainText, MeterRelayMethods.OBIS_CODE.getByteValue());
					String boolValue = String.valueOf(param.get("value"));
					if ( "true".equals(boolValue)){
						actDisconnectControlPlainText = DataUtil.append(actDisconnectControlPlainText, MeterRelayMethods.REMOTE_RECONNECT.getByteValue());
					}
					else if ("false".equals(boolValue)){
						actDisconnectControlPlainText = DataUtil.append(actDisconnectControlPlainText, MeterRelayMethods.REMOTE_DISCONNECT.getByteValue());
					}
					actDisconnectControlPlainText = DataUtil.append(actDisconnectControlPlainText, MeterRelayMethods.OPTION_USE.getByteValue());					
					byte[] disconnectCtrl = new byte[2];
					disconnectCtrl[0] = (byte)0x0F;
					disconnectCtrl[1] = (byte)0x00;
					actDisconnectControlPlainText = DataUtil.append(actDisconnectControlPlainText, disconnectCtrl);
					
					if(isProtocalHLS()) {
						HLSAuthForECG actionDisconnectControlReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getImageBlockTransferReqValue = actionDisconnectControlReqAuth.getActionEncryptionGlobalCiphering(getInvoCounter(), actDisconnectControlPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageBlockTransferReqValue);
					}else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, actDisconnectControlPlainText);
					}
					break;
					
				case ACTION_SET_ENCRYPTION_KEY:
				case ACTION_TRANSFER_KEY:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_LLC.getByteValue());
					byte[] actTransferKeyPlainText = new byte[] {};
					
					actTransferKeyPlainText = DataUtil.append(actTransferKeyPlainText, ActionRequest.ACTION_REQUEST.getByteValue());
					actTransferKeyPlainText = DataUtil.append(actTransferKeyPlainText, ActionRequest.NORMAL.getByteValue());
					actTransferKeyPlainText = DataUtil.append(actTransferKeyPlainText, ActionRequest.ACTION_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					actTransferKeyPlainText = DataUtil.append(actTransferKeyPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					actTransferKeyPlainText = DataUtil.append(actTransferKeyPlainText, obisCodeByte);
					actTransferKeyPlainText = DataUtil.append(actTransferKeyPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					actTransferKeyPlainText = DataUtil.append(actTransferKeyPlainText, Hex.encode("01"));
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						jsonArr = JSONArray.fromObject(value);
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];
//					String strVal = String.valueOf(map.get("value"));
//					byte[] val = strVal.getBytes();
//					logger.debug("KEY_VALUE=" +strVal );
					String strHexVal = String.valueOf(map.get("value"));
					byte[] val = HexUtil.convertHexStringToBytes(strHexVal);

					logger.debug("KEY_HEX=" + HexUtil.conventBytesToHexString(val));
					byte[] data = new byte[2];

					data[0] = DLMSCommonDataType.OctetString.getValue();
					data[1] = DataUtil.getByteToInt(val.length);
					actTransferKeyPlainText = DataUtil.append(actTransferKeyPlainText, data);
					actTransferKeyPlainText = DataUtil.append(actTransferKeyPlainText, val);

					if(isProtocalHLS()) {
						HLSAuthForECG actionTransferKeyReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getTransferKeyReqValue = actionTransferKeyReqAuth.getActionEncryptionGlobalCiphering(getInvoCounter(), actTransferKeyPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getTransferKeyReqValue);
					}else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, actTransferKeyPlainText);
					}
					break;
				case ACTION_SET_STS_TOKEN:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, ActionRequest.ACTION_REQUEST_LLC.getByteValue());
					byte[] actSetTokenPlainText = new byte[] {};
					
					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, ActionRequest.ACTION_REQUEST.getByteValue());
					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, ActionRequest.NORMAL.getByteValue());
					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, ActionRequest.ACTION_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, obisCodeByte);
					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, new byte[] { 0x01, 0x09 });
					
					byte[] preFix = new byte[] {0x00, 0x00, 0x00, 0x01};
					String hex = new BigInteger(param.get("value").toString()).toString(16);
					if((hex.length() % 2) != 0) {
						hex = "0" + hex;			
					}
					
					byte[] token = Hex.encode(hex);
					
					byte hexLen = DataUtil.getByteToInt(preFix.length + token.length);
					
					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, hexLen);
					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, preFix);
					actSetTokenPlainText = DataUtil.append(actSetTokenPlainText, token);
					
					if(isProtocalHLS()) {
						HLSAuthForECG actionSetTokenReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getSetTokenReqValue = actionSetTokenReqAuth.getActionEncryptionGlobalCiphering(getInvoCounter(), actSetTokenPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getSetTokenReqValue);
					}else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, actSetTokenPlainText);
					}
					break;
				
				default:
					break;
				}

				break;
			case GET_REQ:
				gdDLMSFrame = DataUtil.append(super.encode(hdlcType, null, null, command), gdDLMSFrame);

				switch (procedure) {
				/*
				 *   Image Transfer 관련 프로시져 
				 */
				case GET_IMAGE_TRANSFER_ENABLE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getImageTransferEnablePlainText = new byte[] {};
					
					getImageTransferEnablePlainText = DataUtil.append(getImageTransferEnablePlainText, GetRequest.GET_REQUEST.getByteValue());
					getImageTransferEnablePlainText = DataUtil.append(getImageTransferEnablePlainText, GetRequest.NORMAL.getByteValue());
					getImageTransferEnablePlainText = DataUtil.append(getImageTransferEnablePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					getImageTransferEnablePlainText = DataUtil.append(getImageTransferEnablePlainText, ImageTransfer.CLASS_ID.getByteValue());
					getImageTransferEnablePlainText = DataUtil.append(getImageTransferEnablePlainText, ImageTransfer.OBIS_CODE.getByteValue());
					getImageTransferEnablePlainText = DataUtil.append(getImageTransferEnablePlainText, ImageTransferAttributes.IMAGE_TRANSFER_ENABLED.getByteValue());
					getImageTransferEnablePlainText = DataUtil.append(getImageTransferEnablePlainText, ImageTransfer.OPTION_NOT_USE.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG getImageTransferEnableReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getFirmwareVersionReqValue = getImageTransferEnableReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getImageTransferEnablePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getFirmwareVersionReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageTransferEnablePlainText);
					}
					break;
				case GET_IMAGE_BLOCK_SIZE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getImageBlockSizePlainText = new byte[] {};
					
					getImageBlockSizePlainText = DataUtil.append(getImageBlockSizePlainText, GetRequest.GET_REQUEST.getByteValue());
					getImageBlockSizePlainText = DataUtil.append(getImageBlockSizePlainText, GetRequest.NORMAL.getByteValue());
					getImageBlockSizePlainText = DataUtil.append(getImageBlockSizePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					getImageBlockSizePlainText = DataUtil.append(getImageBlockSizePlainText, ImageTransfer.CLASS_ID.getByteValue());
					getImageBlockSizePlainText = DataUtil.append(getImageBlockSizePlainText, ImageTransfer.OBIS_CODE.getByteValue());
					getImageBlockSizePlainText = DataUtil.append(getImageBlockSizePlainText, ImageTransferAttributes.IMAGE_BLOCK_SIZE.getByteValue());
					getImageBlockSizePlainText = DataUtil.append(getImageBlockSizePlainText, ImageTransfer.OPTION_NOT_USE.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG getImageBlockSizeReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getImageBlockSizeReqValue = getImageBlockSizeReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getImageBlockSizePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageBlockSizeReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageBlockSizePlainText);
					}
					break;
				case GET_IMAGE_TRANSFER_STATUS:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getImageTransferStatusPlainText = new byte[] {};
					
					getImageTransferStatusPlainText = DataUtil.append(getImageTransferStatusPlainText, GetRequest.GET_REQUEST.getByteValue());
					getImageTransferStatusPlainText = DataUtil.append(getImageTransferStatusPlainText, GetRequest.NORMAL.getByteValue());
					getImageTransferStatusPlainText = DataUtil.append(getImageTransferStatusPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					getImageTransferStatusPlainText = DataUtil.append(getImageTransferStatusPlainText, ImageTransfer.CLASS_ID.getByteValue());
					getImageTransferStatusPlainText = DataUtil.append(getImageTransferStatusPlainText, ImageTransfer.OBIS_CODE.getByteValue());
					getImageTransferStatusPlainText = DataUtil.append(getImageTransferStatusPlainText, ImageTransferAttributes.IMAGE_TRANSFER_STATUS.getByteValue());
					getImageTransferStatusPlainText = DataUtil.append(getImageTransferStatusPlainText, ImageTransfer.OPTION_NOT_USE.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG getImageTransferStatusReqValueReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getImageTransferStatusReqValue = getImageTransferStatusReqValueReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getImageTransferStatusPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageTransferStatusReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageTransferStatusPlainText);
					}
					break;
				case GET_IMAGE_FIRST_NOT_TRANSFERRED_BLOCK_NUMBER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getImageTransferBlockNumberPlainText = new byte[] {};
					
					getImageTransferBlockNumberPlainText = DataUtil.append(getImageTransferBlockNumberPlainText, GetRequest.GET_REQUEST.getByteValue());
					getImageTransferBlockNumberPlainText = DataUtil.append(getImageTransferBlockNumberPlainText, GetRequest.NORMAL.getByteValue());
					getImageTransferBlockNumberPlainText = DataUtil.append(getImageTransferBlockNumberPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					getImageTransferBlockNumberPlainText = DataUtil.append(getImageTransferBlockNumberPlainText, ImageTransfer.CLASS_ID.getByteValue());
					getImageTransferBlockNumberPlainText = DataUtil.append(getImageTransferBlockNumberPlainText, ImageTransfer.OBIS_CODE.getByteValue());
					getImageTransferBlockNumberPlainText = DataUtil.append(getImageTransferBlockNumberPlainText, ImageTransferAttributes.IMAGE_FIRST_NOT_TRANSFERRED_BLOCK_NUMBER.getByteValue());
					getImageTransferBlockNumberPlainText = DataUtil.append(getImageTransferBlockNumberPlainText, ImageTransfer.OPTION_NOT_USE.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG getImageTransferBlockNumberReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getImageTransferBlockNumberReqValue = getImageTransferBlockNumberReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getImageTransferBlockNumberPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageTransferBlockNumberReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageTransferBlockNumberPlainText);
					}
					break;
				case GET_IMAGE_TO_ACTIVATE_INFO:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getImageToActivateInfoPlainText = new byte[] {};
					
					getImageToActivateInfoPlainText = DataUtil.append(getImageToActivateInfoPlainText, GetRequest.GET_REQUEST.getByteValue());
					getImageToActivateInfoPlainText = DataUtil.append(getImageToActivateInfoPlainText, GetRequest.NORMAL.getByteValue());
					getImageToActivateInfoPlainText = DataUtil.append(getImageToActivateInfoPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					getImageToActivateInfoPlainText = DataUtil.append(getImageToActivateInfoPlainText, ImageTransfer.CLASS_ID.getByteValue());
					getImageToActivateInfoPlainText = DataUtil.append(getImageToActivateInfoPlainText, ImageTransfer.OBIS_CODE.getByteValue());
					getImageToActivateInfoPlainText = DataUtil.append(getImageToActivateInfoPlainText, ImageTransferAttributes.IMAGE_TO_ACTIVATE_INFO.getByteValue());
					getImageToActivateInfoPlainText = DataUtil.append(getImageToActivateInfoPlainText, ImageTransfer.OPTION_NOT_USE.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG getImageToActivateInfoReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getImageToActivateInfoReqValue = getImageToActivateInfoReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getImageToActivateInfoPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageToActivateInfoReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getImageToActivateInfoPlainText);
					}
					break;

				/*
				 *   Meter F/W 버전 확인용 프로시져 
				 */
				case GET_FIRMWARE_VERSION:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					
					byte[] getFirmwareVersionPlainText = new byte[] {};
					
					getFirmwareVersionPlainText = DataUtil.append(getFirmwareVersionPlainText, GetRequest.GET_REQUEST.getByteValue());
					getFirmwareVersionPlainText = DataUtil.append(getFirmwareVersionPlainText, GetRequest.NORMAL.getByteValue());
					getFirmwareVersionPlainText = DataUtil.append(getFirmwareVersionPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					getFirmwareVersionPlainText = DataUtil.append(getFirmwareVersionPlainText, MeterFWInfoAttributes.CLASS_ID.getByteValue());
					getFirmwareVersionPlainText = DataUtil.append(getFirmwareVersionPlainText, MeterFWInfoAttributes.OBIS_CODE.getByteValue());
					getFirmwareVersionPlainText = DataUtil.append(getFirmwareVersionPlainText, MeterFWInfoAttributes.FW_VERSION.getByteValue());
					getFirmwareVersionPlainText = DataUtil.append(getFirmwareVersionPlainText, MeterFWInfoAttributes.OPTION_NOT_USE.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG getFirmwareVersionReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getFirmwareVersionReqValue = getFirmwareVersionReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getFirmwareVersionPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getFirmwareVersionReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getFirmwareVersionPlainText);
					}
					break;

				/*
				 *   Meter BillingCycle information 확인 프로시져 
				 */
				case GET_BILLING_CYCLE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getBillingCyclePlainText = new byte[] {};
					
					getBillingCyclePlainText = DataUtil.append(getBillingCyclePlainText, GetRequest.GET_REQUEST.getByteValue());
					getBillingCyclePlainText = DataUtil.append(getBillingCyclePlainText, GetRequest.NORMAL.getByteValue());
					getBillingCyclePlainText = DataUtil.append(getBillingCyclePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					getBillingCyclePlainText = DataUtil.append(getBillingCyclePlainText, MeterBillingCycleAttributes.CLASS_ID.getByteValue());
					getBillingCyclePlainText = DataUtil.append(getBillingCyclePlainText, MeterBillingCycleAttributes.OBIS_CODE.getByteValue());
					getBillingCyclePlainText = DataUtil.append(getBillingCyclePlainText, MeterBillingCycleAttributes.BILLING_CYCLE_EXECUTION_TIME.getByteValue());
					getBillingCyclePlainText = DataUtil.append(getBillingCyclePlainText, MeterBillingCycleAttributes.OPTION_NOT_USE.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG getBillingCycleReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getBillingCycleReqValue = getBillingCycleReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getBillingCyclePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getBillingCycleReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getBillingCyclePlainText);
					}
					break;
				case GET_STS_SWITCH_TIME:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getSwitchTimePlainText = new byte[] {};
					
					getSwitchTimePlainText = DataUtil.append(getSwitchTimePlainText, GetRequest.GET_REQUEST.getByteValue());
					getSwitchTimePlainText = DataUtil.append(getSwitchTimePlainText, GetRequest.NORMAL.getByteValue());
					getSwitchTimePlainText = DataUtil.append(getSwitchTimePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					logger.debug("procedure[GET_STS_SWITCH_TIME], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"]");
										
					getSwitchTimePlainText = DataUtil.append(getSwitchTimePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getSwitchTimePlainText = DataUtil.append(getSwitchTimePlainText, obisCodeByte);
					getSwitchTimePlainText = DataUtil.append(getSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getSwitchTimePlainText = DataUtil.append(getSwitchTimePlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getSwitchTimeReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getSwitchTimeReqValue = getSwitchTimeReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getSwitchTimePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getSwitchTimeReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getSwitchTimePlainText);
					}
					break;
				case GET_METER_TIME:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getMeterTimePlainText = new byte[] {};
					
					getMeterTimePlainText = DataUtil.append(getMeterTimePlainText, GetRequest.GET_REQUEST.getByteValue());
					getMeterTimePlainText = DataUtil.append(getMeterTimePlainText, GetRequest.NORMAL.getByteValue());
					getMeterTimePlainText = DataUtil.append(getMeterTimePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					logger.debug("procedure[GET_METER_TIME], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"]");
					
					getMeterTimePlainText = DataUtil.append(getMeterTimePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getMeterTimePlainText = DataUtil.append(getMeterTimePlainText, obisCodeByte);
					getMeterTimePlainText = DataUtil.append(getMeterTimePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getMeterTimePlainText = DataUtil.append(getMeterTimePlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getMeterTimeReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getMeterTimeReqValue = getMeterTimeReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getMeterTimePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getMeterTimeReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getMeterTimePlainText);
					}
					break;
				case GET_STS_REGISTER_VALUE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getSTSRegisterValuePlainText = new byte[] {};
					
					getSTSRegisterValuePlainText = DataUtil.append(getSTSRegisterValuePlainText, GetRequest.GET_REQUEST.getByteValue());
					getSTSRegisterValuePlainText = DataUtil.append(getSTSRegisterValuePlainText, GetRequest.NORMAL.getByteValue());
					getSTSRegisterValuePlainText = DataUtil.append(getSTSRegisterValuePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					logger.debug("procedure[GET_STS_REGISTER_VALUE], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"]");
					
					getSTSRegisterValuePlainText = DataUtil.append(getSTSRegisterValuePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getSTSRegisterValuePlainText = DataUtil.append(getSTSRegisterValuePlainText, obisCodeByte);
					getSTSRegisterValuePlainText = DataUtil.append(getSTSRegisterValuePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getSTSRegisterValuePlainText = DataUtil.append(getSTSRegisterValuePlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getSTSRegisterValueReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getSTSRegisterValueReqValue = getSTSRegisterValueReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getSTSRegisterValuePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getSTSRegisterValueReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getSTSRegisterValuePlainText);
					}
					
					break;					
				case GET_REGISTER_VALUE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getRegisterValuePlainText = new byte[] {};
					
					getRegisterValuePlainText = DataUtil.append(getRegisterValuePlainText, GetRequest.GET_REQUEST.getByteValue());
					getRegisterValuePlainText = DataUtil.append(getRegisterValuePlainText, GetRequest.NORMAL.getByteValue());
					getRegisterValuePlainText = DataUtil.append(getRegisterValuePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					logger.debug("procedure[GET_REGISTER_VALUE], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"]");
					
					getRegisterValuePlainText = DataUtil.append(getRegisterValuePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getRegisterValuePlainText = DataUtil.append(getRegisterValuePlainText, obisCodeByte);
					getRegisterValuePlainText = DataUtil.append(getRegisterValuePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getRegisterValuePlainText = DataUtil.append(getRegisterValuePlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getRegisterValueReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getRegisterValueReqValue = getRegisterValueReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getRegisterValuePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getRegisterValueReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getRegisterValuePlainText);
					}
					break;

				case GET_REGISTER_UNIT:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getRegisterUnitPlainText = new byte[] {};
					
					getRegisterUnitPlainText = DataUtil.append(getRegisterUnitPlainText, GetRequest.GET_REQUEST.getByteValue());
					getRegisterUnitPlainText = DataUtil.append(getRegisterUnitPlainText, GetRequest.NORMAL.getByteValue());
					getRegisterUnitPlainText = DataUtil.append(getRegisterUnitPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					logger.debug("procedure[GET_REGISTER_UNIT], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"]");
					
					getRegisterUnitPlainText = DataUtil.append(getRegisterUnitPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getRegisterUnitPlainText = DataUtil.append(getRegisterUnitPlainText, obisCodeByte);
					getRegisterUnitPlainText = DataUtil.append(getRegisterUnitPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getRegisterUnitPlainText = DataUtil.append(getRegisterUnitPlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getRegisterUnitReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getRegisterUnitReqValue = getRegisterUnitReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getRegisterUnitPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getRegisterUnitReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getRegisterUnitPlainText);
					}
					break;

				case GET_PROFILE_OBJECT:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					
					byte[] getProfileObjectPlainText = new byte[] {};
					
					getProfileObjectPlainText = DataUtil.append(getProfileObjectPlainText, GetRequest.GET_REQUEST.getByteValue());
					getProfileObjectPlainText = DataUtil.append(getProfileObjectPlainText, GetRequest.NORMAL.getByteValue());
					getProfileObjectPlainText = DataUtil.append(getProfileObjectPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = "3";

					getProfileObjectPlainText = DataUtil.append(getProfileObjectPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getProfileObjectPlainText = DataUtil.append(getProfileObjectPlainText, obisCodeByte);
					getProfileObjectPlainText = DataUtil.append(getProfileObjectPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getProfileObjectPlainText = DataUtil.append(getProfileObjectPlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getProfileObjectReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getProfileObjectReqValue = getProfileObjectReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getProfileObjectPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getProfileObjectReqValue);	
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getProfileObjectPlainText);
					}
					break;
				case GET_CREDIT_CHARGE_EVENT_LOG:
					if (param.get("isBlock") == null || ((Boolean) param.get("isBlock")) == false) {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
						byte[] getChargeBufferPlainText = new byte[] {};
						
						value = (String) param.get("value");
						
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, GetRequest.GET_REQUEST.getByteValue());
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, GetRequest.NORMAL.getByteValue());
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
						
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, TokenCreditEventAttributes.CLASS_ID.getByteValue());
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, TokenCreditEventAttributes.OBIS_CODE.getByteValue());
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, TokenCreditEventAttributes.Attribute.getByteValue());
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, TokenCreditEventAttributes.OPTION_USE.getByteValue());
						
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, new byte[] { (byte) 0x02 }); //offset
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, new byte[] { (byte) DataType.STRUCTURE.getValue() });
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, new byte[] { (byte) 4 });
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, new byte[] { (byte) DataType.UINT32.getValue() });
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 }); //start offset
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, new byte[] { (byte) DataType.UINT32.getValue() });
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, DataUtil.get4ByteToInt(Integer.parseInt(value))); //end offset
						
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, new byte[] {(byte) 0x12, (byte)0x00, (byte)0x01 });
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, new byte[] {(byte) 0x12, (byte)0x00, (byte)0x00 });
						
						if(isProtocalHLS()) {
							HLSAuthForECG getChargeBufferReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
							byte[] getChargebufferReqValue = getChargeBufferReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getChargeBufferPlainText);
							gdDLMSFrame = DataUtil.append(gdDLMSFrame, getChargebufferReqValue);
						} else {
							gdDLMSFrame = DataUtil.append(gdDLMSFrame, getChargeBufferPlainText);
						}
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
						byte[] getChargeBufferPlainText = new byte[] {};
						
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, GetRequest.GET_REQUEST.getByteValue());
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, GetRequest.NEXT.getByteValue());
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
						int blockNumber = (int) param.get("blockNumber");
						getChargeBufferPlainText = DataUtil.append(getChargeBufferPlainText, DataUtil.get4ByteToInt(blockNumber));
						
						if(isProtocalHLS()) {
							HLSAuthForECG getChargeBufferReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
							byte[] getChargebufferReqValue = getChargeBufferReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getChargeBufferPlainText);
							gdDLMSFrame = DataUtil.append(gdDLMSFrame, getChargebufferReqValue);
						} else {
							gdDLMSFrame = DataUtil.append(gdDLMSFrame, getChargeBufferPlainText);
						}
					}
					break;
				case GET_PROFILE_BUFFER:
					if (param.get("isBlock") == null || ((Boolean) param.get("isBlock")) == false) {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
						
						byte[] getProfileBufferPlainText = new byte[] {};
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, GetRequest.GET_REQUEST.getByteValue());
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, GetRequest.NORMAL.getByteValue());
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
						
						obisCode = (String) param.get("obisCode");
						obisCodeArr = obisCode.split("[.]");
						obisCodeByte = new byte[6];
						for (int i = 0; i < obisCodeArr.length; i++) {
							obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
						}

						classId = (String) param.get("classId");
						attributeNo = (String) param.get("attributeNo");
						
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, obisCodeByte);
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, Hex.encode("01"));
						
						//value = (String) param.get("value");
						value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
						logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
						jsonArr = null;
						if (value == null || value.isEmpty()) {
							jsonArr = new JSONArray();
						} else {
							jsonArr = JSONArray.fromObject(value);
						}

						map = (Map<String, Object>) jsonArr.toArray()[0];
						int option = Integer.parseInt(String.valueOf(map.get("option")));

						String clockObis = String.valueOf(map.get("clockObis"));
						String[] clockObisArr = clockObis.split("[.]");
						byte[] clockObisByte = new byte[6];
						for (int i = 0; i < clockObisArr.length; i++) {
							clockObisByte[i] = (byte) Integer.parseInt(clockObisArr[i]);
						}
						
						logger.debug("procedure[GET_PROFILE_BUFFER], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"] option["+option+"]");
						
						//현재 01만 개발(range_descriptor)
						if (option == 1) { //range_descriptor
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) option }); //range_descriptor

							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) DataType.STRUCTURE.getValue() });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 4 });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) DataType.STRUCTURE.getValue() });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 4 });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) DataType.UINT16.getValue() });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, DataUtil.get2ByteToInt(String.valueOf(ObjectType.CLOCK.getValue())));
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) DataType.OCTET_STRING.getValue() });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 6 });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, clockObisByte);
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) DataType.INT8.getValue() });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 2 });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) DataType.UINT16.getValue() });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, DataUtil.get2ByteToInt(0));
							 
							//from
							//노르웨이
							//getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) DataType.OCTET_STRING.getValue() });							
							//getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 12 });
							
							//이라크, 가나(tvws)
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[]{(byte)DataType.DATETIME.getValue()});
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(map.get("fYear")))));
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("fMonth"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("fDayOfMonth"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("fDayOfWeek"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("fHh"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("fMm"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("fSs"))) });
							//soria, 이라크
							//getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 0xff });
							
							//가나(tvws)
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 0x00 });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 0x80 });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, DataUtil.get2ByteToInt(0));

							//to
							//getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) DataType.OCTET_STRING.getValue() });
							//getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 12 });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[]{(byte)DataType.DATETIME.getValue()});
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(map.get("tYear")))));
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("tMonth"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("tDayOfMonth"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("tDayOfWeek"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("tHh"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("tMm"))) });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("tSs"))) });
							//soria, 이라크
							//getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 0xff });
							
							//가나(tvws)
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 0x00 });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 0x80 });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, DataUtil.get2ByteToInt(0));

							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) DataType.ARRAY.getValue() });
							getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, new byte[] { (byte) 0 });
							
							if(isProtocalHLS()) {							
								HLSAuthForECG getProfileBufferReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
								byte[] getProfilebufferReqValue = getProfileBufferReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getProfileBufferPlainText);
								gdDLMSFrame = DataUtil.append(gdDLMSFrame, getProfilebufferReqValue);
							} else {
								gdDLMSFrame = DataUtil.append(gdDLMSFrame, getProfileBufferPlainText);
							}
						}
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
						byte[] getProfileBufferPlainText = new byte[] {};
						
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, GetRequest.GET_REQUEST.getByteValue());
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, GetRequest.NEXT.getByteValue());
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

						int blockNumber = (int) param.get("blockNumber");
						getProfileBufferPlainText = DataUtil.append(getProfileBufferPlainText, DataUtil.get4ByteToInt(blockNumber));
						
						if(isProtocalHLS()) {
							HLSAuthForECG getProfileBufferReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
							byte[] getProfilebufferReqValue = getProfileBufferReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getProfileBufferPlainText);
							gdDLMSFrame = DataUtil.append(gdDLMSFrame, getProfilebufferReqValue);
						} else {
							gdDLMSFrame = DataUtil.append(gdDLMSFrame, getProfileBufferPlainText);
						}
						
					}
					break;
				case GET_PROFILE_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getProfileBufferPeriodPlainText = new byte[] {};
					
					getProfileBufferPeriodPlainText = DataUtil.append(getProfileBufferPeriodPlainText, GetRequest.GET_REQUEST.getByteValue());
					getProfileBufferPeriodPlainText = DataUtil.append(getProfileBufferPeriodPlainText, GetRequest.NORMAL.getByteValue());
					getProfileBufferPeriodPlainText = DataUtil.append(getProfileBufferPeriodPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					getProfileBufferPeriodPlainText = DataUtil.append(getProfileBufferPeriodPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getProfileBufferPeriodPlainText = DataUtil.append(getProfileBufferPeriodPlainText, obisCodeByte);
					getProfileBufferPeriodPlainText = DataUtil.append(getProfileBufferPeriodPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getProfileBufferPeriodPlainText = DataUtil.append(getProfileBufferPeriodPlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getProfileBufferPeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getProfileBufferPeriodPlainReqValue = getProfileBufferPeriodReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getProfileBufferPeriodPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getProfileBufferPeriodPlainReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getProfileBufferPeriodPlainText);
					}
					break;
				case GET_THRESHOLD_NORMAL:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getThresHoldNormalPlainText = new byte[] {};
					
					getThresHoldNormalPlainText = DataUtil.append(getThresHoldNormalPlainText, GetRequest.GET_REQUEST.getByteValue());
					getThresHoldNormalPlainText = DataUtil.append(getThresHoldNormalPlainText, GetRequest.NORMAL.getByteValue());
					getThresHoldNormalPlainText = DataUtil.append(getThresHoldNormalPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					getThresHoldNormalPlainText = DataUtil.append(getThresHoldNormalPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getThresHoldNormalPlainText = DataUtil.append(getThresHoldNormalPlainText, obisCodeByte);
					getThresHoldNormalPlainText = DataUtil.append(getThresHoldNormalPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getThresHoldNormalPlainText = DataUtil.append(getThresHoldNormalPlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getThresHoldNormalReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getThresHoldNormalReqValue = getThresHoldNormalReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getThresHoldNormalPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getThresHoldNormalReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getThresHoldNormalPlainText);
					}
					break;
				case GET_MINOVER_THRESHOLD_DURATION:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getThresHoldDurationPlainText = new byte[] {};
					
					getThresHoldDurationPlainText = DataUtil.append(getThresHoldDurationPlainText, GetRequest.GET_REQUEST.getByteValue());
					getThresHoldDurationPlainText = DataUtil.append(getThresHoldDurationPlainText, GetRequest.NORMAL.getByteValue());
					getThresHoldDurationPlainText = DataUtil.append(getThresHoldDurationPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					getThresHoldDurationPlainText = DataUtil.append(getThresHoldDurationPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getThresHoldDurationPlainText = DataUtil.append(getThresHoldDurationPlainText, obisCodeByte);
					getThresHoldDurationPlainText = DataUtil.append(getThresHoldDurationPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getThresHoldDurationPlainText = DataUtil.append(getThresHoldDurationPlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getThresHoldDurationReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getThresHoldDurationReqValue = getThresHoldDurationReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getThresHoldDurationPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getThresHoldDurationReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getThresHoldDurationPlainText);
					}
					break;
				case GET_DISCONNECT_CONTROL:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getDisconnectControlPlainText = new byte[] {};
					
					getDisconnectControlPlainText = DataUtil.append(getDisconnectControlPlainText, GetRequest.GET_REQUEST.getByteValue());
					getDisconnectControlPlainText = DataUtil.append(getDisconnectControlPlainText, GetRequest.NORMAL.getByteValue());
					getDisconnectControlPlainText = DataUtil.append(getDisconnectControlPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					getDisconnectControlPlainText = DataUtil.append(getDisconnectControlPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getDisconnectControlPlainText = DataUtil.append(getDisconnectControlPlainText, obisCodeByte);
					getDisconnectControlPlainText = DataUtil.append(getDisconnectControlPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getDisconnectControlPlainText = DataUtil.append(getDisconnectControlPlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getDisconnectControlReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getDisconnectControlReqValue = getDisconnectControlReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getDisconnectControlPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getDisconnectControlReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getDisconnectControlPlainText);
					}
					break;
				case GET_STS_FRIENDLY_DAYS_TABLE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getDaysTablePlainText = new byte[] {};
					
					getDaysTablePlainText = DataUtil.append(getDaysTablePlainText, GetRequest.GET_REQUEST.getByteValue());
					getDaysTablePlainText = DataUtil.append(getDaysTablePlainText, GetRequest.NORMAL.getByteValue());
					getDaysTablePlainText = DataUtil.append(getDaysTablePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					logger.debug("procedure[GET_VALUE], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"]");
										
					getDaysTablePlainText = DataUtil.append(getDaysTablePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getDaysTablePlainText = DataUtil.append(getDaysTablePlainText, obisCodeByte);
					getDaysTablePlainText = DataUtil.append(getDaysTablePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getDaysTablePlainText = DataUtil.append(getDaysTablePlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getDaysTableReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getDaysTableReqValue = getDaysTableReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getDaysTablePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getDaysTableReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getDaysTablePlainText);
					}
					
					break;
				case GET_STS_FRIENDLY_TIME:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getFrindlyTimePlainText = new byte[] {};
					
					getFrindlyTimePlainText = DataUtil.append(getFrindlyTimePlainText, GetRequest.GET_REQUEST.getByteValue());
					getFrindlyTimePlainText = DataUtil.append(getFrindlyTimePlainText, GetRequest.NORMAL.getByteValue());
					getFrindlyTimePlainText = DataUtil.append(getFrindlyTimePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					getFrindlyTimePlainText = DataUtil.append(getFrindlyTimePlainText, FriendlyTimeAttributes.CLASS_ID.getByteValue());
					getFrindlyTimePlainText = DataUtil.append(getFrindlyTimePlainText, FriendlyTimeAttributes.OBIS_CODE.getByteValue());
					getFrindlyTimePlainText = DataUtil.append(getFrindlyTimePlainText, FriendlyTimeAttributes.FRIENDLY_TIME.getByteValue());
					getFrindlyTimePlainText = DataUtil.append(getFrindlyTimePlainText, FriendlyTimeAttributes.OPTION_NOT_USE.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG getFrindlyTimeReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getFrindlyTimelReqValue = getFrindlyTimeReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getFrindlyTimePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getFrindlyTimelReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getFrindlyTimePlainText);
					}
					break;
				case GET_STS_FRIENDLY_WEEK:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getFrindlyWeekPlainText = new byte[] {};
					
					getFrindlyWeekPlainText = DataUtil.append(getFrindlyWeekPlainText, GetRequest.GET_REQUEST.getByteValue());
					getFrindlyWeekPlainText = DataUtil.append(getFrindlyWeekPlainText, GetRequest.NORMAL.getByteValue());
					getFrindlyWeekPlainText = DataUtil.append(getFrindlyWeekPlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					getFrindlyWeekPlainText = DataUtil.append(getFrindlyWeekPlainText, FriendlyWeekAttributes.CLASS_ID.getByteValue());
					getFrindlyWeekPlainText = DataUtil.append(getFrindlyWeekPlainText, FriendlyWeekAttributes.OBIS_CODE.getByteValue());
					getFrindlyWeekPlainText = DataUtil.append(getFrindlyWeekPlainText, FriendlyWeekAttributes.FRIENDLY_TIME.getByteValue());
					getFrindlyWeekPlainText = DataUtil.append(getFrindlyWeekPlainText, FriendlyWeekAttributes.OPTION_NOT_USE.getByteValue());
					
					if(isProtocalHLS()) {
						HLSAuthForECG getFrindlyWeekReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getFrindlyWeeklReqValue = getFrindlyWeekReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getFrindlyWeekPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getFrindlyWeeklReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getFrindlyWeekPlainText);
					}
					break;
				case GET_VALUE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, GetRequest.GET_REQUEST_LLC.getByteValue());
					byte[] getValuePlainText = new byte[] {};
					
					getValuePlainText = DataUtil.append(getValuePlainText, GetRequest.GET_REQUEST.getByteValue());
					getValuePlainText = DataUtil.append(getValuePlainText, GetRequest.NORMAL.getByteValue());
					getValuePlainText = DataUtil.append(getValuePlainText, GetRequest.GET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					logger.debug("procedure[GET_VALUE], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"]");
										
					getValuePlainText = DataUtil.append(getValuePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					getValuePlainText = DataUtil.append(getValuePlainText, obisCodeByte);
					getValuePlainText = DataUtil.append(getValuePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					getValuePlainText = DataUtil.append(getValuePlainText, Hex.encode("00"));
					
					if(isProtocalHLS()) {
						HLSAuthForECG getValueReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] getValueReqValue = getValueReqAuth.getReqEncryptionGlobalCiphering(getInvoCounter(), getValuePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getValueReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, getValuePlainText);
					}
					break;
				default:
					break;
				}

				break;
			case SET_REQ:
				gdDLMSFrame = DataUtil.append(super.encode(hdlcType, null, null, command), gdDLMSFrame);
				switch (procedure) {
				case SET_MINOVER_THRESHOLD_DURATION:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setThresHoldDurationPlainText = new byte[] {};
					
					setThresHoldDurationPlainText = DataUtil.append(setThresHoldDurationPlainText, SetRequest.SET_REQUEST.getByteValue());
					setThresHoldDurationPlainText = DataUtil.append(setThresHoldDurationPlainText, SetRequest.NORMAL.getByteValue());
					setThresHoldDurationPlainText = DataUtil.append(setThresHoldDurationPlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					setThresHoldDurationPlainText = DataUtil.append(setThresHoldDurationPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setThresHoldDurationPlainText = DataUtil.append(setThresHoldDurationPlainText, obisCodeByte);
					setThresHoldDurationPlainText = DataUtil.append(setThresHoldDurationPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setThresHoldDurationPlainText = DataUtil.append(setThresHoldDurationPlainText, Hex.encode("00"));

					//value = (String) param.get("value");
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						jsonArr = JSONArray.fromObject(value);
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];

					setThresHoldDurationPlainText = DataUtil.append(setThresHoldDurationPlainText, new byte[] { (byte) DataType.UINT32.getValue() });
					setThresHoldDurationPlainText = DataUtil.append(setThresHoldDurationPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(map.get("value")))));

					if(isProtocalHLS()) {
						HLSAuthForECG setThresHoldDurationReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setThresHoldDurationReqValue = setThresHoldDurationReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setThresHoldDurationPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setThresHoldDurationReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setThresHoldDurationPlainText);
					}
					break;
				case SET_THRESHOLD_NORMAL:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setThresHoldNormalPlainText = new byte[] {};
					
					setThresHoldNormalPlainText = DataUtil.append(setThresHoldNormalPlainText, SetRequest.SET_REQUEST.getByteValue());
					setThresHoldNormalPlainText = DataUtil.append(setThresHoldNormalPlainText, SetRequest.NORMAL.getByteValue());
					setThresHoldNormalPlainText = DataUtil.append(setThresHoldNormalPlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					setThresHoldNormalPlainText = DataUtil.append(setThresHoldNormalPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setThresHoldNormalPlainText = DataUtil.append(setThresHoldNormalPlainText, obisCodeByte);
					setThresHoldNormalPlainText = DataUtil.append(setThresHoldNormalPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setThresHoldNormalPlainText = DataUtil.append(setThresHoldNormalPlainText, Hex.encode("00"));

					//value = (String) param.get("value");
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						jsonArr = JSONArray.fromObject(value);
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];

					setThresHoldNormalPlainText = DataUtil.append(setThresHoldNormalPlainText, new byte[] { (byte) DataType.UINT32.getValue() });
					setThresHoldNormalPlainText = DataUtil.append(setThresHoldNormalPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(map.get("value")))));

					if(isProtocalHLS()) {
						HLSAuthForECG setThresHoldNormalReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setThresHoldNormalReqValue = setThresHoldNormalReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setThresHoldNormalPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setThresHoldNormalReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setThresHoldNormalPlainText);
					}
					break;
				case SET_PROFILE_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setProfilePeriodText = new byte[] {};
					
					setProfilePeriodText = DataUtil.append(setProfilePeriodText, SetRequest.SET_REQUEST.getByteValue());
					setProfilePeriodText = DataUtil.append(setProfilePeriodText, SetRequest.NORMAL.getByteValue());
					setProfilePeriodText = DataUtil.append(setProfilePeriodText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					setProfilePeriodText = DataUtil.append(setProfilePeriodText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setProfilePeriodText = DataUtil.append(setProfilePeriodText, obisCodeByte);
					setProfilePeriodText = DataUtil.append(setProfilePeriodText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setProfilePeriodText = DataUtil.append(setProfilePeriodText, Hex.encode("00"));

					//value = (String) param.get("value");
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						jsonArr = JSONArray.fromObject(value);
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];

					setProfilePeriodText = DataUtil.append(setProfilePeriodText, new byte[] { (byte) DataType.UINT32.getValue() });
					setProfilePeriodText = DataUtil.append(setProfilePeriodText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(map.get("value")))));

					if(isProtocalHLS()) {
						HLSAuthForECG setProfilePeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setProfilePeriodReqValue = setProfilePeriodReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setProfilePeriodText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setProfilePeriodReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setProfilePeriodText);
					}
					break;
				case SET_STS_REGISTER_VALUE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setSTSRegisterValueText = new byte[] {};
					
					setSTSRegisterValueText = DataUtil.append(setSTSRegisterValueText, SetRequest.SET_REQUEST.getByteValue());
					setSTSRegisterValueText = DataUtil.append(setSTSRegisterValueText, SetRequest.NORMAL.getByteValue());
					setSTSRegisterValueText = DataUtil.append(setSTSRegisterValueText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");
					
					logger.debug("procedure[GET_VALUE], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"]");

					setSTSRegisterValueText = DataUtil.append(setSTSRegisterValueText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setSTSRegisterValueText = DataUtil.append(setSTSRegisterValueText, obisCodeByte);
					setSTSRegisterValueText = DataUtil.append(setSTSRegisterValueText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setSTSRegisterValueText = DataUtil.append(setSTSRegisterValueText, Hex.encode("00"));
					
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + ", replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						try {
							jsonArr = JSONArray.fromObject(value);
						}catch(JSONException e) {
							Map<String,String> valueMap = new HashMap<String, String>();							
							valueMap.put("value", value);
							jsonArr = JSONArray.fromObject(CommandGW.meterParamMapToJSON(valueMap));
						}
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];
					
					setSTSRegisterValueText = DataUtil.append(setSTSRegisterValueText, new byte[] { (byte) DataType.UINT32.getValue() });
					setSTSRegisterValueText = DataUtil.append(setSTSRegisterValueText, DataUtil.get4ByteToInt( Integer.parseInt(String.valueOf(map.get("value"))) ));

					if(isProtocalHLS()) {
						HLSAuthForECG setSTSRegisterValueReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setSTSRegisterValueReqValue = setSTSRegisterValueReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setSTSRegisterValueText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setSTSRegisterValueReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setSTSRegisterValueText);
					}
					break;
				case SET_REGISTER_VALUE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setRegisterValueText = new byte[] {};
					
					setRegisterValueText = DataUtil.append(setRegisterValueText, SetRequest.SET_REQUEST.getByteValue());
					setRegisterValueText = DataUtil.append(setRegisterValueText, SetRequest.NORMAL.getByteValue());
					setRegisterValueText = DataUtil.append(setRegisterValueText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					setRegisterValueText = DataUtil.append(setRegisterValueText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setRegisterValueText = DataUtil.append(setRegisterValueText, obisCodeByte);
					setRegisterValueText = DataUtil.append(setRegisterValueText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setRegisterValueText = DataUtil.append(setRegisterValueText, Hex.encode("00"));

					//value = (String) param.get("value");
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + ", replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						jsonArr = JSONArray.fromObject(value);
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];

					setRegisterValueText = DataUtil.append(setRegisterValueText, new byte[] { (byte) DataType.UINT32.getValue() });
					setRegisterValueText = DataUtil.append(setRegisterValueText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(map.get("value")))));

					if(isProtocalHLS()) {
						HLSAuthForECG setRegisterValueReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setRegisterValueReqValue = setRegisterValueReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setRegisterValueText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setRegisterValueReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setRegisterValueText);
					}
					break;
				case SET_REGISTER_UNIT:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setRegisterUnitText = new byte[] {};
					
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, SetRequest.SET_REQUEST.getByteValue());
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, SetRequest.NORMAL.getByteValue());
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					setRegisterUnitText = DataUtil.append(setRegisterUnitText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, obisCodeByte);
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, Hex.encode("00"));

					//value = (String) param.get("value");
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						jsonArr = JSONArray.fromObject(value);
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];
					
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, new byte[] { (byte) DataType.STRUCTURE.getValue() });
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, new byte[] { (byte) DataType.INT8.getValue() });
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("scaler"))) });
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, new byte[] { (byte) DataType.ENUM.getValue() });
					setRegisterUnitText = DataUtil.append(setRegisterUnitText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("unit"))) });

					if(isProtocalHLS()) {
						HLSAuthForECG setRegisterUnitReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setRegisterUnitReqValue = setRegisterUnitReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setRegisterUnitText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setRegisterUnitReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setRegisterUnitText);
					}
					break;
				case SET_STS_SWITCH_TIME:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setSwitchTimePlainText = new byte[] {};
					
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, SetRequest.SET_REQUEST.getByteValue());
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, SetRequest.NORMAL.getByteValue());
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");
					
					logger.debug("procedure[SET_STS_SWITCH_TIME], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"]");
					
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, obisCodeByte);
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, Hex.encode("00"));
					
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						jsonArr = JSONArray.fromObject(value);
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];
					
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) DataType.DATETIME.getValue() });
					
					logger.debug("obisCode["+Hex.decode(obisCodeByte)+"] classId["+classId+"] attributeNo["+attributeNo+"] pcTime["+String.valueOf(map.get("pcTime"))+"]");
					String pcSwitchTime = String.valueOf(map.get("pcTime"));
					if("true".equals(pcSwitchTime)) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
						String dateTime = sdf.format(new Date());						       
			    		Calendar cal = DateTimeUtil.getCalendar(dateTime);

			    		setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, DataUtil.get2ByteToInt(Integer.parseInt(dateTime.substring(0, 4))));
			    		setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(4, 6))});
			    		setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(6, 8))});
			    		setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(cal.get(Calendar.DAY_OF_WEEK) == 1 ? "7" : (String.valueOf(cal.get(Calendar.DAY_OF_WEEK) - 1))))});
			    		setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(8, 10))});
			    		setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(10, 12))});
			    		setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(12, 14))});
					} else {
						setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(map.get("year")))));
						setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("month"))) });
						setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("dayOfMonth"))) });
						setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("dayOfWeek"))) });
						setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("hh"))) });
						setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("mm"))) });
						setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("ss"))) });
					}
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) 0x00 });
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) 0x80 });
					setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) 0 });
					
					int daySwitchlight = Integer.parseInt(String.valueOf(map.get("daylight")));
					if(daySwitchlight == 0) {
						setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) 0x00 });
					} else {
						setSwitchTimePlainText = DataUtil.append(setSwitchTimePlainText, new byte[] { (byte) 0x80 });
					}
					
					if(isProtocalHLS()) {
						HLSAuthForECG setSwitchTimeReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setSwitchTimeReqValue = setSwitchTimeReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setSwitchTimePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setSwitchTimeReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setSwitchTimePlainText);
					}
					
					break;
				case SET_METER_TIME:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setMeterTimePlainText = new byte[] {};
					
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, SetRequest.SET_REQUEST.getByteValue());
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, SetRequest.NORMAL.getByteValue());
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");
					
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, obisCodeByte);
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, Hex.encode("00"));

					//value = (String) param.get("value");
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						jsonArr = JSONArray.fromObject(value);
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];
					
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) DataType.OCTET_STRING.getValue() });
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { 12 });
					
					logger.debug("obisCode["+Hex.decode(obisCodeByte)+"] classId["+classId+"] attributeNo["+attributeNo+"] pcTime["+String.valueOf(map.get("pcTime"))+"]");
					String pcTime = String.valueOf(map.get("pcTime"));
					if("true".equals(pcTime)) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
						String dateTime = sdf.format(new Date());						       
			    		Calendar cal = DateTimeUtil.getCalendar(dateTime);

			    		setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, DataUtil.get2ByteToInt(Integer.parseInt(dateTime.substring(0, 4))));
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(4, 6))});
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(6, 8))});
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(cal.get(Calendar.DAY_OF_WEEK) == 1 ? "7" : (String.valueOf(cal.get(Calendar.DAY_OF_WEEK) - 1))))});
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(8, 10))});
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(10, 12))});
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(dateTime.substring(12, 14))});
					} else {
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(map.get("year")))));
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("month"))) });
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("dayOfMonth"))) });
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("dayOfWeek"))) });
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("hh"))) });
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("mm"))) });
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) Integer.parseInt(String.valueOf(map.get("ss"))) });
					}
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) 0xff });
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) 0x80 });
					setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) 0 });
					
					int daylight = Integer.parseInt(String.valueOf(map.get("daylight")));
					if(daylight == 0) {
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) 0x00 });
					} else {
						setMeterTimePlainText = DataUtil.append(setMeterTimePlainText, new byte[] { (byte) 0x80 });
					}
					
					if(isProtocalHLS()) {
						HLSAuthForECG setMeterTimeReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setMeterTimeReqValue = setMeterTimeReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setMeterTimePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMeterTimeReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMeterTimePlainText);
					}
					break;
				/*
				 * Image Transfer 관련 프로시저	
				 */
				case SET_IMAGE_TRANSFER_ENABLE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setImageTransferEnablePlainText = new byte[] {};
					
					setImageTransferEnablePlainText = DataUtil.append(setImageTransferEnablePlainText, SetRequest.SET_REQUEST.getByteValue());
					setImageTransferEnablePlainText = DataUtil.append(setImageTransferEnablePlainText, SetRequest.NORMAL.getByteValue());
					setImageTransferEnablePlainText = DataUtil.append(setImageTransferEnablePlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					setImageTransferEnablePlainText = DataUtil.append(setImageTransferEnablePlainText, ImageTransfer.CLASS_ID.getByteValue());
					setImageTransferEnablePlainText = DataUtil.append(setImageTransferEnablePlainText, ImageTransfer.OBIS_CODE.getByteValue());
					setImageTransferEnablePlainText = DataUtil.append(setImageTransferEnablePlainText, ImageTransferAttributes.IMAGE_TRANSFER_ENABLED.getByteValue());
					setImageTransferEnablePlainText = DataUtil.append(setImageTransferEnablePlainText, ImageTransfer.OPTION_NOT_USE.getByteValue());
					byte[] enableOption = new byte[2];
					enableOption[0] = (byte) 0x03;
					enableOption[1] = (byte) 0x01;    // Enable True.
					setImageTransferEnablePlainText = DataUtil.append(setImageTransferEnablePlainText, enableOption);
					
					if(isProtocalHLS()) {
						HLSAuthForECG setImageTransferEnableReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setImageTransferEnableReqValue = setImageTransferEnableReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setImageTransferEnablePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setImageTransferEnableReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setImageTransferEnablePlainText);
					}
					break;					

				/*
				 *   Billing Cycle 관련 프로시져 
				 */
				case SET_BILLING_CYCLE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setBillingCyclePlainText = new byte[] {};
					
					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, SetRequest.SET_REQUEST.getByteValue());
					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, SetRequest.NORMAL.getByteValue());
					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, MeterBillingCycleAttributes.CLASS_ID.getByteValue());
					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, MeterBillingCycleAttributes.OBIS_CODE.getByteValue());
					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, MeterBillingCycleAttributes.BILLING_CYCLE_EXECUTION_TIME.getByteValue());
					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, MeterBillingCycleAttributes.OPTION_NOT_USE.getByteValue());

					byte[] structureA = new byte[4];
					structureA[0] = (byte) 0x01;
					structureA[1] = (byte) 0x01;
					structureA[2] = (byte) 0x02;
					structureA[3] = (byte) 0x02;
					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, structureA);

					String[] times = String.valueOf(param.get("time")).split(":");
					byte[] bilingPartA = new byte[6];
					bilingPartA[0] = (byte) 0x09;
					bilingPartA[1] = (byte) 0x04;
					bilingPartA[2] = (byte) DataUtil.getByteToInt(Integer.parseInt(times[0]));
					bilingPartA[3] = (byte) DataUtil.getByteToInt(Integer.parseInt(times[1]));
					bilingPartA[4] = (byte) DataUtil.getByteToInt(Integer.parseInt(times[2]));
					bilingPartA[5] = (byte) 0x00;
					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, bilingPartA);

					int day = Integer.parseInt(String.valueOf(param.get("day")));
					byte[] bilingPartB = new byte[7];
					bilingPartB[0] = (byte) 0x09;
					bilingPartB[1] = (byte) 0x05;
					bilingPartB[2] = (byte) 0xFF;
					bilingPartB[3] = (byte) 0xFF;
					bilingPartB[4] = (byte) 0xFF;
					bilingPartB[5] = DataUtil.getByteToInt(day);
					bilingPartB[6] = (byte) 0xFF;
					setBillingCyclePlainText = DataUtil.append(setBillingCyclePlainText, bilingPartB);
					
					if(isProtocalHLS()) {
						HLSAuthForECG setBillingCycleReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setBillingCycleReqValue = setBillingCycleReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setBillingCyclePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setBillingCycleReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setBillingCyclePlainText);
					}
					break;
				/*
				 * Demand Period 관련 프로시저
				 */
				case SET_DEMAND_PLUS_A_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setPlusA_PeriodPlainText = new byte[] {};
					
					setPlusA_PeriodPlainText = DataUtil.append(setPlusA_PeriodPlainText, SetRequest.SET_REQUEST.getByteValue());
					setPlusA_PeriodPlainText = DataUtil.append(setPlusA_PeriodPlainText, SetRequest.NORMAL.getByteValue());
					setPlusA_PeriodPlainText = DataUtil.append(setPlusA_PeriodPlainText, new byte[] { 0x40 });

					setPlusA_PeriodPlainText = DataUtil.append(setPlusA_PeriodPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setPlusA_PeriodPlainText = DataUtil.append(setPlusA_PeriodPlainText, DemandPeriodAttributes.OBIS_DEMAND_PLUS_A_PERIOD.getByteValue());
					setPlusA_PeriodPlainText = DataUtil.append(setPlusA_PeriodPlainText, DemandPeriodAttributes.PEROID.getByteValue());
					setPlusA_PeriodPlainText = DataUtil.append(setPlusA_PeriodPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setPlusA_PeriodPlainText = DataUtil.append(setPlusA_PeriodPlainText, new byte[] { 0x06 });
					setPlusA_PeriodPlainText = DataUtil.append(setPlusA_PeriodPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("period")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setPlusPeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setPlusPeriodReqValue = setPlusPeriodReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setPlusA_PeriodPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusPeriodReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusA_PeriodPlainText);
					}
					break;
				case SET_DEMAND_PLUS_A_NUMBER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setPlusA_NumberPlainText = new byte[] {};
					
					setPlusA_NumberPlainText = DataUtil.append(setPlusA_NumberPlainText, SetRequest.SET_REQUEST.getByteValue());
					setPlusA_NumberPlainText = DataUtil.append(setPlusA_NumberPlainText, SetRequest.NORMAL.getByteValue());
					setPlusA_NumberPlainText = DataUtil.append(setPlusA_NumberPlainText, new byte[] { 0x41 });

					setPlusA_NumberPlainText = DataUtil.append(setPlusA_NumberPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setPlusA_NumberPlainText = DataUtil.append(setPlusA_NumberPlainText, DemandPeriodAttributes.OBIS_DEMAND_PLUS_A_NUMBER.getByteValue());
					setPlusA_NumberPlainText = DataUtil.append(setPlusA_NumberPlainText, DemandPeriodAttributes.NUMBER_OF_PERIODS.getByteValue());
					setPlusA_NumberPlainText = DataUtil.append(setPlusA_NumberPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setPlusA_NumberPlainText = DataUtil.append(setPlusA_NumberPlainText, new byte[] { 0x12 });
					setPlusA_NumberPlainText = DataUtil.append(setPlusA_NumberPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(param.get("number")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setPlusNumberReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setPlusNumberReqValue = setPlusNumberReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setPlusA_NumberPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusNumberReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusA_NumberPlainText);
					}
					break;
				case SET_DEMAND_MINUS_A_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setMinusA_PeriodPlainText = new byte[] {};
					
					setMinusA_PeriodPlainText = DataUtil.append(setMinusA_PeriodPlainText, SetRequest.SET_REQUEST.getByteValue());
					setMinusA_PeriodPlainText = DataUtil.append(setMinusA_PeriodPlainText, SetRequest.NORMAL.getByteValue());
					setMinusA_PeriodPlainText = DataUtil.append(setMinusA_PeriodPlainText, new byte[] { 0x42 });

					setMinusA_PeriodPlainText = DataUtil.append(setMinusA_PeriodPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setMinusA_PeriodPlainText = DataUtil.append(setMinusA_PeriodPlainText, DemandPeriodAttributes.OBIS_DEMAND_MINUS_A_PERIOD.getByteValue());
					setMinusA_PeriodPlainText = DataUtil.append(setMinusA_PeriodPlainText, DemandPeriodAttributes.PEROID.getByteValue());
					setMinusA_PeriodPlainText = DataUtil.append(setMinusA_PeriodPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setMinusA_PeriodPlainText = DataUtil.append(setMinusA_PeriodPlainText, new byte[] { 0x06 });
					setMinusA_PeriodPlainText = DataUtil.append(setMinusA_PeriodPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("period")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setMinusPeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setMinusPeriodReqValue = setMinusPeriodReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setMinusA_PeriodPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusPeriodReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusA_PeriodPlainText);
					}
					break;
				case SET_DEMAND_MINUS_A_NUMBER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setMinusA_NumberPlainText = new byte[] {};
					
					setMinusA_NumberPlainText = DataUtil.append(setMinusA_NumberPlainText, SetRequest.SET_REQUEST.getByteValue());
					setMinusA_NumberPlainText = DataUtil.append(setMinusA_NumberPlainText, SetRequest.NORMAL.getByteValue());
					setMinusA_NumberPlainText = DataUtil.append(setMinusA_NumberPlainText, new byte[] { 0x43 });

					setMinusA_NumberPlainText = DataUtil.append(setMinusA_NumberPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setMinusA_NumberPlainText = DataUtil.append(setMinusA_NumberPlainText, DemandPeriodAttributes.OBIS_DEMAND_MINUS_A_NUMBER.getByteValue());
					setMinusA_NumberPlainText = DataUtil.append(setMinusA_NumberPlainText, DemandPeriodAttributes.NUMBER_OF_PERIODS.getByteValue());
					setMinusA_NumberPlainText = DataUtil.append(setMinusA_NumberPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setMinusA_NumberPlainText = DataUtil.append(setMinusA_NumberPlainText, new byte[] { 0x12 });
					setMinusA_NumberPlainText = DataUtil.append(setMinusA_NumberPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(param.get("number")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setMinusNumberReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setMinusNumberReqValue = setMinusNumberReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setMinusA_NumberPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusNumberReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusA_NumberPlainText);
					}
					break;
				case SET_DEMAND_PLUS_R_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setPlusR_PeriodPlainText = new byte[] {};
					
					setPlusR_PeriodPlainText = DataUtil.append(setPlusR_PeriodPlainText, SetRequest.SET_REQUEST.getByteValue());
					setPlusR_PeriodPlainText = DataUtil.append(setPlusR_PeriodPlainText, SetRequest.NORMAL.getByteValue());
					setPlusR_PeriodPlainText = DataUtil.append(setPlusR_PeriodPlainText, new byte[] { 0x44 });

					setPlusR_PeriodPlainText = DataUtil.append(setPlusR_PeriodPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setPlusR_PeriodPlainText = DataUtil.append(setPlusR_PeriodPlainText, DemandPeriodAttributes.OBIS_DEMAND_PLUS_R_PERIOD.getByteValue());
					setPlusR_PeriodPlainText = DataUtil.append(setPlusR_PeriodPlainText, DemandPeriodAttributes.PEROID.getByteValue());
					setPlusR_PeriodPlainText = DataUtil.append(setPlusR_PeriodPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setPlusR_PeriodPlainText = DataUtil.append(setPlusR_PeriodPlainText, new byte[] { 0x06 });
					setPlusR_PeriodPlainText = DataUtil.append(setPlusR_PeriodPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("period")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setPlusR_PeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setPlusR_PeriodReqValue = setPlusR_PeriodReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setPlusR_PeriodPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusR_PeriodReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusR_PeriodPlainText);
					}
					break;
				case SET_DEMAND_PLUS_R_NUMBER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setPlusR_NumberPlainText = new byte[] {};
					
					setPlusR_NumberPlainText = DataUtil.append(setPlusR_NumberPlainText, SetRequest.SET_REQUEST.getByteValue());
					setPlusR_NumberPlainText = DataUtil.append(setPlusR_NumberPlainText, SetRequest.NORMAL.getByteValue());
					setPlusR_NumberPlainText = DataUtil.append(setPlusR_NumberPlainText, new byte[] { 0x45 });

					setPlusR_NumberPlainText = DataUtil.append(setPlusR_NumberPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setPlusR_NumberPlainText = DataUtil.append(setPlusR_NumberPlainText, DemandPeriodAttributes.OBIS_DEMAND_PLUS_R_NUMBER.getByteValue());
					setPlusR_NumberPlainText = DataUtil.append(setPlusR_NumberPlainText, DemandPeriodAttributes.NUMBER_OF_PERIODS.getByteValue());
					setPlusR_NumberPlainText = DataUtil.append(setPlusR_NumberPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setPlusR_NumberPlainText = DataUtil.append(setPlusR_NumberPlainText, new byte[] { 0x12 });
					setPlusR_NumberPlainText = DataUtil.append(setPlusR_NumberPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(param.get("number")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setPlusR_NumberReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setPlusR_NumberReqValue = setPlusR_NumberReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setPlusR_NumberPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusR_NumberReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusR_NumberPlainText);
					}
					break;
				case SET_DEMAND_MINUS_R_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setMinusR_PeriodPlainText = new byte[] {};
					
					setMinusR_PeriodPlainText = DataUtil.append(setMinusR_PeriodPlainText, SetRequest.SET_REQUEST.getByteValue());
					setMinusR_PeriodPlainText = DataUtil.append(setMinusR_PeriodPlainText, SetRequest.NORMAL.getByteValue());
					setMinusR_PeriodPlainText = DataUtil.append(setMinusR_PeriodPlainText, new byte[] { 0x46 });

					setMinusR_PeriodPlainText = DataUtil.append(setMinusR_PeriodPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setMinusR_PeriodPlainText = DataUtil.append(setMinusR_PeriodPlainText, DemandPeriodAttributes.OBIS_DEMAND_MINUS_R_PERIOD.getByteValue());
					setMinusR_PeriodPlainText = DataUtil.append(setMinusR_PeriodPlainText, DemandPeriodAttributes.PEROID.getByteValue());
					setMinusR_PeriodPlainText = DataUtil.append(setMinusR_PeriodPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setMinusR_PeriodPlainText = DataUtil.append(setMinusR_PeriodPlainText, new byte[] { 0x06 });
					setMinusR_PeriodPlainText = DataUtil.append(setMinusR_PeriodPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("period")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setMinusR_PeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setMinusR_PeriodReqValue = setMinusR_PeriodReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setMinusR_PeriodPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusR_PeriodReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusR_PeriodPlainText);
					}
					break;
				case SET_DEMAND_MINUS_R_NUMBER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setMinusR_NumberPlainText = new byte[] {};
					
					setMinusR_NumberPlainText = DataUtil.append(setMinusR_NumberPlainText, SetRequest.SET_REQUEST.getByteValue());
					setMinusR_NumberPlainText = DataUtil.append(setMinusR_NumberPlainText, SetRequest.NORMAL.getByteValue());
					setMinusR_NumberPlainText = DataUtil.append(setMinusR_NumberPlainText, new byte[] { 0x47 });

					setMinusR_NumberPlainText = DataUtil.append(setMinusR_NumberPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setMinusR_NumberPlainText = DataUtil.append(setMinusR_NumberPlainText, DemandPeriodAttributes.OBIS_DEMAND_MINUS_R_NUMBER.getByteValue());
					setMinusR_NumberPlainText = DataUtil.append(setMinusR_NumberPlainText, DemandPeriodAttributes.NUMBER_OF_PERIODS.getByteValue());
					setMinusR_NumberPlainText = DataUtil.append(setMinusR_NumberPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setMinusR_NumberPlainText = DataUtil.append(setMinusR_NumberPlainText, new byte[] { 0x12 });
					setMinusR_NumberPlainText = DataUtil.append(setMinusR_NumberPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(param.get("number")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setMinusR_NumberReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setMinusR_NumberReqValue = setMinusR_NumberReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setMinusR_NumberPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusR_NumberReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusR_NumberPlainText);
					}
					break;
				case SET_DEMAND_R_QI_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setR_QI_PeriodPlainText = new byte[] {};
					
					setR_QI_PeriodPlainText = DataUtil.append(setR_QI_PeriodPlainText, SetRequest.SET_REQUEST.getByteValue());
					setR_QI_PeriodPlainText = DataUtil.append(setR_QI_PeriodPlainText, SetRequest.NORMAL.getByteValue());
					setR_QI_PeriodPlainText = DataUtil.append(setR_QI_PeriodPlainText, new byte[] { 0x48 });

					setR_QI_PeriodPlainText = DataUtil.append(setR_QI_PeriodPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setR_QI_PeriodPlainText = DataUtil.append(setR_QI_PeriodPlainText, DemandPeriodAttributes.OBIS_DEMAND_R_QI_PERIOD.getByteValue());
					setR_QI_PeriodPlainText = DataUtil.append(setR_QI_PeriodPlainText, DemandPeriodAttributes.PEROID.getByteValue());
					setR_QI_PeriodPlainText = DataUtil.append(setR_QI_PeriodPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setR_QI_PeriodPlainText = DataUtil.append(setR_QI_PeriodPlainText, new byte[] { 0x06 });
					setR_QI_PeriodPlainText = DataUtil.append(setR_QI_PeriodPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("period")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setR_QI_PeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setR_QI_PeriodReqValue = setR_QI_PeriodReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setR_QI_PeriodPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setR_QI_PeriodReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setR_QI_PeriodPlainText);
					}
					break;
				case SET_DEMAND_R_QI_NUMBER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setR_QI_NumberPlainText = new byte[] {};
					
					setR_QI_NumberPlainText = DataUtil.append(setR_QI_NumberPlainText, SetRequest.SET_REQUEST.getByteValue());
					setR_QI_NumberPlainText = DataUtil.append(setR_QI_NumberPlainText, SetRequest.NORMAL.getByteValue());
					setR_QI_NumberPlainText = DataUtil.append(setR_QI_NumberPlainText, new byte[] { 0x49 });

					setR_QI_NumberPlainText = DataUtil.append(setR_QI_NumberPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setR_QI_NumberPlainText = DataUtil.append(setR_QI_NumberPlainText, DemandPeriodAttributes.OBIS_DEMAND_R_QI_NUMBER.getByteValue());
					setR_QI_NumberPlainText = DataUtil.append(setR_QI_NumberPlainText, DemandPeriodAttributes.NUMBER_OF_PERIODS.getByteValue());
					setR_QI_NumberPlainText = DataUtil.append(setR_QI_NumberPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setR_QI_NumberPlainText = DataUtil.append(setR_QI_NumberPlainText, new byte[] { 0x12 });
					setR_QI_NumberPlainText = DataUtil.append(setR_QI_NumberPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(param.get("number")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setR_QI_NumberReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setR_QI_NumberReqValue = setR_QI_NumberReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setR_QI_NumberPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setR_QI_NumberReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setR_QI_NumberPlainText);
					}
					break;
				case SET_DEMAND_R_QIV_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setR_QIV_PeriodPlainText = new byte[] {};
					
					setR_QIV_PeriodPlainText = DataUtil.append(setR_QIV_PeriodPlainText, SetRequest.SET_REQUEST.getByteValue());
					setR_QIV_PeriodPlainText = DataUtil.append(setR_QIV_PeriodPlainText, SetRequest.NORMAL.getByteValue());
					setR_QIV_PeriodPlainText = DataUtil.append(setR_QIV_PeriodPlainText, new byte[] { 0x4A });

					setR_QIV_PeriodPlainText = DataUtil.append(setR_QIV_PeriodPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setR_QIV_PeriodPlainText = DataUtil.append(setR_QIV_PeriodPlainText, DemandPeriodAttributes.OBIS_DEMAND_R_QIV_PERIOD.getByteValue());
					setR_QIV_PeriodPlainText = DataUtil.append(setR_QIV_PeriodPlainText, DemandPeriodAttributes.PEROID.getByteValue());
					setR_QIV_PeriodPlainText = DataUtil.append(setR_QIV_PeriodPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setR_QIV_PeriodPlainText = DataUtil.append(setR_QIV_PeriodPlainText, new byte[] { 0x06 });
					setR_QIV_PeriodPlainText = DataUtil.append(setR_QIV_PeriodPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("period")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setR_QIV_PeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setR_QIV_PeriodReqValue = setR_QIV_PeriodReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setR_QIV_PeriodPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setR_QIV_PeriodReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setR_QIV_PeriodPlainText);
					}
					break;
				case SET_DEMAND_R_QIV_NUMBER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setR_QIV_NumberPlainText = new byte[] {};
					
					setR_QIV_NumberPlainText = DataUtil.append(setR_QIV_NumberPlainText, SetRequest.SET_REQUEST.getByteValue());
					setR_QIV_NumberPlainText = DataUtil.append(setR_QIV_NumberPlainText, SetRequest.NORMAL.getByteValue());
					setR_QIV_NumberPlainText = DataUtil.append(setR_QIV_NumberPlainText, new byte[] { 0x4B });

					setR_QIV_NumberPlainText = DataUtil.append(setR_QIV_NumberPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setR_QIV_NumberPlainText = DataUtil.append(setR_QIV_NumberPlainText, DemandPeriodAttributes.OBIS_DEMAND_R_QIV_NUMBER.getByteValue());
					setR_QIV_NumberPlainText = DataUtil.append(setR_QIV_NumberPlainText, DemandPeriodAttributes.NUMBER_OF_PERIODS.getByteValue());
					setR_QIV_NumberPlainText = DataUtil.append(setR_QIV_NumberPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setR_QIV_NumberPlainText = DataUtil.append(setR_QIV_NumberPlainText, new byte[] { 0x12 });
					setR_QIV_NumberPlainText = DataUtil.append(setR_QIV_NumberPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(param.get("number")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setR_QIV_NumberReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setR_QIV_NumberReqValue = setR_QIV_NumberReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setR_QIV_NumberPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setR_QIV_NumberReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setR_QIV_NumberPlainText);
					}
					break;
				case SET_DEMAND_PLUS_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setPlusPeriodPlainText = new byte[] {};
					
					setPlusPeriodPlainText = DataUtil.append(setPlusPeriodPlainText, SetRequest.SET_REQUEST.getByteValue());
					setPlusPeriodPlainText = DataUtil.append(setPlusPeriodPlainText, SetRequest.NORMAL.getByteValue());
					setPlusPeriodPlainText = DataUtil.append(setPlusPeriodPlainText, new byte[] { 0x4C });

					setPlusPeriodPlainText = DataUtil.append(setPlusPeriodPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setPlusPeriodPlainText = DataUtil.append(setPlusPeriodPlainText, DemandPeriodAttributes.OBIS_DEMAND_PLUS_PERIOD.getByteValue());
					setPlusPeriodPlainText = DataUtil.append(setPlusPeriodPlainText, DemandPeriodAttributes.PEROID.getByteValue());
					setPlusPeriodPlainText = DataUtil.append(setPlusPeriodPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setPlusPeriodPlainText = DataUtil.append(setPlusPeriodPlainText, new byte[] { 0x06 });
					setPlusPeriodPlainText = DataUtil.append(setPlusPeriodPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("period")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setPlusPeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setPlusPeriodReqValue = setPlusPeriodReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setPlusPeriodPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusPeriodReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusPeriodPlainText);
					}
					break;
				case SET_DEMAND_PLUS_NUMBER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setPlusNumberPlainText = new byte[] {};
					
					setPlusNumberPlainText = DataUtil.append(setPlusNumberPlainText, SetRequest.SET_REQUEST.getByteValue());
					setPlusNumberPlainText = DataUtil.append(setPlusNumberPlainText, SetRequest.NORMAL.getByteValue());
					setPlusNumberPlainText = DataUtil.append(setPlusNumberPlainText, new byte[] { 0x4D });

					setPlusNumberPlainText = DataUtil.append(setPlusNumberPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setPlusNumberPlainText = DataUtil.append(setPlusNumberPlainText, DemandPeriodAttributes.OBIS_DEMAND_PLUS_NUMBER.getByteValue());
					setPlusNumberPlainText = DataUtil.append(setPlusNumberPlainText, DemandPeriodAttributes.NUMBER_OF_PERIODS.getByteValue());
					setPlusNumberPlainText = DataUtil.append(setPlusNumberPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setPlusNumberPlainText = DataUtil.append(setPlusNumberPlainText, new byte[] { 0x12 });
					setPlusNumberPlainText = DataUtil.append(setPlusNumberPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(param.get("number")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setPlusNumberReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setPlusNumberReqValue = setPlusNumberReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setPlusNumberPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusNumberReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusNumberPlainText);
					}
					break;
				case SET_DEMAND_MINUS_PERIOD:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setMinusPeriodPlainText = new byte[] {};
					
					setMinusPeriodPlainText = DataUtil.append(setMinusPeriodPlainText, SetRequest.SET_REQUEST.getByteValue());
					setMinusPeriodPlainText = DataUtil.append(setMinusPeriodPlainText, SetRequest.NORMAL.getByteValue());
					setMinusPeriodPlainText = DataUtil.append(setMinusPeriodPlainText, new byte[] { 0x4E });

					setMinusPeriodPlainText = DataUtil.append(setMinusPeriodPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setMinusPeriodPlainText = DataUtil.append(setMinusPeriodPlainText, DemandPeriodAttributes.OBIS_DEMAND_MINUS_PERIOD.getByteValue());
					setMinusPeriodPlainText = DataUtil.append(setMinusPeriodPlainText, DemandPeriodAttributes.PEROID.getByteValue());
					setMinusPeriodPlainText = DataUtil.append(setMinusPeriodPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setMinusPeriodPlainText = DataUtil.append(setMinusPeriodPlainText, new byte[] { 0x06 });
					setMinusPeriodPlainText = DataUtil.append(setMinusPeriodPlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("period")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setPlusPeriodReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setPlusPeriodReqValue = setPlusPeriodReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setMinusPeriodPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusPeriodReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusPeriodPlainText);
					}
					break;
				case SET_DEMAND_MINUS_NUMBER:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setMinusNumberPlainText = new byte[] {};
					
					setMinusNumberPlainText = DataUtil.append(setMinusNumberPlainText, SetRequest.SET_REQUEST.getByteValue());
					setMinusNumberPlainText = DataUtil.append(setMinusNumberPlainText, SetRequest.NORMAL.getByteValue());
					setMinusNumberPlainText = DataUtil.append(setMinusNumberPlainText, new byte[] { 0x40 });

					setMinusNumberPlainText = DataUtil.append(setMinusNumberPlainText, DemandPeriodAttributes.CLASS_ID.getByteValue());
					setMinusNumberPlainText = DataUtil.append(setMinusNumberPlainText, DemandPeriodAttributes.OBIS_DEMAND_MINUS_NUMBER.getByteValue());
					setMinusNumberPlainText = DataUtil.append(setMinusNumberPlainText, DemandPeriodAttributes.NUMBER_OF_PERIODS.getByteValue());
					setMinusNumberPlainText = DataUtil.append(setMinusNumberPlainText, DemandPeriodAttributes.OPTION_NOT_USE.getByteValue());

					setMinusNumberPlainText = DataUtil.append(setMinusNumberPlainText, new byte[] { 0x12 });
					setMinusNumberPlainText = DataUtil.append(setMinusNumberPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(param.get("number")))));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setPlusNumberReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setPlusNumberReqValue = setPlusNumberReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setMinusNumberPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setPlusNumberReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setMinusNumberPlainText);
					}
					break;
				/*
				 * TOU Setting 관련
				 */
				case SET_CALENDAR_NAME_PASSIVE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setCalendarPassivePlainText = new byte[] {};
					
					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, SetRequest.SET_REQUEST.getByteValue());
					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, SetRequest.NORMAL.getByteValue());
					//setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, new byte[]{0x40});
					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, getPriorityByteValue());

					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, TOUAttributes.CLASS_ID.getByteValue());
					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, TOUAttributes.ACTIVITY_CALENDAR.getByteValue());
					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, TOUAttributes.CALENDAR_NAME_PASSIVE.getByteValue());
					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, TOUAttributes.OPTION_NOT_USE.getByteValue());

					byte[] calendarName = String.valueOf(param.get("calendarNamePassive")).getBytes();
					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, new byte[] { 0x09 });
					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, new byte[] { DataUtil.getByteToInt(calendarName.length) });
					setCalendarPassivePlainText = DataUtil.append(setCalendarPassivePlainText, calendarName);
					
					if(isProtocalHLS()) {
						HLSAuthForECG setCalendarPassiveReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setCalendarPassiveReqValue = setCalendarPassiveReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setCalendarPassivePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setCalendarPassiveReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setCalendarPassivePlainText);
					}
					break;
				case SET_SEASON_PROFILE:
				case SET_WEEK_PROFILE:
				case SET_DAY_PROFILE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setDateProfilePlainText = new byte[] {};
					
					setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, SetRequest.SET_REQUEST.getByteValue());

					if (param.get("infoBlockType") == TOUInfoBlockType.FIRST_BLOCK && !param.containsKey("blockNumber")) { // Single모드 전송인경우
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, SetRequest.NORMAL.getByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, getPriorityByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.CLASS_ID.getByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.ACTIVITY_CALENDAR.getByteValue());

						if (procedure == Procedure.SET_SEASON_PROFILE) {
							setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.SEASON_PROFILE_PASSIVE.getByteValue());
						} else if (procedure == Procedure.SET_WEEK_PROFILE) {
							setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.WEEK_PROFILE_TABLE_PASSIVE.getByteValue());
						} else if (procedure == Procedure.SET_DAY_PROFILE) {
							setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.DAY_PROFILE_TABLE_PASSIVE.getByteValue());
						}

						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.OPTION_NOT_USE.getByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, (byte[]) param.get("blockValue"));

					} else if (param.get("infoBlockType") == TOUInfoBlockType.FIRST_BLOCK && param.containsKey("blockNumber")) { // Multi 모드 전송인경우
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, SetRequest.WITH_FIRST_DATABLOCK.getByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, getPriorityByteValue(true));
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.CLASS_ID.getByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.ACTIVITY_CALENDAR.getByteValue());

						if (procedure == Procedure.SET_SEASON_PROFILE) {
							setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.SEASON_PROFILE_PASSIVE.getByteValue());
						} else if (procedure == Procedure.SET_WEEK_PROFILE) {
							setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.WEEK_PROFILE_TABLE_PASSIVE.getByteValue());
						} else if (procedure == Procedure.SET_DAY_PROFILE) {
							setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.DAY_PROFILE_TABLE_PASSIVE.getByteValue());
						}

						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, TOUAttributes.OPTION_NOT_USE.getByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, (boolean) param.get("isLastBlock") == true ? new byte[] { (byte) 0xFF } : new byte[] { 0x00 });
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("blockNumber")))));

						int bLength = Integer.parseInt(String.valueOf(param.get("blockLength")));
						byte[] blockLength;
						if (128 <= bLength) {
							blockLength = new byte[] { (byte) 0x81, DataUtil.getByteToInt(bLength) };
						} else {
							blockLength = new byte[] { DataUtil.getByteToInt(bLength) };
						}
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, blockLength);
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, (byte[]) param.get("blockValue"));

					} else if (param.get("infoBlockType") == TOUInfoBlockType.MIDDLE_BLOCK) {
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, SetRequest.WITH_DATABLOCK.getByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, getPriorityByteValue(true));
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, (boolean) param.get("isLastBlock") == true ? new byte[] { (byte) 0xFF } : new byte[] { 0x00 });
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("blockNumber")))));

						int bLength = Integer.parseInt(String.valueOf(param.get("blockLength")));
						byte[] blockLength;
						if (128 <= bLength) {
							blockLength = new byte[] { (byte) 0x81, DataUtil.getByteToInt(bLength) };
						} else {
							blockLength = new byte[] { DataUtil.getByteToInt(bLength) };
						}
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, blockLength);
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, (byte[]) param.get("blockValue"));

					} else if (param.get("infoBlockType") == TOUInfoBlockType.LAST_BLOCK) {
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, SetRequest.WITH_DATABLOCK.getByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, getPriorityByteValue());
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, (boolean) param.get("isLastBlock") == true ? new byte[] { (byte) 0xFF } : new byte[] { 0x00 });
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(param.get("blockNumber")))));

						int bLength = Integer.parseInt(String.valueOf(param.get("blockLength")));
						byte[] blockLength;
						if (128 <= bLength) {
							blockLength = new byte[] { (byte) 0x81, DataUtil.getByteToInt(bLength) };
						} else {
							blockLength = new byte[] { DataUtil.getByteToInt(bLength) };
						}
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, blockLength);
						setDateProfilePlainText = DataUtil.append(setDateProfilePlainText, (byte[]) param.get("blockValue"));
					}

					if(isProtocalHLS()) {
						HLSAuthForECG setDateProfileReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setDateProfileReqValue = setDateProfileReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setDateProfilePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setDateProfileReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setDateProfilePlainText);
					}
					break;
				case SET_STARTING_DATE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setStaringDatePlainText = new byte[] {};
					
					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, SetRequest.SET_REQUEST.getByteValue());
					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, SetRequest.NORMAL.getByteValue());
					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, getPriorityByteValue());

					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, TOUAttributes.CLASS_ID.getByteValue());
					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, TOUAttributes.ACTIVITY_CALENDAR.getByteValue());
					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, TOUAttributes.ACTIVATE_PASSIVE_CALENDAR_TIME.getByteValue());
					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, TOUAttributes.OPTION_NOT_USE.getByteValue());

					String startingDate = String.valueOf(param.get("startingDate"));
					Calendar cal = DateTimeUtil.getCalendar(startingDate);
					byte[] dateTime = DataUtil.getDLMS_OCTETSTRING12ByDateTime(cal);

					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, new byte[] { 0x09 });
					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, new byte[] { DataUtil.getByteToInt(dateTime.length) });
					setStaringDatePlainText = DataUtil.append(setStaringDatePlainText, dateTime);
					
					if(isProtocalHLS()) {
						HLSAuthForECG setStaringDateReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setStaringDateReqValue = setStaringDateReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setStaringDatePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setStaringDateReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setStaringDatePlainText);
					}
					break;
				case SET_DISCONNECT_CONTROL:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setDisconnectControlPlainText = new byte[] {};
					
					setDisconnectControlPlainText = DataUtil.append(setDisconnectControlPlainText, SetRequest.SET_REQUEST.getByteValue());
					setDisconnectControlPlainText = DataUtil.append(setDisconnectControlPlainText, SetRequest.NORMAL.getByteValue());
					setDisconnectControlPlainText = DataUtil.append(setDisconnectControlPlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					setDisconnectControlPlainText = DataUtil.append(setDisconnectControlPlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setDisconnectControlPlainText = DataUtil.append(setDisconnectControlPlainText, obisCodeByte);
					setDisconnectControlPlainText = DataUtil.append(setDisconnectControlPlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setDisconnectControlPlainText = DataUtil.append(setDisconnectControlPlainText, Hex.encode("00"));

					//value = (String) param.get("value");
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						jsonArr = JSONArray.fromObject(value);
					}

					map = (Map<String, Object>) jsonArr.toArray()[0];

					setDisconnectControlPlainText = DataUtil.append(setDisconnectControlPlainText, new byte[] { (byte) DataType.BOOLEAN.getValue() });
					setDisconnectControlPlainText = DataUtil.append(setDisconnectControlPlainText, DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(param.get("value")))));

					if(isProtocalHLS()) {
						HLSAuthForECG setDisconnectControlReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setDisconnectControlReqValue = setDisconnectControlReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setDisconnectControlPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setDisconnectControlReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setDisconnectControlPlainText);
					}
					break;
				case SET_STS_FRIENDLY_DAYS_TABLE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setDayTablePlainText = new byte[] {};
					
					setDayTablePlainText = DataUtil.append(setDayTablePlainText, SetRequest.SET_REQUEST.getByteValue());
					setDayTablePlainText = DataUtil.append(setDayTablePlainText, SetRequest.NORMAL.getByteValue());
					setDayTablePlainText = DataUtil.append(setDayTablePlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					setDayTablePlainText = DataUtil.append(setDayTablePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setDayTablePlainText = DataUtil.append(setDayTablePlainText, obisCodeByte);
					setDayTablePlainText = DataUtil.append(setDayTablePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setDayTablePlainText = DataUtil.append(setDayTablePlainText, Hex.encode("00"));
			
					String[] params = ((String) param.get("value")).split("[,]");
					
					setDayTablePlainText = DataUtil.append(setDayTablePlainText, Hex.encode("01")); //Array
					setDayTablePlainText = DataUtil.append(setDayTablePlainText, DataUtil.getByteToInt(params.length)); //Count
										
					for(int i=0; i<params.length; i++) {
						setDayTablePlainText = DataUtil.append(setDayTablePlainText, new byte[] {0x02, 0x03}); //Structure, Parameter Length
						setDayTablePlainText = DataUtil.append(setDayTablePlainText, new byte[] { (byte) DataType.UINT16.getValue() });
						setDayTablePlainText = DataUtil.append(setDayTablePlainText,  DataUtil.get2ByteToInt(i + 1));
						
						setDayTablePlainText = DataUtil.append(setDayTablePlainText, new byte[] { (byte) DataType.OCTET_STRING.getValue() });
						setDayTablePlainText = DataUtil.append(setDayTablePlainText, new byte[] {0x05});
						setDayTablePlainText = DataUtil.append(setDayTablePlainText, DataUtil.get2ByteToInt(Integer.parseInt(params[i].substring(0, 4))));
						setDayTablePlainText = DataUtil.append(setDayTablePlainText, new byte[] { (byte) Integer.parseInt(params[i].substring(4, 6))});
						setDayTablePlainText = DataUtil.append(setDayTablePlainText, new byte[] { (byte) Integer.parseInt(params[i].substring(6, 8))});
						setDayTablePlainText = DataUtil.append(setDayTablePlainText, new byte[] { 0x04 });
						
						setDayTablePlainText = DataUtil.append(setDayTablePlainText, new byte[] { (byte) DataType.UINT8.getValue() });
						setDayTablePlainText = DataUtil.append(setDayTablePlainText,  DataUtil.getByteToInt(i + 1));
					}
					
					if(isProtocalHLS()) {
						HLSAuthForECG setDayTableReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setDayTablelReqValue = setDayTableReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setDayTablePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setDayTablelReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setDayTablePlainText);
					}
					break;
				case SET_STS_FRIENDLY_TIME:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setFrindlyTimePlainText = new byte[] {};
					
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, SetRequest.SET_REQUEST.getByteValue());
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, SetRequest.NORMAL.getByteValue());
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, FriendlyTimeAttributes.CLASS_ID.getByteValue());
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, FriendlyTimeAttributes.OBIS_CODE.getByteValue());
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, FriendlyTimeAttributes.FRIENDLY_TIME.getByteValue());
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, FriendlyTimeAttributes.OPTION_NOT_USE.getByteValue());
					
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, new byte[] {0x09, 0x04}); //octet-string, length
					String[] timeParam = ((String) param.get("value")).split("[,]");
					
					logger.debug("procedure[SET_STS_FRIENDLY_TIME], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"] dataType["+dataType+"] value["+param.get("value")+"]");
					
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, DataUtil.getByteToInt(timeParam[0].substring(0, 2)));
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, DataUtil.getByteToInt(timeParam[0].substring(2, 4)));
					
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, DataUtil.getByteToInt(timeParam[1].substring(0, 2)));
					setFrindlyTimePlainText = DataUtil.append(setFrindlyTimePlainText, DataUtil.getByteToInt(timeParam[1].substring(2, 4)));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setFrindlyTimeReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setFrindlyTimelReqValue = setFrindlyTimeReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setFrindlyTimePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setFrindlyTimelReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setFrindlyTimePlainText);
					}
					break;
				case SET_STS_FRIENDLY_WEEK:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setFrindlyWeekPlainText = new byte[] {};
					
					setFrindlyWeekPlainText = DataUtil.append(setFrindlyWeekPlainText, SetRequest.SET_REQUEST.getByteValue());
					setFrindlyWeekPlainText = DataUtil.append(setFrindlyWeekPlainText, SetRequest.NORMAL.getByteValue());
					setFrindlyWeekPlainText = DataUtil.append(setFrindlyWeekPlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());
					
					setFrindlyWeekPlainText = DataUtil.append(setFrindlyWeekPlainText, FriendlyWeekAttributes.CLASS_ID.getByteValue());
					setFrindlyWeekPlainText = DataUtil.append(setFrindlyWeekPlainText, FriendlyWeekAttributes.OBIS_CODE.getByteValue());
					setFrindlyWeekPlainText = DataUtil.append(setFrindlyWeekPlainText, FriendlyWeekAttributes.FRIENDLY_TIME.getByteValue());
					setFrindlyWeekPlainText = DataUtil.append(setFrindlyWeekPlainText, FriendlyWeekAttributes.OPTION_NOT_USE.getByteValue());
					
					setFrindlyWeekPlainText = DataUtil.append(setFrindlyWeekPlainText, new byte[] {0x04, 0x08}); //bit-string, length
					
					//0:일요일, 1:월요일, 2:화요일, 3:수요일, 4:목요일, 5:금요일, 6:토요일, 7:Reserved
					int mWeekDay = (byte)0x00;
					String[] weekParam = ((String) param.get("value")).split("[,]");					
					for(String weekDay : weekParam) {
						int hexVal = (int)Math.pow(2, Integer.parseInt(weekDay));
						mWeekDay = mWeekDay | hexVal;
					}
					
					setFrindlyWeekPlainText = DataUtil.append(setFrindlyWeekPlainText, Hex.encode(Integer.toHexString(mWeekDay)));
					
					if(isProtocalHLS()) {
						HLSAuthForECG setFrindlyWeekReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setFrindlyWeeklReqValue = setFrindlyWeekReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setFrindlyWeekPlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setFrindlyWeeklReqValue);
					} else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setFrindlyWeekPlainText);
					}
					break;
				case SET_VALUE:
					gdDLMSFrame = DataUtil.append(gdDLMSFrame, SetRequest.SET_REQUEST_LLC.getByteValue());
					byte[] setValuePlainText = new byte[] {};
					
					setValuePlainText = DataUtil.append(setValuePlainText, SetRequest.SET_REQUEST.getByteValue());
					setValuePlainText = DataUtil.append(setValuePlainText, SetRequest.NORMAL.getByteValue());
					setValuePlainText = DataUtil.append(setValuePlainText, SetRequest.SET_REQUEST_INVOKE_ID_AND_PRIORITY.getByteValue());

					obisCode = (String) param.get("obisCode");
					obisCodeArr = obisCode.split("[.]");
					obisCodeByte = new byte[6];
					for (int i = 0; i < obisCodeArr.length; i++) {
						obisCodeByte[i] = (byte) Integer.parseInt(obisCodeArr[i]);
					}

					classId = (String) param.get("classId");
					attributeNo = (String) param.get("attributeNo");

					setValuePlainText = DataUtil.append(setValuePlainText, DataUtil.get2ByteToInt(String.valueOf(classId)));
					setValuePlainText = DataUtil.append(setValuePlainText, obisCodeByte);
					setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) Integer.parseInt(attributeNo) });
					setValuePlainText = DataUtil.append(setValuePlainText, Hex.encode("00"));
			
					//value = (String) param.get("value");
					value = ((String) param.get("value")).replaceAll("\\\\\"", "\"");
					logger.debug("original=" + (String) param.get("value") + "replaced=" + value);
					dataType = (String) param.get("dataType");
					jsonArr = null;
					if (value == null || value.isEmpty()) {
						jsonArr = new JSONArray();
					} else {
						try {
							jsonArr = JSONArray.fromObject(value);
							map = (Map<String, Object>) jsonArr.toArray()[0];
						}catch(Exception e) {
							map = param;
						}
					}
					
					logger.debug("procedure[SET_VALUE], obisCode["+obisCode+"] classId["+classId+"] attributeNo["+attributeNo+"] dataType["+dataType+"] value["+param.get("value")+"]");
					
					if ("integer".equals(dataType)){
						setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.INT8.getValue() });
						byte data[] = new byte[] { Byte.parseByte(String.valueOf(map.get("value")))};
						logger.debug("integer:" + Hex.decode(data));
						setValuePlainText = DataUtil.append(setValuePlainText,  data);
					}
					else if ( "unsigned".equals(dataType)){
						setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.UINT8.getValue() });
						byte data[] =   new byte[] { DataUtil.getByteToInt(Integer.parseInt(String.valueOf(map.get("value"))))};
						logger.debug("unsigned:" + Hex.decode(data));
						setValuePlainText = DataUtil.append(setValuePlainText,data );		
					}
					else if ( "long".equals(dataType)){
						setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.INT16.getValue() });
						byte data[] = DataUtil.get2ByteToInt(Short.parseShort(String.valueOf(map.get("value"))));
						logger.debug("long:" + Hex.decode(data));
						setValuePlainText = DataUtil.append(setValuePlainText, data);
					}
					else if ( "long-unsigned".equals(dataType)){
						setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.UINT16.getValue() });
						byte data[] = DataUtil.get2ByteToInt(Integer.parseInt(String.valueOf(map.get("value"))));
						logger.debug("long-unsigned:" + Hex.decode(data));
						setValuePlainText = DataUtil.append(setValuePlainText, data);
					}
					else if ( "double-long".equals(dataType)){
						setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.INT32.getValue() });
						byte data[] = DataUtil.get4ByteToInt(Integer.parseInt(String.valueOf(map.get("value"))));
						logger.debug("double-long:" + Hex.decode(data));
						setValuePlainText = DataUtil.append(setValuePlainText, data );
					}
					else if ( "double-long-unsigned".equals(dataType)){
						setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.UINT32.getValue() });
						byte data[] = DataUtil.get4ByteToInt(Integer.parseUnsignedInt(String.valueOf(map.get("value"))));
						logger.debug("double-long-unsigned:" + Hex.decode(data));
						setValuePlainText = DataUtil.append(setValuePlainText, data );
					}
					else if ( "long64".equals(dataType)){
						setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.INT64.getValue() });
						byte data[] = DataUtil.get8ByteToInt(Long.parseLong(String.valueOf(map.get("value"))));
						logger.debug("long64:" + Hex.decode(data));
						setValuePlainText = DataUtil.append(setValuePlainText, data);
					}
					else if ( "long64unsigned".equals(dataType)){
						setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.UINT64.getValue() });
						byte data[] = DataUtil.get8ByteToInt(Long.parseUnsignedLong(String.valueOf(map.get("value"))));
						logger.debug("long64unsigned:" + Hex.decode(data));
						setValuePlainText = DataUtil.append(setValuePlainText, data);			
					}
					else if ( "boolean".equals(dataType)){
						String boolValue = (String)map.get("value");
						if ( "true".equals(boolValue)){
							setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.BOOLEAN.getValue() });
							setValuePlainText = DataUtil.append(setValuePlainText,  new byte[] { (byte) 0xFF } );
						}
						else if ("false".equals(boolValue)){
							setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.BOOLEAN.getValue() });
							setValuePlainText = DataUtil.append(setValuePlainText,  new byte[] { (byte) 0x00 } );
						}
						else {
							logger.error("DLMS Encoding Error - dataType[" + dataType +"] , value["+ boolValue +"]" );
							setValuePlainText = null;	
						}
					}
					else if ( "octet-string".equals(dataType)){
						String strHexVal = String.valueOf(map.get("value"));
						byte[] val = HexUtil.convertHexStringToBytes(strHexVal);

						logger.debug("KEY_HEX=" + HexUtil.conventBytesToHexString(val));
						byte[] data = new byte[2];
						
						data[0] = DLMSCommonDataType.OctetString.getValue();
						data[1] = DataUtil.getByteToInt(val.length);
						setValuePlainText = DataUtil.append(setValuePlainText, data);
						setValuePlainText = DataUtil.append(setValuePlainText, val);
					}
					else if ( "enum".equals(dataType)){
						setValuePlainText = DataUtil.append(setValuePlainText, new byte[] { (byte) DataType.ENUM.getValue() });
						byte data[] =   new byte[] { DataUtil.getByteToInt(Integer.parseInt(String.valueOf(map.get("value"))))};
						logger.debug("enum:" + Hex.decode(data));
						setValuePlainText = DataUtil.append(setValuePlainText,data );		
					} 
					else if( "bit-string".equals(dataType)) {
						String bitHexVal = String.valueOf(map.get("value"));
						byte[] val = HexUtil.convertHexStringToBytes(bitHexVal);
						
						logger.debug("KEY_HEX=" + HexUtil.conventBytesToHexString(val));
						byte[] data = new byte[2];
						
						data[0] = DLMSCommonDataType.BitString.getValue();
						data[1] = DataUtil.getByteToInt(val.length * 8);
						setValuePlainText = DataUtil.append(setValuePlainText, data);
						setValuePlainText = DataUtil.append(setValuePlainText, val);
					}
					else {
						logger.error("DLMS Encoding Error - dataType[" + dataType +"] is not supported" );
						setValuePlainText = null;	
					}
					
					if(isProtocalHLS()) {
						HLSAuthForECG setSetValueReqAuth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION_ENCRYPTION);
						byte[] setSetReqValue = setSetValueReqAuth.setReqEncryptionGlobalCiphering(getInvoCounter(), setValuePlainText);
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setSetReqValue);
					}else {
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, setValuePlainText);
					}
					break;
				default:
					break;
				}
				break;
			case KAIFA_CUSTOM : 
				gdDLMSFrame = DataUtil.append(super.encode(hdlcType, null, null, command), gdDLMSFrame);
				logger.info("####################################################################");
				logger.info("####################################################################");
				logger.info("Error DLMS KAIFA_CUSTOM // procedure="+procedure);
				logger.info("####################################################################");
				logger.info("####################################################################");
				/*
				switch (procedure) {
					case GET_SORIA_METER_KEY_A:
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.KAIFA_CUSTOM_LLC.getByteValue());
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.GET_SORIA_METER_KEY_A.getByteValue());
						break;
					case GET_SORIA_METER_KEY_B:
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.KAIFA_CUSTOM_LLC.getByteValue());
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.GET_SORIA_METER_KEY_B.getByteValue());
						break;
					case GET_SORIA_METER_KEY_C:
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.KAIFA_CUSTOM_LLC.getByteValue());
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.GET_SORIA_METER_KEY_C.getByteValue());
						break;
					case SET_SORIA_METER_SERIAL_A:
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.KAIFA_CUSTOM_LLC.getByteValue());
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.SET_SORIA_METER_SERIAL_A.getByteValue());
						break;
					case SET_SORIA_METER_SERIAL_B:
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.KAIFA_CUSTOM_LLC.getByteValue());
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.SET_SORIA_METER_SERIAL_B.getByteValue());
						break;
					case SET_SORIA_METER_SERIAL_C:
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.KAIFA_CUSTOM_LLC.getByteValue());
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.SET_SORIA_METER_SERIAL_C.getByteValue());	
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, ((String) param.get("meterSerial")).getBytes());//append meter serial
						break;
					case SET_SORIA_METER_SERIAL_D:
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.KAIFA_CUSTOM_LLC.getByteValue());
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.SET_SORIA_METER_SERIAL_D.getByteValue());
						break;
					case SET_SORIA_METER_SERIAL_E:
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.KAIFA_CUSTOM_LLC.getByteValue());
						gdDLMSFrame = DataUtil.append(gdDLMSFrame, KaifaCustomRequest.SET_SORIA_METER_SERIAL_E.getByteValue());
						break;
						
				default:
					break;
				}
				*/
				break;
			case DISC:
				gdDLMSFrame = super.encode(hdlcType, null, null, command);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("DLMS Encoding Error - {}", e);
			gdDLMSFrame = null;
		}

		return gdDLMSFrame;
	}

	@Override
	public boolean decode(byte[] frame, Procedure procedure, String command) {
		logger.info("## Excute NestedDLMSDecorator Decoding...");
		boolean result = true;
		int pos = 0;
		int infoPos = 0;

		byte[] llc = null;
		byte[] information = null;

		try {
			//818014050200AF060200AF070400000001080400000001
			/*
			 * 81
			 * 80
			 * 14
			 * 	05
			 * 	02
			 * 	00AF
			 * 	06
			 * 	02
			 * 	00AF
			 * 	07
			 * 	04
			 * 	00000001
			 * 	08
			 * 	04
			 * 	00000001
			 */
			if (HdlcObjectType.getItem(getType()) == HdlcObjectType.UA) {
				byte[] formatIdentifier = new byte[1];
				System.arraycopy(frame, infoPos, formatIdentifier, 0, formatIdentifier.length);
				infoPos += formatIdentifier.length;
				logger.debug("[DLMS] FORMAT_IDENTIFIER = [{}]", Hex.decode(formatIdentifier));

				byte[] groupIdentifier = new byte[1];
				System.arraycopy(frame, infoPos, groupIdentifier, 0, groupIdentifier.length);
				infoPos += groupIdentifier.length;
				logger.debug("[DLMS] GROUP_IDENTIFIER = [{}]", Hex.decode(groupIdentifier));

				byte[] groupLength = new byte[1];
				System.arraycopy(frame, infoPos, groupLength, 0, groupLength.length);
				infoPos += groupLength.length;
				logger.debug("[DLMS] GROUP_LENGTH = [{}]", Hex.decode(groupLength));

				byte[] sendIdentifier = new byte[1];
				System.arraycopy(frame, infoPos, sendIdentifier, 0, sendIdentifier.length);
				infoPos += sendIdentifier.length;
				logger.debug("[DLMS] PARAM_SEND_IDENTIFIER = [{}]", Hex.decode(sendIdentifier));

				byte[] sendLength = new byte[1];
				System.arraycopy(frame, infoPos, sendLength, 0, sendLength.length);
				infoPos += sendLength.length;
				logger.debug("[DLMS] PARAM_SEND_LENGTH = [{}]", Hex.decode(sendLength));

				byte[] sendValue = new byte[2];
				System.arraycopy(frame, infoPos, sendValue, 0, sendValue.length);
				infoPos += sendValue.length;
				logger.debug("[DLMS] PARAM_SEND_VALUE = [{}]", DataUtil.getIntTo2Byte(sendValue));

				byte[] receiveIdentifier = new byte[1];
				System.arraycopy(frame, infoPos, receiveIdentifier, 0, receiveIdentifier.length);
				infoPos += receiveIdentifier.length;
				logger.debug("[DLMS] PARAM_RECEIVE_IDENTIFIER = [{}]", Hex.decode(receiveIdentifier));

				byte[] receiveLength = new byte[1];
				System.arraycopy(frame, infoPos, receiveLength, 0, receiveLength.length);
				infoPos += receiveLength.length;
				logger.debug("[DLMS] PARAM_RECEIVE_LENGTH = [{}]", Hex.decode(receiveLength));

				byte[] receiveValue = new byte[2];
				System.arraycopy(frame, infoPos, receiveValue, 0, receiveValue.length);
				infoPos += receiveValue.length;
				int sendHdlcPacketMaxSize = DataUtil.getIntTo2Byte(receiveValue);
				logger.debug("[DLMS] PARAM_RECEIVE_VALUE = [{}]", sendHdlcPacketMaxSize);
				setResultData(sendHdlcPacketMaxSize);

				byte[] sendWindowIdentifier = new byte[1];
				System.arraycopy(frame, infoPos, sendWindowIdentifier, 0, sendWindowIdentifier.length);
				infoPos += sendWindowIdentifier.length;
				logger.debug("[DLMS] PARAM_WINDOW_SEND_IDENTIFIER = [{}]", Hex.decode(sendWindowIdentifier));

				byte[] sendWindowLength = new byte[1];
				System.arraycopy(frame, infoPos, sendWindowLength, 0, sendWindowLength.length);
				infoPos += sendWindowLength.length;
				logger.debug("[DLMS] PARAM_WINDOW_SEND_LENGTH = [{}]", Hex.decode(sendWindowLength));

				byte[] sendWindowValue = new byte[4];
				System.arraycopy(frame, infoPos, sendWindowValue, 0, sendWindowValue.length);
				infoPos += sendWindowValue.length;
				logger.debug("[DLMS] PARAM_WINDOW_SEND_VALUE = [{}]", DataUtil.getIntTo4Byte(sendWindowValue));

				byte[] receiveWindowIdentifier = new byte[1];
				System.arraycopy(frame, infoPos, receiveWindowIdentifier, 0, receiveWindowIdentifier.length);
				infoPos += receiveWindowIdentifier.length;
				logger.debug("[DLMS] PARAM_WINDOW_RECEIVE_IDENTIFIER = [{}]", Hex.decode(receiveWindowIdentifier));

				byte[] receiveWindowLength = new byte[1];
				System.arraycopy(frame, infoPos, receiveWindowLength, 0, receiveWindowLength.length);
				infoPos += receiveWindowLength.length;
				logger.debug("[DLMS] PARAM_WINDOW_RECEIVE_LENGTH = [{}]", Hex.decode(receiveWindowLength));

				byte[] receiveWindowValue = new byte[4];
				System.arraycopy(frame, infoPos, receiveWindowValue, 0, receiveWindowValue.length);
				infoPos += receiveWindowValue.length;
				logger.debug("[DLMS] PARAM_WINDOW_RECEIVE_LENGTH = [{}]", DataUtil.getIntTo4Byte(receiveWindowValue));

				logger.debug("[DLMS] UA = [{}]", Hex.decode(frame));
			} else {
				llc = new byte[3];
				pos = 0;

				if(!"E6E700".equals(Hex.decode(llc)) && rawCipheringData != null) {
					information = DataUtil.append(rawCipheringData, frame);
					rawCipheringData = null;
				} else {
					System.arraycopy(frame, pos, llc, 0, llc.length);
					pos += llc.length;
					logger.debug("[DLMS] LLC = [{}]", Hex.decode(llc));

					information = new byte[frame.length - 3]; // 3 : llc
					System.arraycopy(frame, pos, information, 0, information.length);
				}
			

				/**
				 * IFrame Type 파싱
				 */
				List<HashMap<AARE, byte[]>> aareList = new ArrayList<HashMap<AARE, byte[]>>();

				infoPos = 0;
				byte[] commandType = new byte[1];
				System.arraycopy(information, infoPos, commandType, 0, commandType.length);
				infoPos += commandType.length;

				// Command 타입설정
				setType(DataUtil.getIntToByte(commandType[0]));
				setDlmsApdu(XDLMS_APDU.getItem(commandType[0]));
				logger.debug("[DLMS] COMMAND_TYPE = " + HdlcObjectType.getItem(commandType[0]).name() +" || DLMS_TYPE = " + getDlmsApdu().name());

				if (getDlmsApdu() == XDLMS_APDU.AARE) {
					/**
					 * AARE Parsing
					 */
					byte[] infoLength = new byte[1];
					System.arraycopy(information, infoPos, infoLength, 0, infoLength.length);
					infoPos += infoLength.length;

					for (int i = infoPos; i < DataUtil.getIntToByte(infoLength[0]); i = infoPos) {
						byte[] tagLength = new byte[2];
						System.arraycopy(information, infoPos, tagLength, 0, tagLength.length);
						infoPos += tagLength.length;

						AARE tag = AARE.getItem(tagLength[0]);

						// Server System Title or StoC 일 경우 한단계 더 들어감.
						if (AARE.getItem(tagLength[0]) == AARE.RESPONDING_AP_TITLE || AARE.getItem(tagLength[0]) == AARE.RESPONDING_AUTHENTICATION_VALUE || AARE.getItem(tagLength[0]) == AARE.USER_INFORMATION)  {
							tagLength = new byte[2];
							System.arraycopy(information, infoPos, tagLength, 0, tagLength.length);
							infoPos += tagLength.length;
						}

						int valueLength = DataUtil.getIntToByte(tagLength[1]);
						byte[] value = new byte[valueLength];
						System.arraycopy(information, infoPos, value, 0, value.length);
						infoPos += value.length;

						HashMap<AARE, byte[]> item = new HashMap<AARE, byte[]>();
						item.put(tag, value);
						aareList.add(item);
					}

					Iterator<HashMap<AARE, byte[]>> aareIeter = aareList.iterator();
					while (aareIeter.hasNext()) {
						HashMap<AARE, byte[]> map = aareIeter.next();
						if (map.containsKey(AARE.RESPONDING_AP_TITLE)) { // responding-AP-title
							aareRespondingAPtitle = map.get(AARE.RESPONDING_AP_TITLE);
						} else if (map.containsKey(AARE.RESPONDING_AUTHENTICATION_VALUE)) { // Authentication value
							aareAuthenticationValue = map.get(AARE.RESPONDING_AUTHENTICATION_VALUE);
						} else if (map.containsKey(AARE.RESULT)) { // association result
							aareAssociationResult =  map.get(AARE.RESULT);
						}
					}
					
					logger.debug("[DLMS] ## SERVER_SYSTEM_TITLE = [{}]", Hex.decode(aareRespondingAPtitle));
					logger.debug("[DLMS] ## S_TO_C = [{}]", Hex.decode(aareAuthenticationValue));
					if(!isProtocalHLS()) {
						logger.debug("[DLMS] ## RESULT = [{}]", Hex.decode(aareAssociationResult));
					}
					
					if(isProtocalHLS()) {
						if (aareRespondingAPtitle == null || aareRespondingAPtitle.equals("") || aareAuthenticationValue == null || aareAuthenticationValue.equals("")) {
							setResultData(false);
						} else {
							setResultData(true);
						}
					} else {
						logger.debug("aareAssociationResult["+Hex.decode(aareAssociationResult)+"] ASSOCIATION_SUCCESS_RESULT["+Hex.decode(DlmsPiece.ASSOCIATION_SUCCESS_RESULT.getBytes())+"]");
						if(!Hex.decode(aareAssociationResult).equals(Hex.decode(DlmsPiece.ASSOCIATION_SUCCESS_RESULT.getBytes()))) {
							setResultData(false);
						} else {
							setResultData(true);
						}
					}					
				} else if (getDlmsApdu() == XDLMS_APDU.ACTION_RESPONSE || getDlmsApdu() == XDLMS_APDU.GLO_ACTION_RESPONSE) {
				
					if (XDLMS_APDU.getItem(commandType[0]) == XDLMS_APDU.GLO_ACTION_RESPONSE) {
						byte[] dedLength = new byte[1];
						System.arraycopy(information, infoPos, dedLength, 0, dedLength.length);
						infoPos += dedLength.length;

						byte[] SC = new byte[1];
						System.arraycopy(information, infoPos, SC, 0, SC.length);
						infoPos += SC.length;

						byte[] IC = new byte[4];
						System.arraycopy(information, infoPos, IC, 0, IC.length);
						infoPos += IC.length;

						byte[] cipherText = new byte[DataUtil.getIntToByte(dedLength[0]) - 5]; // 6?
						System.arraycopy(information, infoPos, cipherText, 0, cipherText.length);
						infoPos += cipherText.length;

						HLSAuthForECG gloGetReqAuth = new HLSAuthForECG(HLSSecurity.getItem(SC[0]));
						byte[] plainText = gloGetReqAuth.doDecryption(IC, aareRespondingAPtitle, cipherText);

						information = plainText;
						infoPos = 0;
						
						byte[] plainTextCommandType = new byte[1];
						System.arraycopy(information, infoPos, plainTextCommandType, 0, plainTextCommandType.length);
						infoPos += plainTextCommandType.length;

						// Command 타입설정
						logger.debug("[DLMS] PLAIN_TEXT_COMMAND_TYPE = [{}]", XDLMS_APDU.getItem(plainTextCommandType[0]));			
					}
					/**
					 * Action response Parsing
					 */
					byte[] resonseType = new byte[1];
					System.arraycopy(information, infoPos, resonseType, 0, resonseType.length);
					infoPos += resonseType.length;
					logger.debug("[DLMS] ACTION-Response = [{}]", ActionResponse.getItem(resonseType[0]).name());

					switch (ActionResponse.getItem(resonseType[0])) {
					case NORMAL:
						byte[] idProperty = new byte[1];
						System.arraycopy(information, infoPos, idProperty, 0, idProperty.length);
						infoPos += idProperty.length;
						logger.debug("[DLMS] Invoke-Id-And-Priority = [{}]", Hex.decode(idProperty));

						byte[] actionResult = new byte[1];
						System.arraycopy(information, infoPos, actionResult, 0, actionResult.length);
						infoPos += actionResult.length;
						ActionResult aResult = ActionResult.getItem(actionResult[0]);
						logger.debug("[DLMS] Action-Result = [{}]", aResult.name());

						// 결과 저장
						setResultData(aResult);

						if (aResult == ActionResult.SUCCESS) { // 성공
							if (procedure == Procedure.HDLC_ASSOCIATION_LN) {
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));

								byte[] data = new byte[1];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								logger.debug("[DLMS] Data = [{}]", Hex.decode(data));

								byte[] octetString = new byte[1];
								System.arraycopy(information, infoPos, octetString, 0, octetString.length);
								infoPos += octetString.length;
								logger.debug("[DLMS] OCTET_STRING = [{}]", Hex.decode(octetString));

								byte[] length = new byte[1];
								System.arraycopy(information, infoPos, length, 0, length.length);
								infoPos += length.length;
								logger.debug("[DLMS] LENGTH = [{}]", Hex.decode(length));

								byte[] securityControlByte = new byte[1];
								System.arraycopy(information, infoPos, securityControlByte, 0, securityControlByte.length);
								infoPos += securityControlByte.length;
								logger.debug("[DLMS] SECURITY_CONTROL = [{}]", Hex.decode(securityControlByte));

								byte[] invocationCounter = new byte[4];
								System.arraycopy(information, infoPos, invocationCounter, 0, invocationCounter.length);
								infoPos += invocationCounter.length;
								logger.debug("[DLMS] INVOCATION_COUNTER = [{}]", Hex.decode(invocationCounter));

								byte[] tagValue = new byte[12];
								System.arraycopy(information, infoPos, tagValue, 0, tagValue.length);
								infoPos += tagValue.length;
								logger.debug("[DLMS] TAG_VALUE = [{}]", Hex.decode(tagValue));

								/**
								 * Validation
								 */
								HLSAuthForECG auth = new HLSAuthForECG(HLSSecurity.AUTHENTICATION, getMeterId());
								result = auth.doValidation(aareRespondingAPtitle, invocationCounter, DlmsPiece.C_TO_S.getBytes(), tagValue);
								if (!result) {
									logger.debug("[ActionResponse Validation Fail~!! but Skip.");
									logger.debug("[ActionResponse Validation Fail~!! but Skip.");
									logger.debug("[ActionResponse Validation Fail~!! but Skip.");
									logger.debug("[ActionResponse Validation Fail~!! but Skip.");

									result = true;
								}
							} else if (procedure == Procedure.ACTION_IMAGE_TRANSFER_INIT) {
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));
							} else if (procedure == Procedure.ACTION_IMAGE_BLOCK_TRANSFER) {
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));
							} else if (procedure == Procedure.ACTION_IMAGE_VERIFY) {
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));
							} else if (procedure == Procedure.ACTION_IMAGE_ACTIVATE) {
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));
							} else if (procedure == Procedure.ACTION_METER_ALARM_RESET) {
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));
							} else if ( procedure == Procedure.ACTION_DISCONNECT_CONTROL ){
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								HashMap<String, Object> r = new HashMap<String, Object>();
								r.put("status", aResult);
								r.put("value", CONTROL_STATE.getValue(DataUtil.getIntToByte(getDataResult[0])));
								setResultData(r);
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));
							} else if (procedure == Procedure.ACTION_SET_ENCRYPTION_KEY ) {
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));
							} else if (procedure == Procedure.ACTION_TRANSFER_KEY ) {
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));
							}else if (procedure == Procedure.GET_IMAGE_FIRST_NOT_TRANSFERRED_BLOCK_NUMBER ) {
								logger.warn("### HES received Inadequate DLMS Packet. This Procedure is [GET_IMAGE_FIRST_NOT_TRANSFERRED_BLOCK_NUMBER]");
								logger.warn("### HES received Inadequate DLMS Packet. This Procedure is [GET_IMAGE_FIRST_NOT_TRANSFERRED_BLOCK_NUMBER]");
								logger.warn("### HES received Inadequate DLMS Packet. This Procedure is [GET_IMAGE_FIRST_NOT_TRANSFERRED_BLOCK_NUMBER]");
							} else if(procedure == Procedure.ACTION_SET_STS_TOKEN ) {
								byte[] getDataResult = new byte[1];
								System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
								infoPos += getDataResult.length;
								logger.debug("[DLMS] Get-Data-Result = [{}]", Hex.decode(getDataResult));
							}
							else {
								logger.error("### [{}] Inadequate DLMS Packet.", procedure.name());
								logger.error("### [{}] Inadequate DLMS Packet.", procedure.name());
								logger.error("### [{}] Inadequate DLMS Packet.", procedure.name());
								result = false;
							}

						} else {
							logger.debug("[DLMS] Action-Result Fail = [{}]", aResult.name());
							//throw new Exception("[ACTION_RES] Action Result = " + Hex.decode(actionResult) + " - Fail");
						}
						break;
					case WITH_PBLOCK:
						/*
						 * 추후 필요시 구현
						 */
						break;
					case WITH_LIST:
						/*
						 * 추후 필요시 구현
						 */
						break;
					case NEXT_PBLOCK:
						/*
						 * 추후 필요시 구현
						 */
						break;
					default:
						break;
					}

					//					result = true;
				} else if (getDlmsApdu() == XDLMS_APDU.GET_RESPONSE || getDlmsApdu() == XDLMS_APDU.GLO_GET_RESPONSE) {
					if (XDLMS_APDU.getItem(commandType[0]) == XDLMS_APDU.GLO_GET_RESPONSE) {
						try {
							byte[] gloLength = new byte[1];
							System.arraycopy(information, infoPos, gloLength, 0, gloLength.length);
							infoPos += gloLength.length;
							
							//가변길이, 데이터의 길이는 7bit만 사용하기 때문에 128이 최대 크기
							//때문에 128보다 크다면 가변길이를 사용한다.
							//ex) 0x81 = 가변길이 1byte, 0x82 = 가변길이 2byte				
							if((gloLength[0] & 0x80) == 0x80) {
								int dynamicLength = DataUtil.getByteToInt((byte) (gloLength[0] & 0x0F));
								
								gloLength = new byte[dynamicLength];
								System.arraycopy(information, infoPos, gloLength, 0, gloLength.length);
								infoPos += gloLength.length;							
							}
							
							byte[] SC = new byte[1];
							System.arraycopy(information, infoPos, SC, 0, SC.length);
							infoPos += SC.length;
							
							byte[] IC = new byte[4];
							System.arraycopy(information, infoPos, IC, 0, IC.length);
							infoPos += IC.length;
							
							byte[] cipherText = new byte[DataUtil.getIntToByte(gloLength[0]) - 5]; // 6?
							System.arraycopy(information, infoPos, cipherText, 0, cipherText.length);
							infoPos += cipherText.length;
							
							HLSAuthForECG gloGetReqAuth = new HLSAuthForECG(HLSSecurity.getItem(SC[0]));
							byte[] plainText = gloGetReqAuth.doDecryption(IC, DlmsPiece.SERVER_SYSTEM_TITLE.getBytes(), cipherText);
							
							information = plainText;
							infoPos = 0;
							
							byte[] plainTextCommandType = new byte[1];
							System.arraycopy(information, infoPos, plainTextCommandType, 0, plainTextCommandType.length);
							infoPos += plainTextCommandType.length;

							// Command 타입설정
							logger.debug("[DLMS] PLAIN_TEXT_COMMAND_TYPE = [{}]", XDLMS_APDU.getItem(plainTextCommandType[0]));
							logger.debug("[DLMS] INFOAMTION DECRYPTION TEXT : "+Hex.decode(information));
						}catch(ArrayIndexOutOfBoundsException e) {
							logger.info("[DLMS] ArrayIndexOutOfBoundsException ==> HDLC FrameType Check...");
							rawCipheringData = information;
							
							Map<String, Object> tempParamMap = new HashMap<String, Object>();
							tempParamMap.put("isLast", false);
							setResultData(tempParamMap);
							
							return true;
						}
					}
					
					/**
					 * Action response Parsing
					 */
					byte[] getResponseType = new byte[1];
					System.arraycopy(information, infoPos, getResponseType, 0, getResponseType.length);
					infoPos += getResponseType.length;
					logger.debug("[DLMS] GET-Response = [{}]", GetResponse.getItem(getResponseType[0]).name());

					switch (GetResponse.getItem(getResponseType[0])) {
					case NORMAL:
						byte[] idProperty = new byte[1];
						System.arraycopy(information, infoPos, idProperty, 0, idProperty.length);
						infoPos += idProperty.length;
						logger.debug("[DLMS] invoke-id-and-priority = [{}]", Hex.decode(idProperty));

						byte[] getDataResult = new byte[1];
						System.arraycopy(information, infoPos, getDataResult, 0, getDataResult.length);
						infoPos += getDataResult.length;
						GetDataResult aResult = GetDataResult.getItem(getDataResult[0]);
						logger.debug("[DLMS] Get-Data-Result = [{}]", aResult.name());

						if (aResult == GetDataResult.DATA) {
							lpRawData = new byte[information.length - infoPos];
							System.arraycopy(information, infoPos, lpRawData, 0, lpRawData.length);
							
							byte[] getDataResultType = new byte[1];
							System.arraycopy(information, infoPos, getDataResultType, 0, getDataResultType.length);
							infoPos += getDataResultType.length;

							logger.debug("[DLMS] Get-Data-Result-type = [{}]", DLMSCommonDataType.getItem(getDataResultType[0]).name());
							byte[] data = null;
							switch (DLMSCommonDataType.getItem(getDataResultType[0])) {
							case Boolean:
								data = new byte[DLMSCommonDataType.Boolean.getLenth()];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								boolean getResult = Boolean.valueOf(String.valueOf(DlmsConstantsForECG.getValueByDLMSCommonDataType(DLMSCommonDataType.Boolean, data)));
								setResultData(getResult);

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", getResult);
								break;
							case FLOAT32:
								data = new byte[DLMSCommonDataType.FLOAT32.getLenth()];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								float floatResult = Float.parseFloat(String.valueOf(DlmsConstantsForECG.getValueByDLMSCommonDataType(DLMSCommonDataType.FLOAT32, data)));
								setResultData(floatResult);

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", floatResult);

								break;	
							case UINT8:
								data = new byte[DLMSCommonDataType.UINT8.getLenth()];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								long uint8Result = Long.parseLong(String.valueOf(DlmsConstantsForECG.getValueByDLMSCommonDataType(DLMSCommonDataType.UINT8, data)));
								setResultData(uint8Result);

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", uint8Result);

								break;
							case INT8:
								data = new byte[DLMSCommonDataType.INT8.getLenth()];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								long int8Result = Long.parseLong(String.valueOf(DlmsConstantsForECG.getValueByDLMSCommonDataType(DLMSCommonDataType.INT8, data)));
								setResultData(int8Result);

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", int8Result);

								break;	
							case UINT16:
								data = new byte[DLMSCommonDataType.UINT16.getLenth()];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								long uint16Result = Long.parseLong(String.valueOf(DlmsConstantsForECG.getValueByDLMSCommonDataType(DLMSCommonDataType.UINT16, data)));
								setResultData(uint16Result);

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", uint16Result);
								break;
							case INT16:
								data = new byte[DLMSCommonDataType.INT16.getLenth()];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								long int16Result = Long.parseLong(String.valueOf(DlmsConstantsForECG.getValueByDLMSCommonDataType(DLMSCommonDataType.INT16, data)));
								setResultData(int16Result);

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", int16Result);
								break;								
							case UINT32:
								data = new byte[DLMSCommonDataType.UINT32.getLenth()];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								long longResult = Long.parseLong(String.valueOf(DlmsConstantsForECG.getValueByDLMSCommonDataType(DLMSCommonDataType.UINT32, data)));
								setResultData(longResult);

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", longResult);
								break;
							case INT32:
								data = new byte[DLMSCommonDataType.INT32.getLenth()];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								long int32Result = Long.parseLong(String.valueOf(DlmsConstantsForECG.getValueByDLMSCommonDataType(DLMSCommonDataType.INT32, data)));
								setResultData(int32Result);

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", int32Result);
								break;								
							case Enum:
								data = new byte[DLMSCommonDataType.Enum.getLenth()];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;

								setResultData(DataUtil.getIntToBytes(data));

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", DataUtil.getIntToBytes(data));
								break;
							case OctetString:
								if (procedure == Procedure.GET_METER_TIME) {
									byte[] octetLength = new byte[1];
									System.arraycopy(information, infoPos, octetLength, 0, octetLength.length);
									infoPos += octetLength.length;

									byte[] year = new byte[2];
									System.arraycopy(information, infoPos, year, 0, year.length);
									infoPos += year.length;

									byte[] month = new byte[1];
									System.arraycopy(information, infoPos, month, 0, month.length);
									infoPos += month.length;

									byte[] dayOfMonth = new byte[1];
									System.arraycopy(information, infoPos, dayOfMonth, 0, dayOfMonth.length);
									infoPos += dayOfMonth.length;

									byte[] dayOfWeek = new byte[1];
									System.arraycopy(information, infoPos, dayOfWeek, 0, dayOfWeek.length);
									infoPos += dayOfWeek.length;

									byte[] hour = new byte[1];
									System.arraycopy(information, infoPos, hour, 0, hour.length);
									infoPos += hour.length;

									byte[] minute = new byte[1];
									System.arraycopy(information, infoPos, minute, 0, minute.length);
									infoPos += minute.length;

									byte[] second = new byte[1];
									System.arraycopy(information, infoPos, second, 0, second.length);
									infoPos += second.length;

									byte[] hundredthsOfSecond = new byte[1];
									System.arraycopy(information, infoPos, hundredthsOfSecond, 0, hundredthsOfSecond.length);
									infoPos += hundredthsOfSecond.length;

									byte[] deviation = new byte[2];
									System.arraycopy(information, infoPos, deviation, 0, deviation.length);
									infoPos += deviation.length;

									byte[] clockStatus = new byte[1];
									System.arraycopy(information, infoPos, clockStatus, 0, clockStatus.length);
									infoPos += clockStatus.length;

									String dayOfWeekStr = "";
									switch (dayOfWeek[0]) {
									case 1:
										dayOfWeekStr = "Mon";
										break;
									case 2:
										dayOfWeekStr = "The";
										break;
									case 3:
										dayOfWeekStr = "Wed";
										break;
									case 4:
										dayOfWeekStr = "Thu";
										break;
									case 5:
										dayOfWeekStr = "Fri";
										break;
									case 6:
										dayOfWeekStr = "Sat";
										break;
									case 7:
										dayOfWeekStr = "Sun";
										break;
									default:
										break;
									}
									
									String daylight=null;
									if((clockStatus[0] & 0x80) == 0x80) {
										daylight = "true";
									} else if((clockStatus[0] & 0x80) == 0x00) {
										daylight = "false";
									}

									String date = String.format("%04d", DataUtil.getIntTo2Byte(year)) + "/" + String.format("%02d", DataUtil.getIntToByte(month[0])) + "/" + String.format("%02d", DataUtil.getIntToByte(dayOfMonth[0])) + "(" + dayOfWeekStr + ")";
									String time = String.format("%02d", DataUtil.getIntToByte(hour[0])) + ":" + String.format("%02d", DataUtil.getIntToByte(minute[0])) + ":" + String.format("%02d", DataUtil.getIntToByte(second[0]));
									HashMap<String, String> r = new HashMap<String, String>();
									r.put("date", date);
									r.put("time", time);
									r.put("daylight", daylight);

									setResultData(r);

									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", r.toString());
								} else if(procedure == Procedure.GET_STS_FRIENDLY_TIME) {
									byte[] friendlytimeLength = new byte[1];
									System.arraycopy(information, infoPos, friendlytimeLength, 0,friendlytimeLength.length);
									infoPos += friendlytimeLength.length;
									
									byte[] startTime = new byte[2];
									System.arraycopy(information, infoPos, startTime, 0,startTime.length);
									infoPos += startTime.length;
									
									byte[] endTime = new byte[2];
									System.arraycopy(information, infoPos, endTime, 0,endTime.length);
									infoPos += endTime.length;
									
									String mStringTime = String.format("%02d", DataUtil.getIntToByte(startTime[0])) + ":" + String.format("%02d", DataUtil.getIntToByte(startTime[1]));
									String mEndTime = String.format("%02d", DataUtil.getIntToByte(endTime[0])) + ":" + String.format("%02d", DataUtil.getIntToByte(endTime[1]));
									String time = mStringTime + " ~ " + mEndTime;
									
									setResultData(time);
								} else {
									byte[] octetLength = new byte[1];
									System.arraycopy(information, infoPos, octetLength, 0, octetLength.length);
									infoPos += octetLength.length;

									data = new byte[DataUtil.getIntToByte(octetLength[0])];
									System.arraycopy(information, infoPos, data, 0, data.length);
									infoPos += data.length;
									//setResultData(DataUtil.getString(data));
									//logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", DataUtil.getString(data));
									String dataStr = DataUtil.getString(data);
									if ( dataStr.matches("\\p{Print}*") ){
										setResultData(dataStr);
										logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", dataStr);
									}
									else {
										setResultData(Hex.decode(data));
										logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", Hex.decode(data));
									}
								}
							case Array:
								/*
								 *  array image_to_activate_info_element
									image_to_activate_info_element ::= structure
									{
										image_to_activate_size: double-long-unsigned,
										image_to_activate_identification: octet-string,
										image_to_activate_signature: octet-string
									} 
								 */
								if (procedure == Procedure.GET_IMAGE_TO_ACTIVATE_INFO) {
									byte[] arrayLength = new byte[1];
									System.arraycopy(information, infoPos, arrayLength, 0, arrayLength.length);
									infoPos += arrayLength.length;

									byte[] structure = new byte[1];
									System.arraycopy(information, infoPos, structure, 0, structure.length);
									infoPos += structure.length;

									byte[] structureLength = new byte[1];
									System.arraycopy(information, infoPos, structureLength, 0, structureLength.length);
									infoPos += structureLength.length;

									byte[] eltype = new byte[1];
									System.arraycopy(information, infoPos, eltype, 0, eltype.length);
									infoPos += eltype.length;

									byte[] image_to_activate_sizeData = new byte[DLMSCommonDataType.UINT32.getLenth()];
									System.arraycopy(information, infoPos, image_to_activate_sizeData, 0, image_to_activate_sizeData.length);
									infoPos += image_to_activate_sizeData.length;
									long image_to_activate_size = Long.parseLong(String.valueOf(DlmsConstantsForECG.getValueByDLMSCommonDataType(DLMSCommonDataType.UINT32, image_to_activate_sizeData)));
									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", image_to_activate_size);

									byte[] eltype2 = new byte[1];
									System.arraycopy(information, infoPos, eltype2, 0, eltype2.length);
									infoPos += eltype2.length;

									byte[] eltype2Length = new byte[1];
									System.arraycopy(information, infoPos, eltype2Length, 0, eltype2Length.length);
									infoPos += eltype2Length.length;

									byte[] image_to_activate_identificationData = new byte[DataUtil.getIntToByte(eltype2Length[0])];
									System.arraycopy(information, infoPos, image_to_activate_identificationData, 0, image_to_activate_identificationData.length);
									infoPos += image_to_activate_identificationData.length;
									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", Hex.decode(image_to_activate_identificationData));

									byte[] eltype3 = new byte[1];
									System.arraycopy(information, infoPos, eltype3, 0, eltype3.length);
									infoPos += eltype3.length;

									byte[] eltype3Length = new byte[1];
									System.arraycopy(information, infoPos, eltype3Length, 0, eltype3Length.length);
									infoPos += eltype3Length.length;

									HashMap<String, Object> resultDataMap = new HashMap<String, Object>();
									resultDataMap.put("image_to_activate_size", image_to_activate_size);
									resultDataMap.put("image_to_activate_identification", image_to_activate_identificationData);

									// image_to_activate_signature 가 없는경우도 있는것 같음.
									if (0 < DataUtil.getIntToByte(eltype3Length[0])) {
										byte[] image_to_activate_signatureData = new byte[DataUtil.getIntToByte(eltype3Length[0])];
										System.arraycopy(information, infoPos, image_to_activate_signatureData, 0, image_to_activate_signatureData.length);
										infoPos += image_to_activate_signatureData.length;
										logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", Hex.decode(image_to_activate_signatureData));

										resultDataMap.put("image_to_activate_signature", image_to_activate_signatureData);
									}

									setResultData(resultDataMap);
								} else if (procedure == Procedure.GET_BILLING_CYCLE) {
									byte[] arrayLength = new byte[1];
									System.arraycopy(information, infoPos, arrayLength, 0, arrayLength.length);
									infoPos += arrayLength.length;

									byte[] structure = new byte[1];
									System.arraycopy(information, infoPos, structure, 0, structure.length);
									infoPos += structure.length;

									byte[] structureLength = new byte[1];
									System.arraycopy(information, infoPos, structureLength, 0, structureLength.length);
									infoPos += structureLength.length;

									byte[] dataResultType1 = new byte[1];
									System.arraycopy(information, infoPos, dataResultType1, 0, dataResultType1.length);
									infoPos += dataResultType1.length;

									byte[] oLength1 = new byte[1];
									System.arraycopy(information, infoPos, oLength1, 0, oLength1.length);
									infoPos += oLength1.length;

									byte[] time = new byte[DataUtil.getIntToByte(oLength1[0])];
									System.arraycopy(information, infoPos, time, 0, time.length);
									infoPos += time.length;

									byte[] dataResultType2 = new byte[1];
									System.arraycopy(information, infoPos, dataResultType2, 0, dataResultType2.length);
									infoPos += dataResultType2.length;

									byte[] oLength2 = new byte[1];
									System.arraycopy(information, infoPos, oLength2, 0, oLength2.length);
									infoPos += oLength2.length;

									byte[] day = new byte[DataUtil.getIntToByte(oLength2[0])];
									System.arraycopy(information, infoPos, day, 0, day.length);
									infoPos += day.length;

									String times = String.format("%02d", DataUtil.getIntToByte(time[0])) + ":" + String.format("%02d", DataUtil.getIntToByte(time[1])) + ":" + String.format("%02d", DataUtil.getIntToByte(time[2]));
									int days = DataUtil.getIntToByte(day[3]);

									HashMap<String, String> r = new HashMap<String, String>();
									r.put("time", times);
									r.put("day", String.format("%02d", days));

									setResultData(r);

									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", r.toString());
								} else if (procedure == Procedure.GET_PROFILE_OBJECT) {
									List<HashMap<String, Object>> returnList = new ArrayList<HashMap<String, Object>>();
									channelList = new ArrayList<HashMap<String, Object>>();

									byte[] arraySize = new byte[1];
									System.arraycopy(information, infoPos, arraySize, 0, arraySize.length);
									infoPos += arraySize.length;

									logger.debug("[DLMS] Get-Data-Result-type-size = [{}]", DataUtil.getIntToBytes(arraySize));

									for (int i = 0; i < DataUtil.getIntToBytes(arraySize); i++) {
										byte[] structure = new byte[1];
										System.arraycopy(information, infoPos, structure, 0, structure.length);
										infoPos += structure.length;

										byte[] structureSize = new byte[1];
										System.arraycopy(information, infoPos, structureSize, 0, structureSize.length);
										infoPos += structureSize.length;

										byte[] classSize = new byte[1];
										System.arraycopy(information, infoPos, classSize, 0, classSize.length);
										infoPos += classSize.length;

										byte[] classId = new byte[2];
										System.arraycopy(information, infoPos, classId, 0, classId.length);
										infoPos += classId.length;

										byte[] obisType = new byte[1];
										System.arraycopy(information, infoPos, obisType, 0, obisType.length);
										infoPos += obisType.length;

										byte[] obisSize = new byte[1];
										System.arraycopy(information, infoPos, obisSize, 0, obisSize.length);
										infoPos += obisSize.length;

										byte[] obisCode = new byte[6];
										System.arraycopy(information, infoPos, obisCode, 0, obisCode.length);
										infoPos += obisCode.length;

										byte[] attributeType = new byte[1];
										System.arraycopy(information, infoPos, attributeType, 0, attributeType.length);
										infoPos += attributeType.length;

										byte[] attribute = new byte[1];
										System.arraycopy(information, infoPos, attribute, 0, attribute.length);
										infoPos += attribute.length;

										byte[] longType = new byte[1];
										System.arraycopy(information, infoPos, longType, 0, longType.length);
										infoPos += longType.length;

										byte[] longData = new byte[2];
										System.arraycopy(information, infoPos, longData, 0, longData.length);
										infoPos += longData.length;

										HashMap<String, Object> map = new HashMap<String, Object>();
										map.put("classId", DataUtil.getIntTo2Byte(classId));
										map.put("obisCode", Hex.decode(obisCode));
										map.put("attribute", DataUtil.getIntToByte(attribute[0]));
										map.put("longData", longData);
										returnList.add(map);
									}

									channelList = returnList;
									setResultData(returnList);

									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", returnList.toString());
								} else if (procedure == Procedure.GET_PROFILE_BUFFER) {
									
									logger.debug("###################  GET_PROFILE_BUFFER - NORMAL ###############");


									HashMap<String, Object> tempParamMap = new HashMap<String, Object>();
									tempParamMap.put("isBlock", false);
									tempParamMap.put("isLast", true);
									tempParamMap.put("rawData", lpRawData);
									
									setResultData(tempParamMap);
								} else if (procedure == Procedure.GET_CREDIT_CHARGE_EVENT_LOG) {
									
									logger.debug("###################  GET_CREDIT_CHARGE_EVENT_LOG - NORMAL ###############");


									HashMap<String, Object> tempParamMap = new HashMap<String, Object>();
									tempParamMap.put("isBlock", false);
									tempParamMap.put("isLast", true);
									tempParamMap.put("rawData", lpRawData);
									
									setResultData(tempParamMap);
								} else if(procedure == Procedure.GET_STS_FRIENDLY_DAYS_TABLE) {
									logger.debug("###################  GET_STS_DAYS_TABLE - NORMAL ###############");
									List<HashMap<String, Object>> returnList = new ArrayList<HashMap<String, Object>>();
									
									byte[] arraySize = new byte[1];
									System.arraycopy(information, infoPos, arraySize, 0, arraySize.length);
									infoPos += arraySize.length;
									
									logger.debug("[DLMS] Get-Data-Result-type-size = [{}]", DataUtil.getIntToBytes(arraySize));
									
									for (int i = 0; i < DataUtil.getIntToBytes(arraySize); i++) {
										byte[] structure = new byte[1];
										System.arraycopy(information, infoPos, structure, 0, structure.length);
										infoPos += structure.length;
										
										byte[] structureSize = new byte[1];
										System.arraycopy(information, infoPos, structureSize, 0, structureSize.length);
										infoPos += structureSize.length;
										
										byte[] indexType = new byte[1];
										System.arraycopy(information, infoPos, indexType, 0, indexType.length);
										infoPos += indexType.length;
										
										byte[] indexValue = new byte[2];
										System.arraycopy(information, infoPos, indexValue, 0, indexValue.length);
										infoPos += indexValue.length;
										logger.debug("[DLMS] Get-Friendly-Index = [{}]", DataUtil.getIntToBytes(indexValue));
										
										byte[] friendlyType = new byte[1];
										System.arraycopy(information, infoPos, friendlyType, 0, friendlyType.length);
										infoPos += friendlyType.length;
										
										byte[] friendlySize = new byte[1];
										System.arraycopy(information, infoPos, friendlySize, 0, friendlySize.length);
										infoPos += friendlySize.length;
										
										byte[] friendlyValue = new byte[DataUtil.getIntToBytes(friendlySize)];
										System.arraycopy(information, infoPos, friendlyValue, 0, friendlyValue.length);
										infoPos += friendlyValue.length;
										
										byte[] dayIdType = new byte[1];
										System.arraycopy(information, infoPos, dayIdType, 0, dayIdType.length);
										infoPos += dayIdType.length;
										
										byte[] dayIdValue = new byte[1];
										System.arraycopy(information, infoPos, dayIdValue, 0, dayIdValue.length);
										infoPos += dayIdValue.length;
										
										//////////////////////////////////////////////////////////////////////////////
										int infoCur = 0;
										
										byte[] year = new byte[2];
										System.arraycopy(friendlyValue, infoCur, year, 0, year.length);
										infoCur += year.length;

										byte[] month = new byte[1];
										System.arraycopy(friendlyValue, infoCur, month, 0, month.length);
										infoCur += month.length;

										byte[] dayOfMonth = new byte[1];
										System.arraycopy(friendlyValue, infoCur, dayOfMonth, 0, dayOfMonth.length);
										infoCur += dayOfMonth.length;
										
										String date = String.format("%04d", DataUtil.getIntTo2Byte(year)) + "/" + String.format("%02d", DataUtil.getIntToByte(month[0])) + "/" + String.format("%02d", DataUtil.getIntToByte(dayOfMonth[0]));
										
										HashMap<String, Object> map = new HashMap<String, Object>();
										//map.put("index", DataUtil.getIntTo2Byte(indexValue));
										map.put("friendlyDay", date);
										//map.put("dayId", DataUtil.getIntToByte(dayIdValue[0]));
										returnList.add(map);
									}
									
									setResultData(returnList);
									
									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", returnList.toString());
								}
							case Structure:
								if (procedure == Procedure.GET_REGISTER_UNIT) {
									byte[] arrayLength = new byte[1];
									System.arraycopy(information, infoPos, arrayLength, 0, arrayLength.length);
									infoPos += arrayLength.length;
									
									byte[] integerLen = new byte[1];
									System.arraycopy(information, infoPos, integerLen, 0, integerLen.length);
									infoPos += integerLen.length;

									byte[] scaler = new byte[DLMSCommonDataType.INT8.getLenth()];
									System.arraycopy(information, infoPos, scaler, 0, scaler.length);
									infoPos += scaler.length;

									byte[] enumLen = new byte[1];
									System.arraycopy(information, infoPos, enumLen, 0, enumLen.length);
									infoPos += enumLen.length;

									byte[] unit = new byte[DLMSCommonDataType.Enum.getLenth()];
									System.arraycopy(information, infoPos, unit, 0, unit.length);
									infoPos += unit.length;

									HashMap<String, Object> r = new HashMap<String, Object>();
									r.put("scaler", DataUtil.getIntToByte(scaler[0]));
									r.put("unit", DataUtil.getIntToByte(unit[0]));

									setResultData(r);

									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", r.toString());
								}
								break;
							case BitString:
								if (procedure == Procedure.GET_STS_FRIENDLY_WEEK) {
									byte[] weekLength = new byte[1]; 
									System.arraycopy(information, infoPos, weekLength, 0, weekLength.length);
									infoPos += weekLength.length;
									
									byte[] weekData = new byte[1];
									System.arraycopy(information, infoPos, weekData, 0, weekData.length);
									infoPos += weekData.length;
									
									StringBuffer buffer = new StringBuffer();
									String bitString = DataUtil.getBit(weekData[0]);
									
									for(int i=0; i<bitString.length(); i++) {
										if(bitString.charAt(i) == '1') {
											if(buffer.length() > 0) {
												buffer.append(",");
											}
											
											switch(i) {
											case 1:
												buffer.append("Saturday");
												break;
											case 2:
												buffer.append("Friday");
												break;
											case 3:
												buffer.append("Thursday");
												break;
											case 4:
												buffer.append("Wednesday");
												break;
											case 5:
												buffer.append("Tues");
												break;
											case 6:
												buffer.append("Monday");
												break;
											case 7:
												buffer.append("Sunday");
												break;
											default:
												break;
											}
										}
									}
									
									setResultData(buffer.toString());

									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", buffer.toString());
									
								} else {
									byte[] bitLength = new byte[1]; 
									System.arraycopy(information, infoPos, bitLength, 0, bitLength.length);
									infoPos += bitLength.length;
									
									int dataSize = (DataUtil.getIntToByte(bitLength[0]) / 8);
									
									data = new byte[dataSize];
									System.arraycopy(information, infoPos, data, 0, data.length);
									infoPos += data.length;
									
									setResultData(data);
									
									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", Hex.decode(data));
								}
								
								break;
							case VisibleString:
								byte[] visibleLength = new byte[1];
								System.arraycopy(information, infoPos, visibleLength, 0, visibleLength.length);
								infoPos += visibleLength.length;

								data = new byte[DataUtil.getIntToByte(visibleLength[0])];
								System.arraycopy(information, infoPos, data, 0, data.length);
								infoPos += data.length;
								//setResultData(DataUtil.getString(data));
								//logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", DataUtil.getString(data));
								String dataStr = DataUtil.getString(data);
								if ( dataStr.matches("\\p{Print}*") ){
									setResultData(dataStr);
									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", dataStr);
								}
								else {
									setResultData(Hex.decode(data));
									logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", Hex.decode(data));
								}
							case Datetime:								
								byte[] year = new byte[2];
								System.arraycopy(information, infoPos, year, 0, year.length);
								infoPos += year.length;

								byte[] month = new byte[1];
								System.arraycopy(information, infoPos, month, 0, month.length);
								infoPos += month.length;

								byte[] dayOfMonth = new byte[1];
								System.arraycopy(information, infoPos, dayOfMonth, 0, dayOfMonth.length);
								infoPos += dayOfMonth.length;

								byte[] dayOfWeek = new byte[1];
								System.arraycopy(information, infoPos, dayOfWeek, 0, dayOfWeek.length);
								infoPos += dayOfWeek.length;

								byte[] hour = new byte[1];
								System.arraycopy(information, infoPos, hour, 0, hour.length);
								infoPos += hour.length;

								byte[] minute = new byte[1];
								System.arraycopy(information, infoPos, minute, 0, minute.length);
								infoPos += minute.length;

								byte[] second = new byte[1];
								System.arraycopy(information, infoPos, second, 0, second.length);
								infoPos += second.length;

								byte[] hundredthsOfSecond = new byte[1];
								System.arraycopy(information, infoPos, hundredthsOfSecond, 0, hundredthsOfSecond.length);
								infoPos += hundredthsOfSecond.length;

								byte[] deviation = new byte[2];
								System.arraycopy(information, infoPos, deviation, 0, deviation.length);
								infoPos += deviation.length;

								byte[] clockStatus = new byte[1];
								System.arraycopy(information, infoPos, clockStatus, 0, clockStatus.length);
								infoPos += clockStatus.length;

								String dayOfWeekStr = "";
								switch (dayOfWeek[0]) {
								case 1:
									dayOfWeekStr = "Mon";
									break;
								case 2:
									dayOfWeekStr = "The";
									break;
								case 3:
									dayOfWeekStr = "Wed";
									break;
								case 4:
									dayOfWeekStr = "Thu";
									break;
								case 5:
									dayOfWeekStr = "Fri";
									break;
								case 6:
									dayOfWeekStr = "Sat";
									break;
								case 7:
									dayOfWeekStr = "Sun";
									break;
								default:
									break;
								}
								
								String daylight=null;
								if((clockStatus[0] & 0x80) == 0x80) {
									daylight = "true";
								} else if((clockStatus[0] & 0x80) == 0x00) {
									daylight = "false";
								}

								String date = String.format("%04d", DataUtil.getIntTo2Byte(year)) + "/" + String.format("%02d", DataUtil.getIntToByte(month[0])) + "/" + String.format("%02d", DataUtil.getIntToByte(dayOfMonth[0])) + "(" + dayOfWeekStr + ")";
								String time = String.format("%02d", DataUtil.getIntToByte(hour[0])) + ":" + String.format("%02d", DataUtil.getIntToByte(minute[0])) + ":" + String.format("%02d", DataUtil.getIntToByte(second[0]));
								String datetime = String.format("%04d", DataUtil.getIntTo2Byte(year)) + String.format("%02d", DataUtil.getIntToByte(month[0])) + String.format("%02d", DataUtil.getIntToByte(dayOfMonth[0])) + String.format("%02d", DataUtil.getIntToByte(hour[0])) + String.format("%02d", DataUtil.getIntToByte(minute[0])) + String.format("%02d", DataUtil.getIntToByte(second[0])); 
								HashMap<String, String> r = new HashMap<String, String>();
								r.put("date", date);
								r.put("time", time);
								r.put("daylight", daylight);
								r.put("datetime", datetime);

								setResultData(r);

								logger.debug("[DLMS] Get-Data-Result-type-data = [{}]", r.toString());
										
								break;
							default:
								break;
							}

						} else if (aResult == GetDataResult.DATA_ACCESS_RESULT) {
							byte[] getDataAccessResult = new byte[1];
							System.arraycopy(information, infoPos, getDataAccessResult, 0, getDataAccessResult.length);
							infoPos += getDataAccessResult.length;
							setResultData(DataAccessResult.getItem(getDataAccessResult[0]));
							logger.debug("[DLMS] Get-Data-Access-Result = [{}]", DataAccessResult.getItem(getDataAccessResult[0]).name());
						}

						break;
					case WITH_DATABLOCK:
						/*
						 * 추후 필요시 구현
						 */
						byte[] idPropertyB = new byte[1];
						System.arraycopy(information, infoPos, idPropertyB, 0, idPropertyB.length);
						infoPos += idPropertyB.length;
						logger.debug("[DLMS] invoke-id-and-priority = [{}]", Hex.decode(idPropertyB));

						byte[] lastBlock = new byte[1];
						System.arraycopy(information, infoPos, lastBlock, 0, lastBlock.length);
						infoPos += lastBlock.length;
						logger.debug("[DLMS] last-block = [{}]", lastBlock[0] == 0 ? "FALSE" : "TRUE");

						byte[] blockNumber = new byte[4];
						System.arraycopy(information, infoPos, blockNumber, 0, blockNumber.length);
						infoPos += blockNumber.length;
						logger.debug("[DLMS] block-number = [{}]", DataUtil.getIntTo4Byte(blockNumber));

						byte[] getResultChoice = new byte[1];
						System.arraycopy(information, infoPos, getResultChoice, 0, getResultChoice.length);
						infoPos += getResultChoice.length;
						aResult = GetDataResult.getItem(getResultChoice[0]);
						logger.debug("[DLMS] Get-Data-Result = [{}]", aResult.name());

						if (aResult == GetDataResult.DATA) {
							/*
							 * Block Length 구하기 : 사실 이 로직에서 길이는 별의미가 없음 단지 infoPos를 늘리기위함.
							 */
							byte[] length = new byte[1];
							System.arraycopy(information, infoPos, length, 0, length.length);
							infoPos += length.length;

							byte[] byteLength = null;
							if ((length[0] & 0x80) == 0x80) {
								byteLength = new byte[(length[0] & 0x7F)];
								System.arraycopy(information, infoPos, byteLength, 0, byteLength.length);
								infoPos += byteLength.length;
								logger.debug("[DLMS] this block byte Length= [{}]", Hex.decode(byteLength));

								length = byteLength;
							}

							// 
							lpRawData = new byte[information.length - infoPos];
							System.arraycopy(information, infoPos, lpRawData, 0, lpRawData.length);

							//List<HashMap<String, Object>> returnList = new ArrayList<HashMap<String, Object>>();
							Map<String, Object> tempParamMap = new HashMap<String, Object>();
							tempParamMap.put("isBlock", true);
							tempParamMap.put("blockNumber", DataUtil.getIntTo4Byte(blockNumber));
							tempParamMap.put("isLast", lastBlock[0] == 0 ? false : true);
							//tempParamMap.put("dataSize", dataList == null ? 0 : dataList.size());							
							tempParamMap.put("rawData", lpRawData);

							setResultData(tempParamMap);
							logger.debug("[DLMS] Get-Data-Result-data-info = [{}]", tempParamMap.toString());

							
							byte[] getDataResultType = new byte[1];
							System.arraycopy(information, infoPos, getDataResultType, 0, getDataResultType.length);
							infoPos += getDataResultType.length;

							logger.debug("### GET-RESPONSE-WITH-DATABLOCK - RAW Data 저장 ###");
							
						} else if (aResult == GetDataResult.DATA_ACCESS_RESULT) {
							byte[] getDataAccessResult = new byte[1];
							System.arraycopy(information, infoPos, getDataAccessResult, 0, getDataAccessResult.length);
							infoPos += getDataAccessResult.length;
							setResultData(DataAccessResult.getItem(getDataAccessResult[0]));
							logger.debug("[DLMS] Get-Data-Access-Result = [{}]", DataAccessResult.getItem(getDataAccessResult[0]).name());
						}

						break;
					case WITH_LIST:
						/*
						 * 추후 필요시 구현
						 */
						break;
					default:
						break;
					}

					//					result = true;
				} else if (getDlmsApdu() == XDLMS_APDU.SET_RESPONSE || getDlmsApdu() == XDLMS_APDU.GLO_SET_RESPONSE) {
					if (XDLMS_APDU.getItem(commandType[0]) == XDLMS_APDU.GLO_SET_RESPONSE) {
						byte[] gloLength = new byte[1];
						System.arraycopy(information, infoPos, gloLength, 0, gloLength.length);
						infoPos += gloLength.length;
						
						//가변길이, 데이터의 길이는 7bit만 사용하기 때문에 128이 최대 크기
						//때문에 128보다 크다면 가변길이를 사용한다.
						//ex) 0x81 = 가변길이 1byte, 0x82 = 가변길이 2byte				
						if((gloLength[0] & 0x80) == 0x80) {
							int dynamicLength = DataUtil.getByteToInt((byte) (gloLength[0] & 0x0F));
							
							gloLength = new byte[dynamicLength];
							System.arraycopy(information, infoPos, gloLength, 0, gloLength.length);
							infoPos += gloLength.length;
						}
						
						byte[] SC = new byte[1];
						System.arraycopy(information, infoPos, SC, 0, SC.length);
						infoPos += SC.length;
						
						byte[] IC = new byte[4];
						System.arraycopy(information, infoPos, IC, 0, IC.length);
						infoPos += IC.length;
						
						byte[] cipherText = new byte[DataUtil.getIntToByte(gloLength[0]) - 5]; // 6?
						System.arraycopy(information, infoPos, cipherText, 0, cipherText.length);
						infoPos += cipherText.length;
						
						HLSAuthForECG gloGetReqAuth = new HLSAuthForECG(HLSSecurity.getItem(SC[0]));
						byte[] plainText = gloGetReqAuth.doDecryption(IC, DlmsPiece.SERVER_SYSTEM_TITLE.getBytes(), cipherText);
						
						information = plainText;
						infoPos = 0;
						
						byte[] plainTextCommandType = new byte[1];
						System.arraycopy(information, infoPos, plainTextCommandType, 0, plainTextCommandType.length);
						infoPos += plainTextCommandType.length;

						// Command 타입설정
						logger.debug("[DLMS] PLAIN_TEXT_COMMAND_TYPE = [{}]", XDLMS_APDU.getItem(plainTextCommandType[0]));		
					}
					/**
					 * Set response Parsing
					 */
					byte[] resonseType = new byte[1];
					System.arraycopy(information, infoPos, resonseType, 0, resonseType.length);
					infoPos += resonseType.length;
					logger.debug("[DLMS] Set-Response = [{}]", SetResponse.getItem(resonseType[0]).name());

					byte[] idProperty;
					byte[] dataAccessResult;
					DataAccessResult aResult;
					byte[] blockNumber;
					switch (SetResponse.getItem(resonseType[0])) {
					case NORMAL:
						idProperty = new byte[1];
						System.arraycopy(information, infoPos, idProperty, 0, idProperty.length);
						infoPos += idProperty.length;
						logger.debug("[DLMS] Invoke-Id-And-Priority = [{}]", Hex.decode(idProperty));

						dataAccessResult = new byte[1];
						System.arraycopy(information, infoPos, dataAccessResult, 0, dataAccessResult.length);
						infoPos += dataAccessResult.length;
						aResult = DataAccessResult.getItem(dataAccessResult[0]);

						// 결과 저장
						setResultData(aResult);
						logger.debug("[DLMS] Get-Data-Access-Result = [{}]", aResult.name());

						if (aResult != DataAccessResult.SUCCESS) { // 성공이 아닌것.
							result = false;
						}
						break;
					case DATABLOCK:
						idProperty = new byte[1];
						System.arraycopy(information, infoPos, idProperty, 0, idProperty.length);
						infoPos += idProperty.length;
						logger.debug("[DLMS] Invoke-Id-And-Priority = [{}]", Hex.decode(idProperty));

						blockNumber = new byte[4];
						System.arraycopy(information, infoPos, blockNumber, 0, blockNumber.length);
						infoPos += blockNumber.length;
						logger.debug("[DLMS] Block Number = [{}]", DataUtil.getIntTo4Byte(blockNumber));

						// 결과 저장
						setResultData(DataUtil.getIntTo4Byte(blockNumber));
						break;
					case LAST_DATABLOCK:
						idProperty = new byte[1];
						System.arraycopy(information, infoPos, idProperty, 0, idProperty.length);
						infoPos += idProperty.length;
						logger.debug("[DLMS] Invoke-Id-And-Priority = [{}]", Hex.decode(idProperty));

						dataAccessResult = new byte[1];
						System.arraycopy(information, infoPos, dataAccessResult, 0, dataAccessResult.length);
						infoPos += dataAccessResult.length;
						aResult = DataAccessResult.getItem(dataAccessResult[0]);

						// 결과 저장
						setResultData(aResult);
						logger.debug("[DLMS] Get-Data-Access-Result = [{}]", aResult.name());

						if (aResult != DataAccessResult.SUCCESS) { // 성공이 아닌것.
							result = false;
						}

						blockNumber = new byte[4];
						System.arraycopy(information, infoPos, blockNumber, 0, blockNumber.length);
						infoPos += blockNumber.length;
						logger.debug("[DLMS] Block Number = [{}]", DataUtil.getIntTo4Byte(blockNumber));
						break;
					case LAST_DATABLOCK_WITH_LIST:
						/*
						 * 추후 필요시 구현
						 */
						break;
					case WITH_LIST:
						/*
						 * 추후 필요시 구현
						 */
						break;
					default:
						break;
					}
				}else {
					// Kaifa Custom response Parsing
					logger.debug("[DLMS] GET Kaifa Custom DATA = [{}]", Hex.decode(information).trim());
					setResultData(Hex.decode(information).trim());
					
					// Command 타입설정
					setType(DataUtil.getIntToByte(HdlcObjectType.KAIFA_CUSTOM.getBytes()));
					logger.debug("[DLMS] COMMAND_TYPE = [{}]", HdlcObjectType.getItem(commandType[0]).name());
				}

			} // IFrame parsing close.

		} catch (Exception e) {
			logger.error("DLMS Decoding Error - {}", e);
			result = false;
		}

		return result;
	}

	@Override
	public String toByteString() {
		return Hex.decode(gdDLMSFrame);
	}

	/**
	 * 기존 Decode 방식이 아닌 다른 방식으로 처리해야할경우 사용
	 */
	@Override
	public Object customDecode(Procedure procedure, byte[] data) {
		logger.info("## Excute NestedDLMSDecorator - Custom Decoding...");

		int infoPos = 0;
		byte[] information = data;
		List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

		byte[] getDataResultType = new byte[1];
		System.arraycopy(information, infoPos, getDataResultType, 0, getDataResultType.length);
		infoPos += getDataResultType.length;

		logger.debug("[DLMS] Get-Data-Result-type = [{}]", DLMSCommonDataType.getItem(getDataResultType[0]).name());
		switch (DLMSCommonDataType.getItem(getDataResultType[0])) {
		case Array:
			if (procedure == Procedure.GET_PROFILE_BUFFER) {
				byte[] arrayLength = new byte[1];
				System.arraycopy(information, infoPos, arrayLength, 0, arrayLength.length);
				infoPos += arrayLength.length;

				int arraySize = 0;
				if ((arrayLength[0] & 0x80) == 0x80) {
					int aSize = arrayLength[0] & 0x7F;
					
					arrayLength = new byte[aSize];
					System.arraycopy(information, infoPos, arrayLength, 0, arrayLength.length);
					infoPos += arrayLength.length;
					
					arraySize = DataUtil.getIntToBytes(arrayLength); 
				}else{
					arraySize = DataUtil.getIntToBytes(arrayLength);
				}
				logger.debug("[DLMS] Get-Data-Result-type-size = " + arraySize);
				
				HashMap<String, Object> dataMap = new HashMap<String, Object>();

				while (information.length != infoPos) {
					byte[] structureType = new byte[1];
					System.arraycopy(information, infoPos, structureType, 0, structureType.length);
					infoPos += structureType.length;

					byte[] structureLen = new byte[1];
					System.arraycopy(information, infoPos, structureLen, 0, structureLen.length);
					infoPos += structureLen.length;

					dataMap = new HashMap<String, Object>();
					for (int i = 0; i < channelList.size(); i++) {
						HashMap<String, Object> tempMap = channelList.get(i);

						if (ObjectType.CLOCK.getValue() == Integer.parseInt(String.valueOf(tempMap.get("classId"))) && "2".equals(String.valueOf(tempMap.get("attribute")))) {
							byte[] octetType = new byte[1];
							System.arraycopy(information, infoPos, octetType, 0, octetType.length);
							infoPos += octetType.length;

							byte[] octetSize = new byte[1];
							System.arraycopy(information, infoPos, octetSize, 0, octetSize.length);
							infoPos += octetSize.length;

							byte[] year = new byte[2];
							System.arraycopy(information, infoPos, year, 0, year.length);
							infoPos += year.length;

							byte[] month = new byte[1];
							System.arraycopy(information, infoPos, month, 0, month.length);
							infoPos += month.length;

							byte[] dayOfMonth = new byte[1];
							System.arraycopy(information, infoPos, dayOfMonth, 0, dayOfMonth.length);
							infoPos += dayOfMonth.length;

							byte[] dayOfWeek = new byte[1];
							System.arraycopy(information, infoPos, dayOfWeek, 0, dayOfWeek.length);
							infoPos += dayOfWeek.length;

							byte[] hour = new byte[1];
							System.arraycopy(information, infoPos, hour, 0, hour.length);
							infoPos += hour.length;

							byte[] minute = new byte[1];
							System.arraycopy(information, infoPos, minute, 0, minute.length);
							infoPos += minute.length;

							byte[] second = new byte[1];
							System.arraycopy(information, infoPos, second, 0, second.length);
							infoPos += second.length;

							byte[] hundredthsOfSecond = new byte[1];
							System.arraycopy(information, infoPos, hundredthsOfSecond, 0, hundredthsOfSecond.length);
							infoPos += hundredthsOfSecond.length;

							byte[] deviation = new byte[2];
							System.arraycopy(information, infoPos, deviation, 0, deviation.length);
							infoPos += deviation.length;

							byte[] clockStatus = new byte[1];
							System.arraycopy(information, infoPos, clockStatus, 0, clockStatus.length);
							infoPos += clockStatus.length;

							String yyyymmdd = String.format("%04d", DataUtil.getIntTo2Byte(year)) + String.format("%02d", DataUtil.getIntToByte(month[0])) + String.format("%02d", DataUtil.getIntToByte(dayOfMonth[0]));
							String yyyymmddhhmmss = yyyymmdd + String.format("%02d", DataUtil.getIntToByte(hour[0])) + String.format("%02d", DataUtil.getIntToByte(minute[0])) + String.format("%02d", DataUtil.getIntToByte(second[0]));

							dataMap.put("yyyymmddhhmmss", yyyymmddhhmmss);
							dataMap.put("yyyymmdd", yyyymmdd);
						} else if ((ObjectType.DATA.getValue() == Integer.parseInt(String.valueOf(tempMap.get("classId"))) 
								|| ObjectType.REGISTER.getValue() == Integer.parseInt(String.valueOf(tempMap.get("classId")))) 
								&& "2".equals(String.valueOf(tempMap.get("attribute")))) {
							byte[] dataType = new byte[1];
							System.arraycopy(information, infoPos, dataType, 0, dataType.length);
							infoPos += dataType.length;

							byte[] dataValue = null;
							switch (DLMSCommonDataType.getItem(dataType[0])) {
							case Null:
								dataValue = null;
								break;
							case UINT8:
								dataValue = new byte[DLMSCommonDataType.UINT8.getLenth()];
								System.arraycopy(information, infoPos, dataValue, 0, dataValue.length);
								infoPos += dataValue.length;
								break;
							case UINT16:
								dataValue = new byte[DLMSCommonDataType.UINT16.getLenth()];
								System.arraycopy(information, infoPos, dataValue, 0, dataValue.length);
								infoPos += dataValue.length;
								break;
							case UINT32:
								dataValue = new byte[DLMSCommonDataType.UINT32.getLenth()];
								System.arraycopy(information, infoPos, dataValue, 0, dataValue.length);
								infoPos += dataValue.length;
								break;
							default:
								break;
							}

							if (String.valueOf(tempMap.get("obisCode")).startsWith("0000600B") && String.valueOf(tempMap.get("obisCode")).endsWith("FF")) {
								dataMap.put("eventCode", dataValue == null ? null : DataUtil.getIntToBytes(dataValue));
							} else {
								dataMap.put("value"+i, dataValue == null ? null : DataUtil.getIntToBytes(dataValue));
							}

						} else if (ObjectType.LIMITER.getValue() == Integer.parseInt(String.valueOf(tempMap.get("classId"))) && ("3".equals(String.valueOf(tempMap.get("attribute"))))) {
							byte[] dataType = new byte[1];
							System.arraycopy(information, infoPos, dataType, 0, dataType.length);
							infoPos += dataType.length;

							byte[] dataValue = new byte[DLMSCommonDataType.UINT32.getLenth()];
							System.arraycopy(information, infoPos, dataValue, 0, dataValue.length);
							infoPos += dataValue.length;

							dataMap.put("thresholdActive", DataUtil.getIntToBytes(dataValue));
						}
					}

					dataList.add(dataMap);
				}

				logger.debug("#### Custom Decoding Data List [Array] = {}", dataList.toString());
				return dataList;
			}
			break;

		case Structure:
			if (procedure == Procedure.GET_PROFILE_BUFFER) {
				infoPos -= getDataResultType.length;

				while (information.length != infoPos) {
					byte[] structureType = new byte[1];
					System.arraycopy(information, infoPos, structureType, 0, structureType.length);
					infoPos += structureType.length;

					byte[] structureLen = new byte[1];
					System.arraycopy(information, infoPos, structureLen, 0, structureLen.length);
					infoPos += structureLen.length;

					HashMap<String, Object> dataMap = new HashMap<String, Object>();
					for (int i = 0; i < channelList.size(); i++) {
						HashMap<String, Object> tempMap = channelList.get(i);

						if (ObjectType.CLOCK.getValue() == Integer.parseInt(String.valueOf(tempMap.get("classId"))) && "2".equals(String.valueOf(tempMap.get("attribute")))) {
							byte[] octetType = new byte[1];
							System.arraycopy(information, infoPos, octetType, 0, octetType.length);
							infoPos += octetType.length;

							byte[] octetSize = new byte[1];
							System.arraycopy(information, infoPos, octetSize, 0, octetSize.length);
							infoPos += octetSize.length;

							byte[] year = new byte[2];
							System.arraycopy(information, infoPos, year, 0, year.length);
							infoPos += year.length;

							byte[] month = new byte[1];
							System.arraycopy(information, infoPos, month, 0, month.length);
							infoPos += month.length;

							byte[] dayOfMonth = new byte[1];
							System.arraycopy(information, infoPos, dayOfMonth, 0, dayOfMonth.length);
							infoPos += dayOfMonth.length;

							byte[] dayOfWeek = new byte[1];
							System.arraycopy(information, infoPos, dayOfWeek, 0, dayOfWeek.length);
							infoPos += dayOfWeek.length;

							byte[] hour = new byte[1];
							System.arraycopy(information, infoPos, hour, 0, hour.length);
							infoPos += hour.length;

							byte[] minute = new byte[1];
							System.arraycopy(information, infoPos, minute, 0, minute.length);
							infoPos += minute.length;

							byte[] second = new byte[1];
							System.arraycopy(information, infoPos, second, 0, second.length);
							infoPos += second.length;

							byte[] hundredthsOfSecond = new byte[1];
							System.arraycopy(information, infoPos, hundredthsOfSecond, 0, hundredthsOfSecond.length);
							infoPos += hundredthsOfSecond.length;

							byte[] deviation = new byte[2];
							System.arraycopy(information, infoPos, deviation, 0, deviation.length);
							infoPos += deviation.length;

							byte[] clockStatus = new byte[1];
							System.arraycopy(information, infoPos, clockStatus, 0, clockStatus.length);
							infoPos += clockStatus.length;

							String yyyymmdd = String.format("%04d", DataUtil.getIntTo2Byte(year)) + String.format("%02d", DataUtil.getIntToByte(month[0])) + String.format("%02d", DataUtil.getIntToByte(dayOfMonth[0]));
							String yyyymmddhhmmss = yyyymmdd + String.format("%02d", DataUtil.getIntToByte(hour[0])) + String.format("%02d", DataUtil.getIntToByte(minute[0])) + String.format("%02d", DataUtil.getIntToByte(second[0]));

							dataMap.put("yyyymmddhhmmss", yyyymmddhhmmss);
							dataMap.put("yyyymmdd", yyyymmdd);
						} else if ((ObjectType.DATA.getValue() == Integer.parseInt(String.valueOf(tempMap.get("classId"))) 
								|| ObjectType.REGISTER.getValue() == Integer.parseInt(String.valueOf(tempMap.get("classId")))) 
									&& "2".equals(String.valueOf(tempMap.get("attribute")))) {
							byte[] dataType = new byte[1];
							System.arraycopy(information, infoPos, dataType, 0, dataType.length);
							infoPos += dataType.length;

							byte[] dataValue = null;
							switch (DLMSCommonDataType.getItem(dataType[0])) {
							case UINT8:
								dataValue = new byte[DLMSCommonDataType.UINT8.getLenth()];
								System.arraycopy(information, infoPos, dataValue, 0, dataValue.length);
								infoPos += dataValue.length;
								break;
							case UINT16:
								dataValue = new byte[DLMSCommonDataType.UINT16.getLenth()];
								System.arraycopy(information, infoPos, dataValue, 0, dataValue.length);
								infoPos += dataValue.length;
								break;
							case UINT32:
								dataValue = new byte[DLMSCommonDataType.UINT32.getLenth()];
								System.arraycopy(information, infoPos, dataValue, 0, dataValue.length);
								infoPos += dataValue.length;
								break;
							default:
								break;
							}

							if (String.valueOf(tempMap.get("obisCode")).startsWith("0000600B") && String.valueOf(tempMap.get("obisCode")).endsWith("FF")) {
								dataMap.put("eventCode", dataValue == null ? null : DataUtil.getIntToBytes(dataValue));
							} else {
								dataMap.put("value"+i, dataValue == null ? null : DataUtil.getIntToBytes(dataValue));
							}

						} else if (ObjectType.LIMITER.getValue() == Integer.parseInt(String.valueOf(tempMap.get("classId"))) && ("3".equals(tempMap.get("attribute")))) {
							byte[] dataType = new byte[1];
							System.arraycopy(information, infoPos, dataType, 0, dataType.length);
							infoPos += dataType.length;

							byte[] dataValue = new byte[DLMSCommonDataType.UINT32.getLenth()];
							System.arraycopy(information, infoPos, dataValue, 0, dataValue.length);
							infoPos += dataValue.length;

							dataMap.put("thresholdActive", DataUtil.getIntToBytes(dataValue));
						}
					}

					dataList.add(dataMap);
				}

				logger.debug("#### Custom Decoding Data List [Structure] = ", dataList.toString());
				
				return dataList;
			}
			break;

		default:
			break;
		}
		return null;
	}

}
