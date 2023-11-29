/**
 * 
 */
package gov.noaa.pmel.qcaas.qc;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gov.noaa.pmel.tws.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author kamb
 *
 */
public class DataRange {

    private static Logger logger = LogManager.getLogger(DataRange.class);
    
    /** The minimum questionable value.
     * Below this, values are flagged as bad.
     */
    double min_questionable;
    /** The minimum acceptable value.
     * Below this, values are flagged as questionable.
     */
    double min_acceptable;
    /** The maximum acceptable value.
     * Above this, values are flagged as questionable.
     */
    double max_acceptable;
    /** The maximum questionable value.
     * Above this, values are flagged as bad.
     */
    double max_questionable;
    
    double missing_value = -999.;
    
    private static final String FLAG_GOOD = "2";
    private static final String FLAG_QUESTIONABLE = "3";
    private static final String FLAG_BAD = "4";
    private static final String FLAG_MISSING = "9";
    
    public DataRange(double min_questionable, double min_acceptable, 
                     double max_questionable, double max_acceptable) { 
        this.max_questionable = max_questionable;
        this.max_acceptable = max_acceptable;
        this.min_acceptable = min_acceptable;
        this.min_questionable  = min_questionable;
    }
    public DataRange(double min_questionable, double min_acceptable, 
                     double max_questionable, double max_acceptable,
                     double missing_value) { 
        this(min_questionable, min_acceptable, max_questionable, max_acceptable);
        this.missing_value = missing_value;
    }
    
    public String check(String string_value) {
        if ( StringUtils.emptyOrNullOrNull(string_value)) {
            return FLAG_MISSING;
        }
        try {
            return check(Double.valueOf(string_value));
        } catch (NumberFormatException nfe) {
            logger.debug("Bad string value:"+string_value);
            return FLAG_BAD;
        }
    }
    public String check(double value) {
        if ( Double.isNaN(value) || missing_value == value ) {
            return FLAG_MISSING;
        }
        if ( Double.isInfinite(value)) {
            return FLAG_BAD;
        }
        if ( value < min_questionable ||
             value > max_questionable ) { 
            return FLAG_BAD; }
        if ( value < min_acceptable ||
             value > max_acceptable ) { 
            return FLAG_QUESTIONABLE; }
        return FLAG_GOOD;
    }
}
