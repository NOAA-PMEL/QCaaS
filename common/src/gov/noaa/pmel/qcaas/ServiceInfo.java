/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kamb
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Setter(AccessLevel.NONE)
public class ServiceInfo {

    @JsonProperty("name")
    private String _name;
    
    @JsonProperty("version")
    private String _version;

    @JsonProperty("location")
    private URL _location;
}
