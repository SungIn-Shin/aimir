package com.aimir.fep.trap.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aimir.constants.CommonConstants.TargetClass;
import com.aimir.dao.device.MCUDao;
import com.aimir.fep.trap.common.EV_Action;
import com.aimir.fep.util.EventUtil;
import com.aimir.model.device.EventAlertAttr;
import com.aimir.model.device.EventAlertLog;
import com.aimir.model.device.MCU;
import com.aimir.notification.FMPTrap;

/**
 * Event ID : EV_SP_200_34_0 Processing Class
 *
 * @author Tatsumi
 * @version $Rev: 1 $, $Date: 2016-05-17 15:59:15 +0900 $,
 */
@Component
public class EV_TW_200_34_0_Action implements EV_Action
{
    private static Log log = LogFactory.getLog(EV_TW_200_34_0_Action.class);
    
    @Autowired
    MCUDao mcuDao;
    /**
     * execute event action
     *
     * @param trap - FMP Trap(MCU Event)
     * @param event - Event Data
     */
    public void execute(FMPTrap trap, EventAlertLog event) throws Exception
    {
        log.debug("EV_SP_200_34_0_Action : EventCode[" + trap.getCode()
                +"] MCU["+trap.getMcuId()+"]");
        
        String mcuId = trap.getMcuId();
        MCU mcu = mcuDao.get(mcuId);
        log.debug("EV_SP_200_34_0_Action : mcuId[" + mcuId + "]");

        if(mcu != null)
        {
           log.debug("MCU Traffic Alarm Action Started");
           
           /*
            * added interfaceType (OID:1.4.0)
            * 2016.06.02
            */
           byte infType = Byte.parseByte(event.getEventAttrValue("byteEntry"));
           String inf = "";

           switch (infType) {
           case 0x00 :
               inf = "IF4 SSL";
               break;
           case 0x01 :
               inf = "NI SSL";
               break;
           case 0x02 :
               inf = "IF4";
               break;
           case 0x03 :
               inf = "NI";
               break;
           default:
               inf = "Interface Unknown ";
           }
           
//                EventAlertLog alert = EventUtil.findOpenAlert(event);
//                
//                if (alert == null)
//                {
//                    log.debug("MCU Traffic Alarm Action Failed");
//                    return;
//                }
//    
//                Integer t = alert.getOccurCnt();
//                int ti = t.intValue()+1;
//                alert.setOccurCnt(new Integer(ti));
//                
//                 event = alert;
           event.setActivatorId(trap.getSourceId());
           event.setActivatorType(TargetClass.DCU);
             EventAlertAttr ea = EventUtil.makeEventAlertAttr("message",
                     "java.lang.String",
                     "INTERFACE[" + inf + "] Traffic Alarm");
             event.append(ea);
        } 
        else
        {
            log.debug("MCU Traffic Alarm Action failed : Unknown MCU");
        }
    }
}
