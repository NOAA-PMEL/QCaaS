/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

/**
 * @author kamb
 *
 */
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DataRow {

    @JsonProperty("values")
    @Singular("addValue")
    private List<Object> _values;
    
    public String getStringValue(int at) {
        return (String) _values.get(at);
    }
    
    public Integer getIntegerValue(int at) {
        return (Integer) _values.get(at);
    }
    
    public Double getDoubleValue(int at) {
        return (Double) _values.get(at);
    }
    
    public static void main(String[] args) {
        DataRow row = DataRow.builder().addValue("one").build();
    }
}
