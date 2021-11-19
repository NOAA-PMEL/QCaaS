/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.util.Map;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.Singular;

/**
 * @author kamb
 *
 */
@Data
@Builder
@Setter(AccessLevel.NONE)
public class QcTestConfiguration {

    @Singular("addConfigParam")
    private Map<String, Object> _configParameters;
}
