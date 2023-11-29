/**
 * 
 */
package gov.noaa.pmel.qcaas.client;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author kamb
 *
 */
public class DebugQcWsClient extends DirectQcClient {

    public DebugQcWsClient() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        
//        String serviceHost = "http://localhost:8546/qcaas/tws/qc";
        String serviceHost = "http://localhost:8288/qcs/tws/qc";
        String requestFile = "test/data/qcrequest.js"; 
        String test = "spike";
        String[] fileArgs = new String[] { "-f", requestFile, "-s", serviceHost, "-t", test, "-v" };
        String[] inStreamArgs = new String[] { "-s", serviceHost, "-t", test };
        boolean useInStream = true;
        String[] debugArgs = useInStream ? inStreamArgs : fileArgs;
        try {
            if ( useInStream ) {
                System.setIn(new FileInputStream(requestFile));
            }
            QcWsClient.main(debugArgs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
