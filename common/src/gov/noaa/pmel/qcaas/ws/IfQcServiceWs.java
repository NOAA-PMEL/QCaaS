/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import javax.ws.rs.core.Response;

/**
 * @author kamb
 *
 */
public interface IfQcServiceWs {
    
    public static final String QC_SERVICE_METADATA_PATH = "";
    public static final String QC_TEST_METADATA_PATH = "";
    public static final String QC_TEST_INVOCATION_PATH = "";
    
    Response getServiceMetadata();
//    Response getServiceMetadata(String p_version);
    
    Response getTestMetadata(String p_testName, String p_version);
//    Response getTestMetadata(String p_testName, String p_version);
    
    Response performQc(String p_test, QcInvocationRequest qcRequest);
    
}
