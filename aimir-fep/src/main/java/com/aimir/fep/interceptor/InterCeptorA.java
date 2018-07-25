package com.aimir.fep.interceptor;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.ServiceInvokerInterceptor;
import org.apache.cxf.phase.Phase;

public class InterCeptorA extends AbstractSoapInterceptor {
	private static Log log = LogFactory.getLog(InterCeptorA.class);

	public InterCeptorA() {
		super(Phase.INVOKE);
		addBefore(Arrays.asList(ServiceInvokerInterceptor.class.getName()));
	}

	@Override
	public void handleMessage(SoapMessage arg0) throws Fault {
		log.debug("handleMessage");		
	}
}
