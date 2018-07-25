/**
 * (@)# HLSAuthForECG.java
 *
 * 2016. 4. 13.
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
package com.aimir.fep.bypass.decofactory.consts;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aimir.fep.bypass.decofactory.consts.DlmsConstants.XDLMS_APDU;
import com.aimir.fep.bypass.decofactory.consts.DlmsConstantsForECG.DlmsPiece;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.Hex;

/**
 * @author simhanger
 *
 */
public class HLSAuthForECG {
	private static Logger logger = LoggerFactory.getLogger(HLSAuthForECG.class);

	private HLSSecurity securityMode;
	private final int tLen = 12 * Byte.SIZE; // 96 bit

	/*
	 * OAC에서 미터키를 받기위한 디바이스용 인증서를 받기위한 디바이스번호(고정값)
	 */
	private String HES_DEVICE_SERIAL;

	// Iraq MOE
	//	private final byte[] EK = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F };
	//	private final byte[] AK = { (byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB, (byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF };

	// SORIA 미터키 인증 적용하지 않을때 사용	
	//	private final byte[] EK = { (byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0 };
	//	private final byte[] AK = { (byte) 0xFE, (byte) 0xA0, (byte) 0xFE, (byte) 0xA0, (byte) 0xFE, (byte) 0xA0, (byte) 0xFE, (byte) 0xA0, (byte) 0xFE, (byte) 0xA0, (byte) 0xFE, (byte) 0xA0, (byte) 0xFE, (byte) 0xA0, (byte) 0xFE, (byte) 0xA0 };

	// Ghana TVWS
	private final byte[] EK = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F };
	private final byte[] AK = { (byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB, (byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF };
		
	// SORIA 미터키 인증 적용시 사용
	//private byte[] EK;
	//private byte[] AK;

	public static enum HLSSecurity {
		NONE(0x00), AUTHENTICATION(0x10), ENCRYPTION(0x20), AUTHENTICATION_ENCRYPTION(0x30);

		private int value;

		private HLSSecurity(int value) {
			this.value = value;
		}

		public byte[] getValue() {
			return new byte[] { (byte) value };
		}
		
		public static HLSSecurity getItem(byte value) {
			for (HLSSecurity fc : HLSSecurity.values()) {
				if (fc.value == value) {
					return fc;
				}
			}
			return null;
		}
	}
	
	public HLSAuthForECG(HLSSecurity securityMode) throws Exception {
		this.securityMode = securityMode;
	}

	public HLSAuthForECG(HLSSecurity securityMode, String meterId) throws Exception {
		this.securityMode = securityMode;

		if (securityMode == null || meterId == null || meterId.equals("")) {
			throw new Exception("HLSAuth init error.");
		}

		logger.debug("HLS Security Mode={}, MeterId={}", securityMode.name(), meterId);
		/*
		Properties prop = new Properties();
		try {
			prop.load(getClass().getClassLoader().getResourceAsStream("config/fmp.properties"));
			if (prop.containsKey("protocol.security.hes.deviceSerial")) {
				this.HES_DEVICE_SERIAL = prop.getProperty("protocol.security.hes.deviceSerial");
			} else {
				this.HES_DEVICE_SERIAL = "000H000000000003";
			}
			logger.debug("### HES DEVICE SERIAL = {}", HES_DEVICE_SERIAL);
		} catch (IOException e) {
			logger.error("Properties loading error - {}", e.getMessage());
			throw new Exception("Properties loading error.");
		}
		*/
		
		switch (HLSSecurity.NONE) {
		case AUTHENTICATION:		
			/*
			logger.debug("### getPanaMeterSharedKey ###");			
			OacServerApi oacApi = new OacServerApi();
			HashMap<String, String> sharedKey = oacApi.getPanaMeterSharedKey(HES_DEVICE_SERIAL, meterId);
			if (sharedKey != null) {
				String unicastKey = sharedKey.get("UnicastKey");
				String authKey = sharedKey.get("AuthenticationKey");
				String pinCode = sharedKey.get("pin_arg");

				logger.debug("##############  unicastKey={}", unicastKey);
				logger.debug("##############  authKey={}", authKey);
				logger.debug("##############  pinCode={}", pinCode);

				logger.debug("[UnicastKey Decrypting...][{}]", unicastKey);
				EK = DataUtil.readByteString(unicastKey);
				logger.debug("EK    = {}", Hex.decode(EK)); // encryption_unicast_key

				logger.debug("[AuthenticationKey Decrypting...][{}]", authKey);
				AK = DataUtil.readByteString(authKey);
				logger.debug("AK     = {}", Hex.decode(AK)); // authentication_key

			} else {
				throw new Exception("Can not find Shared Key.");
			}
			*/
			break;
		case AUTHENTICATION_ENCRYPTION:
			break;
		case ENCRYPTION:

			break;
		case NONE:

			break;
		default:
			break;
		}
	}

	/*
	 * RESTful Service로 Feph가 Fepa를 호출한다.
	 */
	public void getMeterKeyForHLS(String meterId) throws Exception {
		if (meterId == null || meterId.equals("")) {
			throw new Exception("HLSAuth init error - no meterId");
		}
	}

	private Cipher getCipher(int mode, byte[] iv, byte[] aad) throws Exception {
		SecretKey sKey = new SecretKeySpec(EK, "AES");
		GCMParameterSpec params = new GCMParameterSpec(tLen, iv);

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
		cipher.init(mode, sKey, params);
		cipher.updateAAD(aad);

		return cipher;
	}

	public byte[] doEncryption(byte[] ic, byte[] apTitle, byte[] aadInfor) {
		byte[] result = null;

		try {
			if (ic != null && apTitle != null && aadInfor != null) {
				/*
				 * P(plainText) : Authentication only 모드에서는 plainText를 쓰지 않는다.
				 * EK:16byte
				 * IV:12byte = Sys-T(apTitle:8byte) + IC:4byte
				 * AAD = SC:1byte + AK:16byte + P(aadInfor)
				 */
				byte[] IV = DataUtil.append(apTitle, ic);
				byte[] AAD = null;
				
				switch(securityMode) {
				case NONE:
					break;
				case AUTHENTICATION:
					AAD = DataUtil.append(DataUtil.append(securityMode.getValue(), AK), aadInfor);
					result = getCipher(Cipher.ENCRYPT_MODE, IV, AAD).doFinal(); // Authentication only 모드에서는 plainText를 쓰지 않는다
					break;
				case ENCRYPTION:
					break;
				case AUTHENTICATION_ENCRYPTION:					
					AAD = DataUtil.append(securityMode.getValue(), AK);
					result =  getCipher(Cipher.ENCRYPT_MODE, IV, AAD).doFinal(aadInfor);					
					break;
				default:
					break;
				}
				
				logger.info("[ENCRYPTION] IC    = [{}]", Hex.decode(ic));
				logger.info("[ENCRYPTION] Sys-T = [{}]", Hex.decode(apTitle));
				logger.info("[ENCRYPTION] IV    = [{}]", Hex.decode(IV));
				logger.info("[ENCRYPTION] AAD   = [{}]", Hex.decode(AAD));
				logger.info("[ENCRYPTION] AAD_INFO   = [{}]", Hex.decode(aadInfor));
				logger.info("[ENCRYPTION] CYPER_TEXT = [{}]", Hex.decode(result));
			}
		} catch (Exception e) {
			logger.error("HLSAuth Encryption Error - {}", e);
			result = null;
		}

		return result;
	}
	
	public byte[] doDecryption(byte[] ic, byte[] apTitle, byte[] cipherText) {
		byte[] result = null;
		
		try {
			if (ic != null && apTitle != null && cipherText != null) {
				/*
				 * P(plainText) = information
				 * EK:16byte
				 * IV:12byte = Sys-T(apTitle:8byte) + IC:4byte
				 * AAD
				 *  - Authentication Only      : SC:1byte + AK:16byte + (C) Information
				 *  - Encryption Only          : null 
				 *  - Authenticated encryption : SC:1byte + AK:16byte
				 */
				byte[] IV = DataUtil.append(apTitle, ic);
				byte[] AAD = null;

				switch (securityMode) {
				case NONE:
					break;
				case AUTHENTICATION:
					AAD = DataUtil.append(DataUtil.append(securityMode.getValue(), AK), cipherText);
					result = getCipher(Cipher.DECRYPT_MODE, IV, AAD).doFinal(); // Authentication only 모드에서는 plainText를 쓰지 않는다
					break;
				case ENCRYPTION:
					break;
				case AUTHENTICATION_ENCRYPTION:					
					AAD = DataUtil.append(securityMode.getValue(), AK);
					result = getCipher(Cipher.DECRYPT_MODE, IV, AAD).doFinal(cipherText);
					//Cipher chipher = getCipher(Cipher.DECRYPT_MODE, IV, AAD);
					//chipher.update(cipherText);
					//result = chipher.doFinal(cipherText);
					break;
				default:
					break;
				}

				logger.info("[DECRYPTION] IC    = [{}]", Hex.decode(ic));
				logger.info("[DECRYPTION] Sys-T = [{}]", Hex.decode(apTitle));
				logger.info("[DECRYPTION] IV    = [{}]", Hex.decode(IV));
				logger.info("[DECRYPTION] AAD   = [{}]", Hex.decode(AAD));
				logger.info("[DECRYPTION] Cyper Text = [{}]", Hex.decode(cipherText));
				logger.info("[DECRYPTION] Plain Text = [{}]", Hex.decode(result));
			}
		} catch (Exception e) {
			logger.error("HLSAuth doDecryption Error - {}", e);
			e.printStackTrace();
			result = null;
		}
		
		return result;
	}

	public byte[] getTagValue(byte[] ic, byte[] apTitle, byte[] aadInfor) {
		byte[] tagValue = null;

		if (ic != null && apTitle != null && aadInfor != null) {
			byte[] cipherText = doEncryption(ic, apTitle, aadInfor);
			tagValue = Arrays.copyOfRange(cipherText, cipherText.length - (tLen / Byte.SIZE), cipherText.length);

			logger.info("[ENCRYPTION] TAG_VALUE = [{}]", Hex.decode(tagValue));
		}

		return tagValue;
	}

	public boolean doValidation(byte[] apTitle, byte[] ic, byte[] aadInfor, byte[] responseTagValue) {
		boolean result = false;

		if (ic != null && apTitle != null && aadInfor != null && responseTagValue != null) {
			byte[] myTagValue = getTagValue(ic, apTitle, aadInfor);
			result = Arrays.equals(responseTagValue, myTagValue);

			if (!result) {
				logger.debug("[Action Response Validation] Org TagValue = [{}], Response TagValue = [{}]", Hex.decode(myTagValue), Hex.decode(responseTagValue));
			}
		}

		return result;
	}

	public byte[] getActionEncryptionGlobalCiphering(byte[] ic, byte[] information) {
		byte[] reqValue = new byte[] {};
		byte[] encValue = new byte[] {};
		
		try {
			byte[] cipherText = new byte[] {};

			if (ic != null && information != null) {
				cipherText = doEncryption(ic, DlmsPiece.CLIENT_SYSTEM_TITLE.getBytes(), information);
								
				XDLMS_APDU cyperType = XDLMS_APDU.GLO_ACTION_REQUEST;
							
				encValue = DataUtil.append(encValue, securityMode.getValue()); // SC
				encValue = DataUtil.append(encValue, ic); // IC
				encValue = DataUtil.append(encValue, cipherText); // Cipher Text
				if(encValue.length > 127) {
					reqValue = DataUtil.append(reqValue, cyperType.getValue());
					reqValue = DataUtil.append(reqValue, (byte)0x81);					
					reqValue = DataUtil.append(reqValue, DataUtil.getByteToInt(encValue.length));
					reqValue = DataUtil.append(reqValue, encValue);
				} else {
					reqValue = DataUtil.append(reqValue, cyperType.getValue());
					reqValue = DataUtil.append(reqValue, DataUtil.getByteToInt(encValue.length));
					reqValue = DataUtil.append(reqValue, encValue);
				}
				
				logger.info("[GET-REQ:GLOBAL_CIPHERING] XDLMS-APDU Type = {} [{}]", cyperType.name(), Hex.decode(cyperType.getValue()));
				logger.info("[GET-REQ:GLOBAL_CIPHERING] REQ_VALUE[APDU + LENGTH + SC + IC + CIPHER_TEXT(+TAG)] = [{}]", Hex.decode(reqValue));
			}
		} catch (Exception e) {
			logger.error("HLSAuth getReqEncryptionDedicateCiphering Error - {}", e);
			reqValue = null;
		}

		return reqValue;
	}
	
	public byte[] getReqEncryptionGlobalCiphering(byte[] ic, byte[] information) {
		byte[] reqValue = new byte[] {};

		try {
			byte[] cipherText = new byte[] {};

			if (ic != null && information != null) {
				cipherText = doEncryption(ic, DlmsPiece.CLIENT_SYSTEM_TITLE.getBytes(), information);
								
				XDLMS_APDU cyperType = XDLMS_APDU.GLO_GET_REQUEST;
				reqValue = DataUtil.append(reqValue, cyperType.getValue()); // ded-get-request
				reqValue = DataUtil.append(reqValue, new byte[1]); // length
				reqValue = DataUtil.append(reqValue, securityMode.getValue()); // SC
				reqValue = DataUtil.append(reqValue, ic); // IC
				reqValue = DataUtil.append(reqValue, cipherText); // Cipher Text
				reqValue[1] = DataUtil.getByteToInt(reqValue.length - 2); // request & request length 2바이트제외

				logger.info("[GET-REQ:GLOBAL_CIPHERING] XDLMS-APDU Type = {} [{}]", cyperType.name(), Hex.decode(cyperType.getValue()));
				logger.info("[GET-REQ:GLOBAL_CIPHERING] REQ_VALUE[APDU + LENGTH + SC + IC + CIPHER_TEXT(+TAG)] = [{}]", Hex.decode(reqValue));
			}
		} catch (Exception e) {
			logger.error("HLSAuth getReqEncryptionDedicateCiphering Error - {}", e);
			reqValue = null;
		}

		return reqValue;
	}
	
	public byte[] setReqEncryptionGlobalCiphering(byte[] ic, byte[] information) {
		byte[] reqValue = new byte[] {};

		try {
			byte[] cipherText = new byte[] {};

			if (ic != null && information != null) {
				cipherText = doEncryption(ic, DlmsPiece.CLIENT_SYSTEM_TITLE.getBytes(), information);

				XDLMS_APDU cyperType = XDLMS_APDU.GLO_SET_REQUEST;
				reqValue = DataUtil.append(reqValue, cyperType.getValue()); // ded-get-request
				reqValue = DataUtil.append(reqValue, new byte[1]); // length
				reqValue = DataUtil.append(reqValue, securityMode.getValue()); // SC
				reqValue = DataUtil.append(reqValue, ic); // IC
				reqValue = DataUtil.append(reqValue, cipherText); // Cipher Text
				reqValue[1] = DataUtil.getByteToInt(reqValue.length - 2); // request & request length 2바이트제외

				logger.info("[SET-REQ:GLOBAL_CIPHERING] XDLMS-APDU Type = {} [{}]", cyperType.name(), Hex.decode(cyperType.getValue()));
				logger.info("[SET-REQ:GLOBAL_CIPHERING] REQ_VALUE[APDU + LENGTH + SC + IC + CIPHER_TEXT(+TAG)] = [{}]", Hex.decode(reqValue));
			}
		} catch (Exception e) {
			logger.error("HLSAuth setReqEncryptionDedicateCiphering Error - {}", e);
			reqValue = null;
		}

		return reqValue;
	}
	
}
