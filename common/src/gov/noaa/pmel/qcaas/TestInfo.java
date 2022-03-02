/**
 * 
 */
package gov.noaa.pmel.qcaas;

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
public class TestInfo {
    
    @JsonProperty("name")
    private String _name;

}
