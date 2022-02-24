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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author kamb
 *
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
public class QcInvocationResponse extends QcServiceResponse {
    
    @JsonIgnore
    private ServiceInfo _service;
    
    @JsonIgnore
    private TestInfo _test;

    @JsonProperty("configuration")
    private QcTestConfiguration _configuration;
    
    @JsonProperty("flaggedData")
    private QcServiceData _flaggedData;

}
