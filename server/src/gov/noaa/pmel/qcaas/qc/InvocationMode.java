/**
 * 
 */
package gov.noaa.pmel.qcaas.qc;

import org.apache.log4j.Logger;

import gov.noaa.pmel.tws.util.Logging;
import gov.noaa.pmel.tws.util.StringUtils;

/**
 * @author kamb
 *
 */
public enum InvocationMode {
    
    FILE,
    STDIN;
    
    static InvocationMode DEFAULT = InvocationMode.FILE;
    private static final Logger logger = Logging.getLogger(InvocationMode.class);
    
    public static InvocationMode modeFor(String mode) {
        if (StringUtils.emptyOrNull(mode)) {
            return DEFAULT;
        }
        try {
            return valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException iax) {
            logger.warn("Invalid InvocationMode specified: " + mode, iax);
            return DEFAULT;
        }
    }
}
