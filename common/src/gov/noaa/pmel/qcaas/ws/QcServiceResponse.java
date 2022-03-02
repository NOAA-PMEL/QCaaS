/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gov.noaa.pmel.qcaas.QcTestConfiguration;
import gov.noaa.pmel.qcaas.ServiceInfo;
import gov.noaa.pmel.qcaas.TestInfo;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author kamb
 *
 */
@Data
@SuperBuilder
//@Setter(AccessLevel.NONE)
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class QcServiceResponse implements QcInvocationMessage {

    @Builder.Default
    @JsonProperty("timestamp")
    private Date _timestamp = new Date();
    
    @JsonProperty("request_id")
    private String _requestId;
    
    @JsonProperty("service_info")
    private ServiceInfo _serviceInfo;
    
    @JsonProperty("test_info")
    private TestInfo _testInfo;

    @JsonProperty("configuration")
    private QcTestConfiguration _configuration;
    
  

}
