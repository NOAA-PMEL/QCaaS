/**
 * 
 */
package gov.noaa.pmel.qcaas.qc;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author kamb
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class DataRange {

    double max_questionable;
    double max_acceptable;
    double min_acceptable;
    double min_questionable;
    
    double missing_value = -999.;
    
    char FLAG_GOOD = '2';
    char FLAG_QUESTIONABLE = '3';
    char FLAG_BAD = '4';
    char FLAG_MISSING = '9';
    
    public DataRange(double max_questionable, double max_acceptable, 
                     double min_acceptable, double min_questionable) {
        this.max_questionable = max_questionable;
        this.max_acceptable = max_acceptable;
        this.min_acceptable = min_acceptable;
        this.min_questionable  = min_questionable;
    }
    
    public char check(double value) {
        if ( value < min_questionable ||
             value > max_questionable ) { return FLAG_BAD; }
        if ( value < min_acceptable ||
             value > max_acceptable ) { return FLAG_QUESTIONABLE; }
        return FLAG_GOOD;
    }
}
