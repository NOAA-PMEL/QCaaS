/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.net.URL;
import java.util.List;

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
public class QcTestMetadata extends QcMetadata {

    @JsonProperty("test_name")
    private String _testName;
    
    @JsonProperty("description")
    private String _description;
    
    @JsonProperty("reference_url")
    private URL _referenceUrl;
    
    @JsonProperty("checked_variable")
    private VariableDefinition _checkedVariable;
    
    @JsonProperty("supplementary_variables")
    private List<VariableDefinition> _supplementaryVariables;
}
