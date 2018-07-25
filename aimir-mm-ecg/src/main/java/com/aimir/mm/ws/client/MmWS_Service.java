package com.aimir.mm.ws.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(name = "MmWS", 
                  wsdlLocation = "http://localhost:8082/services/MmWS?wsdl",
                  targetNamespace = "http://server.ws.mm.aimir.com/") 
public class MmWS_Service extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://server.ws.mm.aimir.com/", "MmWS");
    public final static QName MmWSPort = new QName("http://server.ws.mm.aimir.com/", "MmWSPort");
    static {
        URL url = null;
        try {
            url = new URL("http://localhost:8082/services/MmWS?wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(MmWS_Service.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "http://localhost:8082/services/MmWS?wsdl");
        }
        WSDL_LOCATION = url;
    }

    public MmWS_Service(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public MmWS_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public MmWS_Service() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns MmWS
     */
    @WebEndpoint(name = "MmWSPort")
    public MmWS getMmWSPort() {
        return super.getPort(MmWSPort, MmWS.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns MmWS
     */
    @WebEndpoint(name = "MmWSPort")
    public MmWS getMmWSPort(WebServiceFeature... features) {
        return super.getPort(MmWSPort, MmWS.class, features);
    }

}
