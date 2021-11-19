/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;


import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.Logging;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

/**
 * @author kamb
 *
 */
//@Path("*")	// Make sure this is consistent with the PATH variable
@Path("")
public class BaseResource {
	
//	public static final String PATH = "tws";
	
	public static String serviceBase = ApplicationConfiguration.getProperty("argo.service.url", "http:/localhost:8088/argolab"); // XXX TODO: discover dynamically from context ?

	private static Logger logger = Logging.getLogger(BaseResource.class);
	
	@Context SecurityContext securityContext;
	@Context ServletContext servletContext;
	@Context HttpServletRequest httpRequest;
	@Context HttpServletResponse httpResponse;
		
	@GET
	@Produces("text/plain")
	public Response defaultGetbase() {
		String req = httpRequest.getRequestURI().toString();
		logger.info("plain root:"+req);
		return Response.ok("plain root: " + req).build();
	}

	@GET
	@Produces("text/html")
	public Response defaultGetHtml() {
		String req = httpRequest.getRequestURI().toString();
		logger.info("html root:"+req);
		return Response.ok("html root: " + req).build();
	}
	
	@GET
	@Path("{default: .*}")
	public Response badBad() {
		String req = httpRequest.getRequestURI().toString();
		logger.debug("bad:"+req);
		System.out.println("bad:"+req);
		return Response.status(Status.NOT_FOUND).entity(req).build();
	}
	
//	public static File writeFile(Part part, File dest) throws IOException {
//		
//		File writtenFile = null;
//	    FileChannel cout = null;
//	    if ( ! dest.getParentFile().exists()) {
//	    	if ( ! dest.getParentFile().mkdirs()) {
//	    		throw new IOException("Failed to create result directory: " + dest.getParentFile());
//	    	}
//	    }
//		try ( ReadableByteChannel cin = Channels.newChannel(part.getInputStream())) {
//			java.nio.file.Path path = dest.toPath(); // FileSystems.getDefault().getPath("uploads", part.getSubmittedFileName());
//			logger.debug("Writing file to: " + path);
//		    cout = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
//		    cout.transferFrom(cin, 0, part.getSize());
//		    writtenFile = path.toFile();
//		} finally {
//			try { if ( cout != null ) { cout.close(); }} catch (Exception e) {}
//		}
//		return writtenFile;
//	}

}
