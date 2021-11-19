/**
 * 
 */
package gov.noaa.pmel.qcaas.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivateKey;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.noaa.pmel.tws.client.HttpServiceResponse;
import gov.noaa.pmel.tws.client.TwsClient;
import gov.noaa.pmel.tws.client.TwsClientFactory;
import gov.noaa.pmel.qcaas.DataRow;
import gov.noaa.pmel.qcaas.QcServiceData;
import gov.noaa.pmel.qcaas.ws.QcInvocationRequest;
import gov.noaa.pmel.qcaas.ws.QcInvocationResponse;
import gov.noaa.pmel.tws.auth.HttpRequestSigner;
import gov.noaa.pmel.tws.auth.util.PrivateKeyReader;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.Logging;
import gov.noaa.pmel.tws.util.StringUtils;
import gov.noaa.pmel.tws.util.config.ConfigurationProperty;

import static gov.noaa.pmel.qcaas.ws.IfQcServiceWs.*;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * @author kamb
 *
 */
@SuperBuilder
public class QcWsClient {
    
    private static Logger logger = Logging.getLogger(QcWsClient.class);
    
    public static final String SERVICE_URL_PKEY = "argo.service.url";
    public static final String SERVICE_CLIENT_PKEY = "argo.service.clientid";
    
    protected static final String ACCEPT_JSON = MediaType.APPLICATION_JSON;
    
    @NonNull
    protected URL _serviceEndpoint;
    @Builder.Default
    protected String _clientId = "QC_CLIENT";
    @Builder.Default
    protected PrivateKey _privateKey = null;
    @Builder.Default
    protected boolean _verbose = false;
    @Builder.Default
    protected boolean _noop = false;
    
    @Builder.Default
    protected boolean _logResponse = false;
//    @Builder.Default
//    protected boolean _verbose = false;
//    @Builder.Default
//    protected boolean _no_op = false;

    protected TwsClient _twsClient;
    
    @Override
    public String toString() {
        return String.valueOf(_twsClient);
    }
    
    public QcWsClient(URL serviceUrl, String clientId, PrivateKey privateKey, 
                           boolean logResponse, boolean verbose, boolean noop) {
        _serviceEndpoint = serviceUrl;
        _clientId = clientId;
        _privateKey = privateKey;
        _verbose = verbose;
        _noop = noop;
    }
    /*
    protected QcWsClient(TwsClient twsClient, boolean logResponse) {
        _twsClient = twsClient;
        _logResponse = logResponse;
    }
    protected QcWsClient(URL serviceUrl, String clientId, PrivateKey privateKey) {
        this(TwsClientFactory.Instance(serviceUrl, clientId, privateKey), false);
    }
    protected QcWsClient(URL serviceUrl, String clientId, PrivateKey privateKey, 
                           boolean logResponse, boolean verbose, boolean noop) {
        this(TwsClientFactory.Instance()
                .serviceUrl(serviceUrl)
                .clientId(clientId)
                .privateKey(privateKey)
                .noop(noop)
                .build(), logResponse);
//        _verbose = verbose;
        logger.debug("ArgoWsClient: service:" + serviceUrl +", client:"+clientId+", noop:"+noop);
    }
    */

    /**
     * @return
     */
    public static PrivateKey getConfiguredPrivateKey() {
        PrivateKey pkey = null;
        String clientId = ApplicationConfiguration.getProperty(SERVICE_CLIENT_PKEY, "floatlab");
        pkey = getPrivateKey(clientId);
        return pkey;
    }
    
    public static PrivateKey getPrivateKey(String clientId) { // XXX TODO: duplicate of ArgoCliClient.readPrivateKey()
        try {
            if ( StringUtils.emptyOrNull(clientId)) {
                throw new IllegalArgumentException("null clientId");
            }
            String keyDir = ApplicationConfiguration.getProperty("tws.pkauth.keys.dir", null);
            logger.debug("key dir: " + keyDir);
            String configKeyFile = null;
            configKeyFile = ( StringUtils.emptyOrNull(keyDir) ? "" : keyDir + "/" ) + clientId + ".key";
            File keyFile = null;
            String pkeyFileName = configKeyFile;
            String passphrase = "";
            if ( ApplicationConfiguration.containsProperty("tws.pkauth.key.file")) {
                ConfigurationProperty prop = ApplicationConfiguration.getConfigurationProperty("tws.pkauth.key.file");
                logger.debug("Using key file " + prop.value() + " specified in " + prop.source());
                pkeyFileName = prop.value();
            }
            keyFile = new File(pkeyFileName);
            logger.debug("Pkey file: " + keyFile.getAbsolutePath());
            if ( !keyFile.exists()) {
                throw new FileNotFoundException(keyFile.getAbsolutePath());
            }
            if ( ApplicationConfiguration.containsProperty("tws.pkauth.key.pass")) {
                ConfigurationProperty prop = ApplicationConfiguration.getConfigurationProperty("tws.pkauth.key.pass");
                logger.debug("Using key file password from " + prop.source());
                passphrase = prop.value();
            }
            PrivateKey pkey = PrivateKeyReader.readFile(keyFile, passphrase);
            return pkey;
        } catch (Exception ex) {
            throw new RuntimeException("Error getting Private Key", ex);
        }
    }
    /**
     * @param t0
     * @param t1
     * @return
     */
    public static String timedNsString(long t0, long t1) {
        return timedNsString(t0, t1, "ms");
    }
    public static String timedNsString(long t0, long t1, String format) {
        String timeString;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos); ) {
            if ( "s".equalsIgnoreCase(format)) {
                ps.printf("%-4.4f s", (((double)(t1-t0)/1000000)/1000));
            } else if ( "ms".equalsIgnoreCase(format)) {
                ps.printf("%d ms", (t1-t0)/1000000);
            } else {
                ps.printf("%d ns", (t1-t0));
            }
            timeString = new String(baos.toByteArray());
        } catch (IOException iox) {
            timeString = String.valueOf(iox);
        }
        return timeString;
    }
    
    private static HttpURLConnection setupConnection(URL url, String contentType, String acceptType)
            throws IOException {
        HttpURLConnection api;
        try {
            api = (HttpURLConnection) url.openConnection();
        } catch (ClassCastException ex) {
            throw new IOException("Server URL must begin with http: or https:");
        }
        api.addRequestProperty("Content-Type", contentType);
        api.addRequestProperty("Accept", acceptType);
        api.addRequestProperty("User-Agent", "nctr/twtrigger_0.1");
        api.setDoOutput(true);
        api.setRequestMethod("POST");
        api.setDoOutput(true);
        return api;
    }

    public QcInvocationResponse invokeQc(String test, QcInvocationRequest request) {
        HttpServiceResponse serviceResponse = unsignedPostContent(QC_TEST_INVOCATION_PATH + "/" + test, 
                                                                  MediaType.APPLICATION_JSON, 
                                                                  request, 
                                                                  MediaType.APPLICATION_JSON);
        System.out.println(serviceResponse);
        return null;
    }
    private HttpServiceResponse unsignedPostContent(String requestPath, String accept, // String fileName,
                                                    QcInvocationRequest request, String contentType) {
        HttpServiceResponse response = null;
        try {
          URL url = new URL(_serviceEndpoint.toString() + requestPath);
          System.out.println("posting to " + url);
          logger.info("posting to " + url);
                      
          long t0 = System.currentTimeMillis();
          HttpURLConnection api = setupConnection(url, contentType, accept);
                      
        //              HttpRequestSigner signer = HttpRequestSigner.getInstance(api, _clientId, _privateKey);
        //              signer.signRequest();
        
          try ( OutputStream os = api.getOutputStream(); ) {
              os.write(new ObjectMapper().writeValueAsBytes(request));
              os.flush();
          }
        
          // read response - nothing expected
          response = new  HttpServiceResponse(api);
          long t1 = System.currentTimeMillis();
          logger.info("upload completed in " + ( t1 - t0 ) + "ms");
        } catch (Exception ex) {
          logger.warn(ex, ex);
          response = HttpServiceResponse.Error(HttpURLConnection.HTTP_INTERNAL_ERROR, ex.toString());
        } finally {
//          if (os != null) { try { os.close(); } catch (Exception ex) {} }
          logger.info("TIME: finish:"+requestPath+":"+System.currentTimeMillis());
        }
        return response;
    }
            
    public static void main(String[] args) {
        try {
            QcInvocationRequest request = QcInvocationRequest.builder()
                    .data(QcServiceData.builder()
                          .addRow(DataRow.builder()
                                  .addValue(new Integer(1))
                                  .addValue("one")
                                  .addValue(new Integer(2))
                                  .addValue("two")
                                  .build())
                          .build())
                    .build();
            QcWsClient client = QcWsClient.builder()
                    .serviceEndpoint(new URL("http://localhost:8288/qcs/tws/qcs"))
                    .build();
            QcInvocationResponse response = client.invokeQc("test", request);
            System.out.println(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO: handle exception
        }
    }
}
