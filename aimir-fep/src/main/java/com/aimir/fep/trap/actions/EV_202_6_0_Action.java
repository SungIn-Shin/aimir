package com.aimir.fep.trap.actions;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.aimir.dao.device.MCUCodiDao;
import com.aimir.dao.device.MCUDao;
import com.aimir.fep.trap.common.EV_Action;
import com.aimir.model.device.EventAlertLog;
import com.aimir.model.device.MCU;
import com.aimir.model.device.MCUCodi;
import com.aimir.notification.FMPTrap;

/**
 * Event ID : 202.6.0 Processing Class
 *
 * @author Y.S Kim
 * @version $Rev: 1 $, $Date: 2005-12-13 15:59:15 +0900 $,
 */
@Component
public class EV_202_6_0_Action implements EV_Action
{
    private static Log log = LogFactory.getLog(EV_202_6_0_Action.class);
    
    @Resource(name="transactionManager")
    JpaTransactionManager txmanager;
    
    @Autowired
    MCUDao mcuDao;
    
    @Autowired
    MCUCodiDao mcuCodiDao;
    
    /**
     * execute event action
     *
     * @param trap - FMP Trap(MCU Event)
     * @param event - Event Alert Log Data
     */
    public void execute(FMPTrap trap, EventAlertLog event) throws Exception
    {
        log.debug("EV_202_6_0_Action : EventCode[" + trap.getCode()
                +"] MCU["+trap.getMcuId()+"]");

        TransactionStatus txstatus = null;
        
        try {
            txstatus = txmanager.getTransaction(null);
            
            MCU mcu = mcuDao.get(trap.getMcuId());
            
            if (mcu == null)
            {
                log.debug("no mcu intance exist mcu["+trap.getMcuId()+"]");
                return;
            }
            //Code mcuType = mcu.getMcuType();
    
            // Update MCU Mobile IP        
            String ipAddr = event.getEventAttrValue("ipAddr");
            log.debug("Mobile Ipaddr=["+ipAddr+"]");
    
            if (ipAddr != null && !ipAddr.equals("") && !ipAddr.equals("0.0.0.0"))
            {
                mcu.setIpAddr(ipAddr);
            }
            
            mcu.setNetworkStatus(1);
            
            processingCodi(trap, event, mcu);
            
           // MOINSTANCE mmp = IUtil.makeDummyMO("MCUMobilePort");
            String mobileId = null;
    
            try
            {
                mobileId = trap.getVarBinds().get("2.104.1").toString();
            }
            catch (Exception e) {}
    
            if (mobileId == null)
            {
                log.debug("no mobile id in event");
                return;
            }
            //MOINSTANCE[] mobiles = IUtil.getMIOLocal().EnumerateAssociationInstances(
            //        "MCUHasMobilePort",mcu.getName(),"hasMobilePort",
            //        "MCUMobilePort");
    
            //if (mobiles == null || mobiles.length == 0)
            //{
            //    log.debug("no mobile associated by "+mcu.getName());
            //    return;
            //}
    
           // for (int i = 0; i < mobiles.length; i++)
            //{
            //    tmpId = mobiles[i].getPropertyValueString("id");
            //    if (mobileId.equals(tmpId))
            //    {
            //        mmp.setName(mobiles[i].getName());
            //    }
            //}
    
            log.debug("mobile Id=["+mobileId+"]");
            //mmp.addProperty(new MOPROPERTY("portState", "1"));
            //mmp.addProperty(new MOPROPERTY("packetIpAddr", ipAddr));
            // mcuDao.update(mcu);
            log.debug("Mobile Keepalive Event Action Compelte");
        }
        finally {
            if (txstatus != null) txmanager.commit(txstatus);
        }
    }

    
    private void processingCodi(FMPTrap trap,EventAlertLog event, MCU mcu)
        throws Exception {
        // Update Codi
    	String codiId = null;
        try
        {
        	codiId = trap.getVarBinds().get("3.3.3").toString();
        }catch (Exception e) {}

        if (codiId == null)
        {
            log.debug("no codi id in event");
            return;
        }
        
        MCUCodi mcuCodi = mcuCodiDao.findByCondition("codiId", codiId); //TODO CHECK 

        if (mcuCodi == null)
        {
            log.debug("no codi associated by "+mcu.getSysID());
            return;
        }

        //TODO 집중기와 코디간의 연관관계 설정
        //TODO CHANGE CODI PROPERTY
        /*
        String sinkState = event.getEventAttrValue("portState");
        String sinkNeighborNode = event.getEventAttrValue("neighborNodeCnt");

        log.debug("sinkState=["+sinkState+"], sinkNeighborNode=["+
                sinkNeighborNode+"]");
        if (sinkState != null && !sinkState.equals(""))
        {
            mo.addProperty(new MOPROPERTY("portState", sinkState));
        }

        if (sinkNeighborNode != null && !sinkNeighborNode.equals(""))
        {
            mo.addProperty(new MOPROPERTY("neighborNodeCnt",sinkNeighborNode));
        }
        */
    }
}
