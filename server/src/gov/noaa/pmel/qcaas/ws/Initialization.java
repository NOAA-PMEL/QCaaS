/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import java.net.InetAddress;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import gov.noaa.pmel.qcaas.QcServiceWS;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.StringUtils;
import gov.noaa.pmel.tws.util.config.Configuration;
import gov.noaa.pmel.tws.util.config.ConfigurationProperty;

/**
 * @author kamb
 *
 */
public class Initialization implements ServletContextListener {

	static Logger logger = null;
	static {
		// System.out.println("Initialization Configuring logger");
		// LoggingConfigurator.Configure();
		// logger = Logging.getLogger(Initialization.class);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("destroy context:"+arg0);
		ServletContext context = arg0 != null ? arg0.getServletContext() : null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("QCaaS initialization:"+arg0);
		ServletContext context = null;
		boolean hostedMode = false;
		if ( arg0 != null ) {
			context = arg0.getServletContext();
			System.out.println("context:"+context);
			String path = context.getContextPath();
			System.out.println("path:"+path);
			hostedMode = StringUtils.emptyOrNull(path);
			System.out.println("serverinfo:"+context.getServerInfo());
		}
		System.out.println("hosted mode:"+hostedMode);
		// Logging.Configure(context);
		// logger = Logging.getLogger(Initialization.class);
		// dumpSystemProperties();
		try {
			ApplicationConfiguration.Initialize(QcServiceWS.QC_CONFIGURATION_MODULE);
            Configuration config = ApplicationConfiguration.getConfiguration();
            ConfigurationProperty check = config.getConfigProperty("qcaas.qc.check.test", null);
            System.out.println("Application config check:"+ check);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static String getHost() {
		String host = null;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			host = new StringTokenizer(addr.getHostName(),".").nextToken();
		} catch (Exception ex) {
			logger.warn("Exception getting hostname:"+ex.getMessage());
		}
		return host;
	}
	
	private static void dumpSystemProperties() {
		for (Object key : System.getProperties().keySet()) {
			System.out.println(key + " : " + System.getProperty((String)key));
		}
	}

	public static void main(String[] args) {
		new Initialization().contextInitialized(null);
	}
}
