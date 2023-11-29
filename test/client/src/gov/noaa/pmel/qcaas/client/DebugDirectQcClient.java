/**
 * 
 */
package gov.noaa.pmel.qcaas.client;

/**
 * @author kamb
 *
 */
public class DebugDirectQcClient extends DirectQcClient {

    public DebugDirectQcClient() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        
//        String serviceHost = "http://localhost:8573/qcaas/ws/qc";
//        String serviceHost = "http://localhost:8288/qcaas/ws/qc";
//        String serviceHost = "PROD";
//        String dataFile = "test/data/33RO20150410.short.csv"; // "test-data.json";
//        String dataFile = "test/data/W03_WCOA2012R_djg_v2.csv"; // "test-data.json";
//        String dataFile = "test/data/33RO20150410.exc_units.csv"; // "test-data.json";

//        TEMP_SBE37_MEAN,time,latitude,longitude
//        sd1033_tpos_2022_TEMP_SBE37_MEAN-20221129T163000.csv
//        sd1033_tpos_2022_TEMP_SBE37_MEAN-20221129T163007.csv
//        sd1033_tpos_2022_TEMP_SBE37_MEAN-20221129T163015.csv
        String dataFields = "TEMP_SBE37_MEAN";
        String suppleFields = "TIME,LATITUDE,LONGITUDE";
        String dataDir = "/Users/kamb/workspace/qcaas/test/data/saildrone/";
        String data1day = "sd1033_tpos_2022_TEMP_SBE37_MEAN-20221129T163000.csv.1day";
        String data7day = "sd1033_tpos_2022_TEMP_SBE37_MEAN-20221129T163007.csv.7day";
        String data30day = "sd1033_tpos_2022_TEMP_SBE37_MEAN-20221129T163015.csv.30day";


//        String dataFile = "/Users/kamb/workspace/qcaas/test/data/saildrone/test_20220725T132252.csv";
//        String test = "qcpy"; // "random";
//        String dataFields = "TEMP-AIR-MEAN"; // Note that actual saildrone files use underscores: TEMP_AIR_MEAN // _degree_C";
        String[] debugArgs = new String[] { "qc", 
//                                            "-s", "qcrequest_1.js", 
//                                            "-o", "qcresponse_1.js", 
                                            "-f", "placeHolder",
                                            "-d", dataFields, 
                                            "-a", suppleFields,
//                                            "-t", test,
                                            "-v" };
//        String[] debugArgs = new String[] { "qc", "-s", serviceHost, "-t", test, " < ", "qcrequest.js" };
        try {
            String dataFile = dataDir + data30day;
            debugArgs[2] = dataFile;
            DirectQcClient.main(debugArgs);
            System.out.println("QcDebug complete.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("QcDebug done.");
    }

}
