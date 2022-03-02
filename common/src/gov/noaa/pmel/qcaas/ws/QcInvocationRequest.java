/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gov.noaa.pmel.qcaas.QcServiceData;
import gov.noaa.pmel.qcaas.QcTestConfiguration;
import gov.noaa.pmel.qcaas.TestInfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author kamb
 *
 */
@Data
@Builder
@Setter(AccessLevel.NONE)
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(Include.NON_NULL)
public class QcInvocationRequest implements QcInvocationMessage {

    @Builder.Default
    @JsonProperty("request_id")
    private String _requestId = String.valueOf(System.currentTimeMillis());
    
    @JsonProperty("test_info")
    private TestInfo _test;
    
    @JsonProperty("configuration")
    private QcTestConfiguration _configuration;
    
    @JsonProperty("data")
    private QcServiceData _data;
}
