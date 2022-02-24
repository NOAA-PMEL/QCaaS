/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import gov.noaa.pmel.qcaas.QcServiceWS;
import gov.noaa.pmel.tws.auth.RequestSignature;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.Logging;
import gov.noaa.pmel.tws.util.StringUtils;

/**
 * @author kamb
 *
 */
@Path(QcServiceWS.DO_PUT_ENDPOINT)
@MultipartConfig
public class UploadResource   { // extends ResourceBase {

	private static Logger logger = Logging.getLogger(UploadResource.class);
	
	@Context SecurityContext securityContext;
	@Context ServletContext servletContext;
	@Context HttpServletRequest httpRequest;
	@Context HttpServletResponse httpResponse;
		
    @GET
    @Path("get/{file:.*}")
    public Response getCheck(@PathParam("file") String p_filePath) {
        System.out.println(p_filePath);
        return Response.ok(String.valueOf(httpRequest)).build();
    }
    
	@POST
	@Path("put/{file:.*}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postSyncFile(@PathParam("file") String p_filePath) { // , @DefaultValue("") @FormParam("link") String p_link) {
		Response response;
//		String req = httpRequest.getRequestURI().toString();
		logger.info("post file");
		try {
				Collection<Part>parts = httpRequest.getParts();
				Map<String, String>formParams = getFormParams(parts);
                String f_link = formParams.get("link");
				File destFile = null;
				for (Part part : parts) {
					if ( part.getSubmittedFileName() != null ) {
//						System.out.println(part.getName() + " : " + part.getSubmittedFileName());
						String fileName = part.getSubmittedFileName();
                        logger.info("Writing submitted file " + fileName + " to " + p_filePath);
						destFile = getUploadDestFile(p_filePath);
						File writtenFile = writeFile(part, destFile);
                        if ( writtenFile != null && writtenFile.exists() && 
                             ! StringUtils.emptyOrNull(f_link)) {
                            logger.info("linking " + destFile + " to " + f_link);
                            File linkFile = getUploadDestFile(f_link);
                            java.nio.file.Path linkFilePath = Paths.get(linkFile.toURI());
                            java.nio.file.Path destFilePath = Paths.get(destFile.toURI());
                            Files.createSymbolicLink(linkFilePath, destFilePath);
                        }
					} else {
					    String partValue = getPartValue(part);
					    System.out.println(part.getName() + " : " + partValue);
					}
				}
//				URL location = new URL(EventsResource.eventUrl(net, officialEventName));
//				response = Response.created(location.toURI()).entity(location.toString()).build();
                response = Response.ok().build();
		} catch (Exception e) {
			logger.warn(e, e);
			e.printStackTrace();
			response = Response.serverError().entity(e.getMessage()).build();
		}
		return response;
	}

    private static File _rootDir;
	public static File getUploadsRootDir() {
        if ( _rootDir == null ) {
    		String rootDirName = ApplicationConfiguration.getProperty("argo.server.syncdb.dir", "db_sync");
            _rootDir = new File(rootDirName);
    		if ( ! _rootDir.exists()) {
    			_rootDir.mkdirs();
    		}
        }
	    return _rootDir;
	}
	
	public static File getUploadDestFile(String submittedFileName) {
		File rootDir = getUploadsRootDir();
		String destFileName = submittedFileName; // TimeUtils.formatCurrentLocalTime_ISO_COMPRESSEDfmt() + "-" + submittedFileName;
		File destFile = new File(rootDir, destFileName);
		return destFile;
	}

    public static String getPartValue(Part part) throws IOException {
        try ( BufferedReader r = new BufferedReader(new InputStreamReader(part.getInputStream()));) {
            return r.readLine();
        }
    }

    protected static Map<String, String> getFormParams(Collection<Part> parts) throws IOException {
        Map<String, String>formParams = new HashMap<String, String>();
        for (Part part : parts) {
            if ( part.getSubmittedFileName() == null ) {
                String value = getPartValue(part);
                formParams.put(part.getName(), value);
            }
        }
        return formParams;
    }

    public static File writeFile(Part part, File dest) throws IOException {
        
        File writtenFile = null;
        FileChannel cout = null;
        String submittedFileName = part.getSubmittedFileName();
        if ( dest.exists() && dest.isDirectory()) {
            dest = new File(dest, submittedFileName);
        } else {
            if ( ! dest.getParentFile().exists()) {
                if ( ! dest.getParentFile().mkdirs()) {
                    throw new IOException("Failed to create result directory: " + dest.getParentFile().getAbsolutePath());
                }
            }
        }
        try ( ReadableByteChannel cin = Channels.newChannel(part.getInputStream())) {
            java.nio.file.Path path = dest.toPath(); // FileSystems.getDefault().getPath("uploads", part.getSubmittedFileName());
            logger.debug("Writing file to: " + path);
            cout = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            cout.transferFrom(cin, 0, part.getSize());
            writtenFile = path.toFile();
        } finally {
            try { if ( cout != null ) { cout.close(); }} catch (Exception e) {}
        }
        return writtenFile;
    }

}
