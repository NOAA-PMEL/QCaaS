package gov.noaa.pmel.qcaas.ws;
/**
 * 
 */

import gov.noaa.pmel.qcaas.QcServiceWS;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.Logging;
import gov.noaa.pmel.tws.util.config.Configuration;
import gov.noaa.pmel.tws.util.config.ConfigurationProperty;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * This class provides a simple, stand-alone servlet container.
 * It has been used primarily for stand-alone testing.  
 * However, we keep it in the project to provide an internal end-point destination to upload and process 
 * benchmark test results.
 * 
 * @author kamb
 *
 */
public class TestMockWebServer {

	private static final String MODULE_NAME = "tws";

	private static final int DEFAULT_PORT = 8288;
	private static final String DEFAULT_CONTEXT = "qcs";
	private static final String DEFAULT_HANDLER = "org.eclipse.jetty.server.handler.DefaultHandler";


	private static int port = DEFAULT_PORT;
	private static final String pPORT = MODULE_NAME + ".server.port";
	
	// web application root path
	private static String contextPath = "/" + DEFAULT_CONTEXT;
	private static final String pCONTEXT = MODULE_NAME + ".server.context";
	
	// Jetty Handler.
	private static String handlerClassName = DEFAULT_HANDLER;
	private static final String pHANDLER = MODULE_NAME + ".server.handler";

	private static void parseArgs(String[] args) {
		Integer portArg = null;
		String ctxtArg = null;
		String handlerArg = null;
		List<String> argList = Arrays.asList(args);
		for (int i = 0; i < args.length; i++) {
			String argName = argList.get(i);
			switch (argName) {
				case "-p":
				case "--port":
					try { 
						if ( portArg != null ) { throw new IllegalArgumentException("Multiple ports specified: " + argList); }
						portArg = new Integer(argList.get(++i)); }
					catch (Exception ex) { throw new IllegalArgumentException("Illegal port value: " + argList, ex); }
					break;
				case "-P":
				case "--path":
					try { 
						if ( ctxtArg != null ) { throw new IllegalArgumentException("Multiple contexts specified: " + argList); }
						ctxtArg = argList.get(++i); 
						if ( ! ctxtArg.startsWith("/")) {
							ctxtArg = "/" + ctxtArg;
						}
					} catch (Exception ex) { throw new IllegalArgumentException("Invalid context path value: " + argList, ex); }
					break;
				case "-c":
				case "--class":
					try { 
						if ( handlerArg == null ) {
							handlerArg = argList.get(++i);
						} else {
							handlerArg += ";"+argList.get(++i);
						}
					} catch (Exception ex) { throw new IllegalArgumentException("Invalid handler class name: " + argList, ex); }
					break;
				default:
					System.out.println("Ignoring unknown argument: " + argName); 
			}
		}
		if ( portArg != null ) { port = portArg.intValue(); }
		if ( ctxtArg != null ) { contextPath = ctxtArg; }
		if ( handlerArg != null ) { handlerClassName = handlerArg; }
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.setProperty("configuration_debug", "true");
        Logging.Configure(QcServiceWS.QC_CONFIGURATION_MODULE);
        try {
            ApplicationConfiguration.Initialize(QcServiceWS.QC_CONFIGURATION_MODULE);
            Configuration config = ApplicationConfiguration.getConfiguration();
            ConfigurationProperty check = config.getConfigProperty("qcaas.qc.check.test", null);
            System.out.println("Application config check:"+ check);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }

//        Logger logger = Logging.getLogger(TestMockWebServer.class);
//        logger.debug("Here I am!  Yes it's me.");
//        for (Enumeration loggers=LogManager.getCurrentLoggers(); loggers.hasMoreElements(); )  {
//            Logger l = (Logger) loggers.nextElement();
//            for (Enumeration appenders=l.getAllAppenders(); appenders.hasMoreElements(); )  {
//                Appender appender = (Appender) appenders.nextElement();
//                System.out.println(appender);
//            }
//        }
//
//        Appender logfile = logger.getAppender("LOGFILE");
//        System.out.println(logfile);
//        Appender console = Logger.getRootLogger().getAppender("LOGFILE");
//        System.out.println(console);
//        Enumeration<Appender> loggers = logger.getAllAppenders();
//        while (loggers.hasMoreElements()) {
//            Appender a = loggers.nextElement();
//            System.out.println(a + " : " + a.getName());
//        }
//        loggers = logger.getRootLogger().getAllAppenders();
//        while (loggers.hasMoreElements()) {
//            Appender a = loggers.nextElement();
//            System.out.println(a + " : " + a.getName());
//        }

//		port = ApplicationConfiguration.getProperty(pPORT, port);
//		contextPath = ApplicationConfiguration.getProperty(pCONTEXT, contextPath);
//		handlerClassName = ApplicationConfiguration.getProperty(pHANDLER, handlerClassName);
		
		if ( args.length > 0 ) {
			parseArgs(args);
		}
		
		@SuppressWarnings("resource")
		ServerConnector  connector = null; 
		try {
			System.out.println("Running server on port " + port + ", listener " + handlerClassName + " for " + contextPath);
			Server server = new Server();
			connector = new ServerConnector(server); connector.setPort(port);
			server.setConnectors(new Connector[] { connector });
			 
			HttpConfiguration https = new HttpConfiguration();
			https.addCustomizer(new SecureRequestCustomizer());
			 
			runWsRsServices(server);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if ( connector != null ) { connector.close(); }
		}
	}
	
	private static void runWsRsServices(Server server) throws Exception {
		HandlerCollection contexts = new HandlerList();
		 
		ServletHolder sh = new ServletHolder(ServletContainer.class);
		String location = "tmp";
		int maxFileSize = 524288000;
		int maxRequestSize = 554288000;
		int fileSizeThreshold = 500000;
		sh.getRegistration().setMultipartConfig(new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold));
		sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
		sh.setInitParameter("com.sun.jersey.config.property.packages", "gov.noaa.pmel.qcaas.ws");//Set the package where the services reside
		sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
		
		ServletContextHandler jerseyContext = new ServletContextHandler(server, contextPath, ServletContextHandler.SESSIONS);
		jerseyContext.setResourceBase("WebContent");
        
        // uncomment to add signed message verifier
        jerseyContext.addFilter(gov.noaa.pmel.qcaas.ws.SignedMsgVerifier.class, "/tws/*", EnumSet.of(DispatcherType.REQUEST));
		jerseyContext.addServlet(sh, "/tws/*");
        
//		ServletHolder sh2 = new ServletHolder(ServletContainer.class);
//		sh2.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
//		sh2.setInitParameter("com.sun.jersey.config.property.packages", "gov.noaa.pmel.qcaas.ws");//Set the package where the services reside
//		sh2.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
//		jerseyContext.addServlet(sh, "/twx/*"); // WARN: messages NOT verified
		
//		jerseyContext.addServlet(DefaultServlet.class, "/*");
        ServletHolder holderHome = new ServletHolder("static-home", DefaultServlet.class);
        holderHome.setInitParameter("resourceBase","/Users/kamb/workspace/qcaas");
        holderHome.setInitParameter("dirAllowed","true");
        holderHome.setInitParameter("gzip", "true");
        holderHome.setInitParameter("pathInfoOnly","true");
        
//        jerseyContext.addServlet(holderHome,"/tws/files");
//        ServletHolder idbStatic1 = new ServletHolder("idbStatic1", StaticServlet.class);
//        idbStatic1.setInitParameter("resource.root.property", "argo.server.syncdb.dir");
//        jerseyContext.addServlet(idbStatic1,"/twx/idb/files/*");
//        ServletHolder idbStatic2 = new ServletHolder("idbStatic2", StaticServlet.class);
//        idbStatic2.setInitParameter("resource.root.property", "argo.server.syncdb.dir");
//        jerseyContext.addServlet(idbStatic2,"/tws/idb/files/*");
//        ServletHolder ilogStatic1 = new ServletHolder("ilmStatic1", StaticServlet.class);
//        ilogStatic1.setInitParameter("resource.root", "/");
//        jerseyContext.addServlet(ilogStatic1,"/twx/ilm/files/*");
//        ServletHolder ilogStatic2 = new ServletHolder("ilmStatic2", StaticServlet.class);
//        ilogStatic2.setInitParameter("resource.root", "/");
//        jerseyContext.addServlet(ilogStatic2,"/tws/ilm/files/*");
        
        // WARNING: Serves up content of project directory.
        jerseyContext.addServlet(holderHome,"/*");

//        jerseyContext.addFilter(gov.noaa.pmel.argo.tws.ws.SignedMsgVerifier.class, "/files/sync/*", EnumSet.of(DispatcherType.REQUEST));
		
		contexts.addHandler(jerseyContext);
		
		server.setHandler(contexts);
		
		server.start();
        server.dump(System.err);
		server.join();
		
	}
//    private static void runServices(Server server) throws Exception {
//        System.setProperty("org.eclipse.jetty.LEVEL","INFO");
//
//        // The filesystem paths we will map
//        String homePath = System.getProperty("user.home");
//        String pwdPath = System.getProperty("user.dir");
//
//        // Setup the basic application "context" for this application at "/"
//        // This is also known as the handler tree (in jetty speak)
//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setBaseResource(Resource.newResource(pwdPath));
//        context.setContextPath("argo");
//        server.setHandler(context);
//
//        // add a simple Servlet at "/dynamic/*"
//        ServletHolder holderDynamic = new ServletHolder("argo", ServletContainer.class);
//		holderDynamic.setInitParameter("com.sun.jersey.config.property.packages", "gov.noaa.pmel.argo.tws.ws");//Set the package where the services reside
//        context.addServlet(holderDynamic, "/ws/*");
//
//        // add special pathspec of "/home/" content mapped to the homePath
//        ServletHolder holderHome = new ServletHolder("static-home", DefaultServlet.class);
//        holderHome.setInitParameter("resourceBase","WebContext");
//        holderHome.setInitParameter("dirAllowed","true");
//        holderHome.setInitParameter("pathInfoOnly","true");
//        context.addServlet(holderHome,"/*");
//
//        // Lastly, the default servlet for root content (always needed, to satisfy servlet spec)
//        // It is important that this is last.
////        ServletHolder holderPwd = new ServletHolder("default", DefaultServlet.class);
//////        holderPwd.setInitParameter("dirAllowed","false");
////        context.addServlet(holderPwd,"/");
//
//        try {
//            server.start();
//            server.dump(System.err);
//            server.join();
//        }
//        catch (Throwable t) {
//            t.printStackTrace(System.err);
//        }
//    }
}
