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
import lombok.ToString;

/**
 * @author kamb
 *
 */
@Data
@Builder
@ToString
@Setter(AccessLevel.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class QcServiceData {

    @JsonProperty("variables")
    @Singular("addVariableDefinition")
    private List<VariableDefinition> _variables;
    
    @JsonProperty("rows")
    @Singular("addRow")
    private List<DataRow> _rows; //  = new ArrayList<>();
    
}
