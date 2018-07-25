package com.aimir.fep.trap.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aimir.dao.device.MCUDao;
import com.aimir.fep.trap.common.EV_Action;
import com.aimir.fep.util.EventUtil;
import com.aimir.model.device.EventAlertAttr;
import com.aimir.model.device.EventAlertLog;
import com.aimir.model.device.MCU;
import com.aimir.dao.device.MeterDao;
import com.aimir.dao.device.ModemDao;
import com.aimir.notification.FMPTrap;


/**
 * Event ID : EV_SP_200.38.0 (Security Alarm)
 *
 * @author tatsumi
 * @version $Rev: 1 $, $Date: 2016-05-13 10:00:00 +0900 $,
 */
@Component
public class EV_TW_200_38_0_Action implements EV_Action
{
    private static Log log = LogFactory.getLog(EV_TW_200_38_0_Action.class);

    @Autowired
    MCUDao mcuDao;
    
    @Autowired
    ModemDao modemDao;
    
    @Autowired
    MeterDao meterDao;
    /**
     * execute event action
     *
     * @param trap - FMP Trap(Security Alarm Event)
     * @param event - Event Alert Log Data
     */
    public void execute(FMPTrap trap, EventAlertLog event) throws Exception
    {
        log.debug("EV_SP_200_38_0_Action : EventName[evtSecurity] "
                +" EventCode[" + trap.getCode()
                +"] MCU["+trap.getMcuId()+"]");

        try { 
            MCU mcu = mcuDao.get(trap.getMcuId());
            if (mcu == null)
            {
                log.debug("no mcu intance exist mcu["+trap.getMcuId()+"]");
                return;
            }
            log.debug("EV_SP_200_38_0_Action : event[" + event.toString() + "]");

            byte b = Byte.parseByte(event.getEventAttrValue("byteEntry"));
            String alertType = null;
            
            switch (b) {
            case 0x00 :
                alertType = "Not Supported";
                break;
            case 0x01 : 
                alertType = "Network Authentication Failure (3 Pass Authentication)";
                break;
            case 0x02 :
                alertType = "TLS Failure";
                break;
            case 0x03 :
                alertType = "DTLS Failure";
                break;
            }
            event.setActivatorId(trap.getSourceId());
            
            EventAlertAttr ea = EventUtil.makeEventAlertAttr("message", "java.lang.String", alertType);
            event.append(ea);
            
        }
        catch(Exception ex) {
            log.error(ex,ex);
        }

        log.debug("Security Alarm Event Action Compelte");
    }
}
