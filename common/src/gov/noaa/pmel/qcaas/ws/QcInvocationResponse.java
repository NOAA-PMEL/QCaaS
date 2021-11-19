/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import gov.noaa.pmel.qcaas.QcServiceData;
import gov.noaa.pmel.qcaas.QcTestConfiguration;
import gov.noaa.pmel.qcaas.ServiceInfo;
import gov.noaa.pmel.qcaas.TestInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * @author kamb
 *
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=true)
@XmlRootElement
public class QcInvocationResponse extends QcServiceResponse {
    
    @JsonIgnore
    private ServiceInfo _service;
    
    @JsonIgnore
    private TestInfo _test;

    @JsonIgnore
    private QcTestConfiguration _configuration;
    
    @JsonProperty("flaggedData")
    private QcServiceData _flaggedData;

}
