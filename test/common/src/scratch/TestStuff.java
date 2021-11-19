/**
 * 
 */
package scratch;

import gov.noaa.pmel.qcaas.DataRow;
import gov.noaa.pmel.qcaas.QcServiceData;

/**
 * @author kamb
 *
 */
public class TestStuff {

    public static QcServiceData getTestData() {
        QcServiceData data = QcServiceData.builder()
            .addRow(DataRow.builder()
                        .addValue("one")
                        .addValue(2)
                        .addValue(3.4)
                        .build()
                    )
            .build();
        return data;
    }
    
    public static void main(String[] args) {
        try {
            QcServiceData data = getTestData();
            System.out.println(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO: handle exception
        }
    }
}
