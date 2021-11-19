/**
 * 
 */
package gov.noaa.pmel.qcaas.qc;

import gov.noaa.pmel.qcaas.QcServiceException;
import gov.noaa.pmel.qcaas.ws.QcInvocationRequest;
import gov.noaa.pmel.qcaas.ws.QcInvocationResponse;
import gov.noaa.pmel.qcaas.ws.QcMetadataResponse;

/**
 * @author kamb
 *
 */
public interface QcServiceIfc {
    
    public QcMetadataResponse getServiceMetadata();

    public QcMetadataResponse getTestMetadata();
    
    public QcInvocationResponse performQc(QcInvocationRequest msg) throws QcServiceException;
}
