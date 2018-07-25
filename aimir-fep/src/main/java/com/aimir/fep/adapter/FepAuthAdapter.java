/**
 *
 * (관련용어)<br>
 * <p>FEP (Front End Processor)</p>
 * <p>FMP (FEP and MCU Protocol)</p>
 * <p>MRP (Meter Read Protocol)</p>
 *
 */

package com.aimir.fep.adapter;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aimir.fep.protocol.security.FepTcpAuthenticatorAdapter;
import com.aimir.fep.util.DataUtil;
import com.aimir.fep.util.FMPProperty;


/**
 * FEP 3-Pass Authentication Startup class
 *
 * 2018.06.21
 */
public class FepAuthAdapter {
    private static Log logger = LogFactory.getLog(FepAuthAdapter.class);

    public final static String SERVICE_DOMAIN = "Service";
    public final static String ADAPTER_DOMAIN = "Adapter";

    // private MBeanServer server = null;

    private String fepName;
    // INSERT START SP-121 //
    private String  authTcpPort;
    // INSERT END SP-121 //
    
    public void init()
    {
        fepName = System.getProperty("name");
        System.setProperty("fepName", fepName);
    }

    public void setAuthTcpPort(String authTcpPort) {
        this.authTcpPort = authTcpPort;
    }

    public void startService() throws Exception {
        logger.info("Create MBean Server");

        // INSERT START SP-121 //
        logger.info("Register TcpAuthenticatorAdapter");
        registerTcpAuthenticatorAdapter();
        
        logger.info("\t" + fepName + " FEPAuth is Ready for Service...\n");
    }

    private void registerMBean(Object obj, ObjectName objName)
    throws Exception
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(obj, objName);
    }

    // INSERT START SP-121 //
    protected void registerTcpAuthenticatorAdapter()
    throws Exception
    {
        ObjectName adapterName = null;
        //ReverseGPRSAdapter adapter = new ReverseGPRSAdapter();
        FepTcpAuthenticatorAdapter adapter = new FepTcpAuthenticatorAdapter();
        logger.info("registerTcpAuthenticatorAdapter  adapter Created");
        if(authTcpPort == null || authTcpPort.length() < 1) {
        	authTcpPort = "9001";
        }
        adapter.setPort(Integer.parseInt(authTcpPort));

        adapterName = new ObjectName(
                ADAPTER_DOMAIN+":name=TcpAuthenticatorAdapter, port="+authTcpPort);
        logger.debug(adapterName);
        registerMBean(adapter, adapterName);
        adapter.start();

        logger.info("registerTcpAuthenticatorAdapter ready");
    }
    
    public static void main(String[] args) {
        String authTcpPort = "9001";
        
        if (args.length < 1 ) {
            logger.info("Usage:");
            logger.info("FepAuthAapter -DfepName AdapterName -jmxPort AdapterPort -authTcpPort authTcpPort");
            return;
        }

        for (int i=0; i < args.length; i+=2) {

            String nextArg = args[i];
            if (nextArg.startsWith("-authTcpPort")) {
                authTcpPort = new String(args[i+1]);
            }
        }

        try {
            /**
             * Audit Target Name 설정.
             */
    		String fepName = System.getProperty("name");
            if(fepName == null || fepName.equals("")){
                System.setProperty("aimir.auditTargetName", "FEPh");
            }else{
                System.setProperty("aimir.auditTargetName", fepName);
            }

            ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[]{"/config/spring-fepa.xml"});

            Tomcat tomcat = new Tomcat();
            tomcat.setPort(Integer.parseInt(FMPProperty.getProperty("fepa.webservice.port")));
            tomcat.addContext("", "");
            
            DataUtil.setApplicationContext(applicationContext);
        }
        catch (Exception e) {
            logger.error(e, e);
        }

        FepAuthAdapter fep = new FepAuthAdapter();
        fep.setAuthTcpPort(authTcpPort);
        fep.init();

        try {
            fep.startService();
        }
        catch (Exception e) {
            logger.error(e, e);
            System.exit(1);
        }
    }
}
