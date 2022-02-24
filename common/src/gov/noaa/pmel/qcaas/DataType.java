/**
 * 
 */
package gov.noaa.pmel.qcaas;

/**
 * @author kamb
 *
 */
public enum DataType {
    
    BOOLEAN(Boolean.class),
    INTEGER(Integer.class),
    DECIMAL(Double.class),
    STRING(String.class),
    FLAG(Character.class);

    private Class _class;
    
    private DataType(Class dataClass) {
        _class = dataClass;
    }
    
    public Class typeClass() {
        return _class;
    }
}
