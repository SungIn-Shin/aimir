package com.aimir.fep.protocol.fmp.processor;

import org.springframework.beans.factory.annotation.Autowired;

import com.aimir.fep.protocol.fmp.log.CommLogger;
import com.aimir.model.device.CommLog;

/**
 * Alarm Service Data Processor
 * 
 * @author J.S Park (elevas@nuritelecom.com)
 * @version $Rev: 1 $, $Date: 2009-11-21 15:59:15 +0900 $,
 */
public class CommLogProcessor extends Processor
{
    @Autowired
    private CommLogger commLogger;

    /**
     * constructor
     *
     * @throws Exception
     */
    public CommLogProcessor() throws Exception
    {
        // commLogger = (CommLogger)ctx.getBean("commLogger");
        // commLogger.init();
    }

    /**
     * processing Alarm Service Data
     *
     * @param sdata <code>Object</code> ServiceData
     */
    public int processing(Object obj) throws Exception
    {
        log.debug(obj);
        if(!(obj instanceof CommLog))
        {
            log.debug("CommLogProcessor obj["+obj
                    +"] is not FMPCommLog");
            return 0;
        }
        CommLog commLog = (CommLog)obj;
        
        if(commLogger != null)
        {
            commLogger.sendLog(commLog);
        }
        else
        {
            try { 
                // commLogger = (CommLogger)ctx.getBean("commLogger");
                commLogger.sendLog(commLog);
            }
            catch(Exception ex)
            {
                commLogger.writeObject(commLog);
            }
        }
        
        return 1;
    }

    @Override
    public void restore() throws Exception {
        commLogger.init();
        commLogger.resendLogger();
    }

}
