/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;

/**
 * @author kamb
 *
 */
@Builder(toBuilder=true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DataRow {

    @NonNull
    @JsonProperty("row_id")
    private String _rowId;
    
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
        Object v = _values.get(at);
        if ( v instanceof Double ) { return (Double) v; }
        return Double.parseDouble(String.valueOf(v));
    }
    
    public int size() {
        return _values.size();
    }
    
    public static void main(String[] args) {
        DataRow row = DataRow.builder().addValue("one").build();
    }
}
