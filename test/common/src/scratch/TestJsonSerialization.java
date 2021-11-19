/**
 * 
 */
package scratch;


import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import gov.noaa.pmel.qcaas.QcServiceData;

/**
 * @author kamb
 *
 */
public class TestJsonSerialization {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
//            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            QcServiceData data = TestStuff.getTestData();
//            writer.writeValue(System.out, data);
            mapper.writeValue(System.out, data);
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO: handle exception
        }
    }
}
