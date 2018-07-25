package com.aimir.mm;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.aimir.mm.util.MmProperty;

@Service
public class App {

	private static Log log = LogFactory.getLog(App.class);
	private String name;

	public void init() {
		name = System.getProperty("name");
		System.setProperty("name", name);

		log.info("\t" + name + " Mobile Money WEB SERVICE is Ready for Service...\n");
	}

	public static void main(String[] args) {
		try {

			log.info("Mobile Money WEB SERVICE START=SERVICE PORT[" + MmProperty.getProperty("mm.webservice.port") + "]");
			
			ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] { "/spring.xml" });

			Tomcat tomcat = new Tomcat();
			tomcat.setPort(Integer.parseInt(MmProperty.getProperty("mm.webservice.port")));
			Context context = tomcat.addContext("/", "");
			
			log.info("SESSION_TIMEOUT[" + context.getSessionTimeout() + "] SET TO["
					+ MmProperty.getProperty("mm.session.timeout") + "]");
			
			context.setSessionTimeout(Integer.parseInt(MmProperty.getProperty("mm.session.timeout")));
			Wrapper servletWrap = context.createWrapper();
			servletWrap.setName("cxf");
			CXFNonSpringServlet servlet = new CXFNonSpringServlet();
			// Wire the bus that endpoint uses with the Servlet
			servlet.setBus((Bus) applicationContext.getBean("cxf"));
			servletWrap.setServlet(servlet);
			servletWrap.setLoadOnStartup(1);
			context.addChild(servletWrap);
			context.addServletMapping("/services/*", "cxf");
			File webapps = new File(context.getCatalinaBase().getAbsolutePath() + File.separator + "webapps");
			if (!webapps.exists())
				webapps.mkdir();
			tomcat.start();

			App app = applicationContext.getBean(App.class);
			app.init();

			tomcat.getServer().await();
		} catch (Exception e) {
			log.error(e, e);
		}
	}
}
