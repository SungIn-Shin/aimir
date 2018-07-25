package com.aimir.cms.validator;

import java.util.Date;

import com.aimir.cms.constants.CMSConstants.ErrorType;
import com.aimir.cms.exception.CMSException;
import com.aimir.cms.model.AuthCred;
import com.aimir.cms.model.MeterEnt;
import com.aimir.util.DateTimeUtil;

public class MeterImportReqParameterValidator {

    public static void validator( AuthCred authCred, MeterEnt meterEnt)
			 throws com.aimir.cms.exception.CMSException {   	
    	
    	if(meterEnt == null){
    		throw new CMSException(ErrorType.Error.getIntValue(), "MeterEnt is null");    		
    	}
    	
    	if(meterEnt.getMeterSerialNo() == null || "".equals(meterEnt.getMeterSerialNo())){
    		throw new CMSException(ErrorType.Error.getIntValue(), "Meter_serial_no is empty");    
    	}
    	
    	if(meterEnt.getMake() == null || "".equals(meterEnt.getMake())){
    		throw new CMSException(ErrorType.Error.getIntValue(), "Meter Make is empty"); 
    	}
    	else {
    	    if (meterEnt.getMake().equals("MC013")) {
    	        if (meterEnt.getModel() != null && 
    	                (meterEnt.getModel().equals("ML001") 
    	                        || meterEnt.getModel().equals("ML002") 
    	                        || meterEnt.getModel().equals("ML053")
    	                        || meterEnt.getModel().equals("ML058")
    	                        || meterEnt.getModel().equals("ML046")
    	                        || meterEnt.getModel().equals("ML059"))) {
    	            
    	        } else {
    	            throw new CMSException(ErrorType.Error.getIntValue(), "Invalid Meter Make or Model"); 
    	        }
    	        
    	    } else if(meterEnt.getMake().equals("MC039")) {
    	    	// STS 모델은 Make값 "MC039", Model값 "ML089"로 들어온다.
    	    	if (meterEnt.getModel() != null && (meterEnt.getModel().equals("ML089")) ) {
    	    		// 정상값은 별도 처리없음.
    	    	} else {
    	            throw new CMSException(ErrorType.Error.getIntValue(), "Invalid Meter Make or Model of STS"); 
    	        }
    	    	
    	    } else {
    	        throw new CMSException(ErrorType.Error.getIntValue(), "Invalid Meter Make or Model"); 
    	    }
    	}
    	
    	if(meterEnt.getBatchNo() == null || "".equals(meterEnt.getBatchNo())){
    		throw new CMSException(ErrorType.Error.getIntValue(), "Batch_no is empty"); 
    	}

    	meterEnt.setWriteDate(DateTimeUtil.getDateString(new Date()));
    }
	
}
