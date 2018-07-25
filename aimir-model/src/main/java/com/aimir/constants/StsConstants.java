package com.aimir.constants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 * @file  	: StsConstant.java
 * @author	: sejin 
 * @date	: 2018. 6. 1.
 * @desc	:
 */
@Component
public class StsConstants {
	private static Log log = LogFactory.getLog(StsConstants.class);
	
	public enum SuniCMD {
    	cmdSetPaymentMode((byte)0x01),
    	cmdGetPaymentMode((byte)0x11),
    	cmdGetRemainingCredit((byte)0x02),
    	cmdSetSTSToken((byte)0x03),
    	SetInitialCredit((byte)0x03),
    	cmdGetSTSToken((byte)0x13),
    	cmdSetTariff((byte)0x04),
    	cmdGetTariff((byte)0x14),
    	cmdSetFriendlyCreditSchedule((byte)0x05),
    	cmdGetFriendlyCreditSchedule((byte)0x15),
    	cmdSetEmergencyCredit((byte)0x06),
    	cmdGetEmergencyCredit((byte)0x16),
    	cmdGetPreviousMonthNetCharge((byte)0x07),
    	cmdGetSpecificMonthNetCharge((byte)0x17),
    	cmdSetMessage((byte)0x08),
    	cmdSuniFirmwareUpdateKeyWrite((byte)0x09),
    	cmdSuniFirmwareUpdateKeyRead((byte)0x19),
    	cmdGetSuniFirmwareUpdateInfo((byte)0x0A),
        cmdGetCIUCommStateHistory((byte)0x0B),
        cmdSuniFirmwareUpdateControl_0((byte)0x0C),
        cmdSuniFirmwareUpdateFileBlockWrite((byte)0x0D),
        cmdSuniFirmwareUpdateFileBlockRead((byte)0x1D),
        cmdSetSTSSetup((byte)0x0E),
        cmdGetSTSSetup((byte)0x1E),
        cmdSetRFSetup((byte)0x0F),
        cmdGetRFSetup((byte)0x1F);
        
        private byte cmd;
        
        SuniCMD(byte cmd) {
            this.cmd = cmd;
        }
        
        public byte getCmd() {
            return this.cmd;
        }
    }
}
