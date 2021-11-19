/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author kamb
 *
 */
@Data
@SuperBuilder
@Setter(AccessLevel.NONE)
@EqualsAndHashCode(callSuper=true)
public class QcServiceMetadata extends QcMetadata {

    @JsonProperty("service_info")
    private ServiceInfo _serviceInfo;
    
    @JsonProperty("brief_description")
    private String _briefDescription;
    
    @JsonProperty("reference_url")
    private URL _referenceUrl;

}
