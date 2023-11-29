/**
 * 
 */
package gov.noaa.pmel.qcaas.client;

/**
 * @author kamb
 *
 */
public class DebugQcClient extends DirectQcClient {

    public DebugQcClient() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        
//        String serviceHost = "http://localhost:8573/qcaas/ws/qc";
//        String serviceHost = "http://localhost:8288/qcaas/ws/qc";
        String serviceHost = "PROD";
//        String dataFile = "test/data/33RO20150410.short.csv"; // "test-data.json";
//        String dataFile = "test/data/W03_WCOA2012R_djg_v2.csv"; // "test-data.json";
//        String dataFile = "test/data/33RO20150410.exc_units.csv"; // "test-data.json";
        String dataFile = "/Users/kamb/workspace/oa_dashboard_test_data/typechecker/33RO20150410.exc.csv";
        String jsonFile = "qcrequest.js";
        String test = "qcpy"; // "random";
        String dataFields = "ctdsal,salnty,ctdoxy,oxygen";
        String suppleFields = "DATE,TIME,LATITUDE,LONGITUDE,DEPTH,CTDPRS";
        String[] debugArgs = new String[] { "qc", "-j", jsonFile, "-u", serviceHost, 
                                            "-s", "qcrequest_1.js", 
                                            "-o", "qcresponse_1.js", 
//                                            "-d", dataFields, 
//                                            "-a", suppleFields,
                                            "-t", test, "-v" };
//        String[] debugArgs = new String[] { "qc", "-s", serviceHost, "-t", test, " < ", "qcrequest.js" };
        try {
            DirectQcClient.main(debugArgs);
            System.out.println("QcDebug complete.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("QcDebug done.");
    }

}
