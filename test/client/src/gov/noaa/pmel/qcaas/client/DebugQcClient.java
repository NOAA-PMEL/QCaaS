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
        
//        String serviceHost = "http://localhost:8546/qcaas/tws/qc";
        String serviceHost = "http://localhost:8288/qcs/tws/qc";
//        String dataFile = "test/data/33RO20150410.short.csv"; // "test-data.json";
//        String dataFile = "test/data/W03_WCOA2012R_djg_v2.csv"; // "test-data.json";
        String dataFile = "test/data/33RO20150410.exc_units.csv"; // "test-data.json";
        String test = "spike";
        String dataFields = "ctdprs,ctdsalinty,salnty,ctdoxy";
        String[] debugArgs = new String[] { "qc", "-f", dataFile, "-s", serviceHost, "-d", dataFields, "-t", test, "-v" };
//        String[] debugArgs = new String[] { "qc", "-s", serviceHost, "-t", test, " < ", "qcrequest.js" };
        try {
            QcClient.main(debugArgs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
