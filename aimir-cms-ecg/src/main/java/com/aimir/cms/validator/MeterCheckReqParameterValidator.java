package com.aimir.cms.validator;

import java.util.Date;

import com.aimir.cms.exception.CMSException;
import com.aimir.cms.model.AuthCred;
import com.aimir.cms.model.MeterEnt;
import com.aimir.util.DateTimeUtil;

public class MeterCheckReqParameterValidator {

    public static void validator( AuthCred authCred, MeterEnt meterEnt)
			 throws com.aimir.cms.exception.CMSException {   	
    	
    	if(meterEnt == null){
    		throw new CMSException("MeterEnt is null");    		
    	}
    	
    	if(meterEnt.getMeterSerialNo() == null || "".equals(meterEnt.getMeterSerialNo())){
    		throw new CMSException("Meter_serial_no is empty");    
    	}
    	
    	if(meterEnt.getMake() == null || "".equals(meterEnt.getMake())){
    		throw new CMSException("Make is empty"); 
    	}

    	meterEnt.setWriteDate(DateTimeUtil.getDateString(new Date()));
    }
	
}
