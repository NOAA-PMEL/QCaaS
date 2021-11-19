/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

/**
 * @author kamb
 *
 */
@Data
@Builder
@Setter(AccessLevel.NONE)
public class ServiceInfo {

    @JsonProperty("name")
    private String _name;
    
    @JsonProperty("version")
    private String _version;

    @JsonProperty("location")
    private URL _location;
}
