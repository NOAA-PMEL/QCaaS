/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

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
@Builder(toBuilder=true)
@Setter(AccessLevel.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class StandardName {

    @JsonProperty("name")
    private String _name;
    
    @JsonProperty("vocabulary")
    private URI _vocabulary;
}
