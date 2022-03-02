/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

/**
 * @author kamb
 *
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true)
@JsonInclude(Include.NON_NULL)
public class QcServiceData {

    @JsonProperty("data_variables")
    @Singular("addDataVariableDefinition")
    private List<VariableDefinition> _data_variables;
    
    @JsonProperty("supplemental_variables")
    @Singular("addSupplementalVariableDefinition")
    private List<VariableDefinition> _supplemental_variables;
    
    @JsonProperty("flag_variables")
    @Singular("addFlagVariableDefinition")
    private List<VariableDefinition> _flag_variables;
    
    @JsonProperty("rows")
    @Singular("addRow")
    private List<DataRow> _rows; //  = new ArrayList<>();
    
}
