/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;

/**
 * @author kamb
 *
 */
@Data
@Builder
@Setter(AccessLevel.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class VariableDefinition {

    @JsonProperty("standard_name")
    private StandardName _standardName;
    
    private String _description;
    
    @Singular("addSupportedUnits")
    private List<StandardName> _supportedUnits;
    
    private DataType _dataType;
}
