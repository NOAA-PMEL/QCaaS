/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import java.util.Date;

import gov.noaa.pmel.qcaas.ServiceInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author kamb
 *
 */
@Data
@SuperBuilder
@Setter(AccessLevel.NONE)
public class QcServiceResponse {

    @Builder.Default
    private Date _timestamp = new Date();
    
    private ServiceInfo _serviceInfo;
    
    private String _request;
    

}
