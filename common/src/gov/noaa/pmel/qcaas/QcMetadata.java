/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author kamb
 *
 */
@Data
@SuperBuilder
@Setter(AccessLevel.NONE)
public class QcMetadata {

    @Builder.Default
    private Date _timestamp = new Date();
    
    private String _serviceVersion;
    
}
