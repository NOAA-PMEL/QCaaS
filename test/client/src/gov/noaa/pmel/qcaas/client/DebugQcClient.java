/**
 * 
 */
package gov.noaa.pmel.qcaas.client;

/**
 * @author kamb
 *
 */
public class DebugQcClient extends QcClient {

    public DebugQcClient() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        String serviceHost = "http://localhost:8288/qcs/tws/qc";
//        String dataFile = "test/data/33RO20150410.short.csv"; // "test-data.json";
        String dataFile = "test/data/33RO20150410.exc_testing.csv"; // "test-data.json";
        String test = "spike";
        String[] debugArgs = new String[] { "qc", "-f", dataFile, "-s", serviceHost, "-t", test };
        try {
            QcClient.main(debugArgs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
