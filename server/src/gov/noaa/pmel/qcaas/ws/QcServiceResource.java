/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import gov.noaa.pmel.qcaas.QcServiceWS;
import gov.noaa.pmel.qcaas.qc.QcServiceIfc;
import gov.noaa.pmel.tws.util.Logging;

/**
 * @author kamb
 *
 */
@Path(QcServiceWS.QC_SERVICE_PATH)
public class QcServiceResource extends ResourceBase implements IfQcServiceWs {
    
    private static Logger logger = Logging.getLogger(QcServiceResource.class);
    
    @Context SecurityContext securityContext;
    @Context ServletContext servletContext;
    @Context HttpServletRequest httpRequest;
    @Context HttpServletResponse httpResponse;
        

    /* (non-Javadoc)
     * @see gov.noaa.pmel.qcaas.ws.IfQcServiceWs#getServiceMetadata()
    @GET
    @Path("")
    @Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
     */
    @Override
    public Response getServiceMetadata() {
        logger.info(dumpRequest(httpRequest));
        // TODO Auto-generated method stub
        return Response.ok(httpRequest.toString()).build();
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.qcaas.ws.IfQcServiceWs#getServiceMetadata(java.lang.String)
    @Override
    @Path("")
     */
    @GET
    @Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    public Response getServiceMetadata(@QueryParam("v") String p_version) {
        logger.info(dumpRequest(httpRequest));
        // TODO Auto-generated method stub
        return Response.ok(httpRequest.getRequestURL().toString()).build();
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.qcaas.ws.IfQcServiceWs#getTestMetadata(java.lang.String)
    @GET
    @Path("{test}")
    @Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
     */
    @Override
    public Response getTestMetadata(@PathParam("test") String testName) {
        logger.info(dumpRequest(httpRequest));
        // TODO Auto-generated method stub
        return Response.ok(httpRequest.toString()).build();
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.qcaas.ws.IfQcServiceWs#getTestMetadata(java.lang.String, java.lang.String)
    @Override
     */
    @GET
    @Path("{test}")
    @Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    public Response getTestMetadata(@PathParam("test") String p_testName, 
                                    @QueryParam("v") String p_version) {
        logger.info(dumpRequest(httpRequest));
        // TODO Auto-generated method stub
        return Response.ok(httpRequest.getRequestURL().toString()).build();
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.qcaas.ws.IfQcServiceWs#performQc(java.lang.String, gov.noaa.pmel.qcaas.ws.QcInvocationhttpRequest)
     */
    @POST
    @Override
    @Path("{test}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response performQc(@PathParam("test") String p_testName,
                              QcInvocationRequest qcRequest) {
        logger.info(dumpRequest(httpRequest));
        logger.debug(qcRequest);
        try {
            QcServiceIfc svc = QcServiceFactory.getQcService(p_testName, qcRequest);
            QcInvocationResponse response = svc.performQc(qcRequest);
            logger.debug(response);
            return Response.ok(response).build();
        } catch (Exception ex) {
            logger.warn(ex,ex);
            return Response.serverError()
                           .entity("There was an exception processing your request: " + 
                                    ex.getMessage()).build();
        }
    }

}
