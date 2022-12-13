/**
 * 
 */
package gov.noaa.pmel.qcaas.qc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.noaa.pmel.qcaas.QcServiceData;
import gov.noaa.pmel.qcaas.QcServiceException;
import gov.noaa.pmel.qcaas.ws.QcInvocationRequest;
import gov.noaa.pmel.qcaas.ws.QcInvocationResponse;
import gov.noaa.pmel.qcaas.ws.QcMetadataResponse;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.ApplicationConfiguration.PropertyNotFoundException;
import gov.noaa.pmel.tws.util.FileUtils;
import gov.noaa.pmel.tws.util.StringUtils;
import gov.noaa.pmel.tws.util.process.ScriptRunner;

/**
 * @author kamb
 *
 */
public class QcScriptRunner implements QcServiceIfc {

    private static final Logger logger = LogManager.getLogger(QcScriptRunner.class);

    private static final String RANDOM = "random";
    private static final String STANDARD_SCRIPTS_DIR = "content/qcaas/bin/";
    private static String SCRIPTS_DIR;
    static {
        SCRIPTS_DIR = ApplicationConfiguration.getProperty("qcaas.qc.script.dir", STANDARD_SCRIPTS_DIR);
        if ( ! SCRIPTS_DIR.endsWith("/")) {
            SCRIPTS_DIR += "/";
        }
    }
    
//    private File _script;
    private String _scriptName = RANDOM;
//    private OutputStream _outputStream;
//    private InputStream _inputStream;
    private InvocationMode _mode;
    
    /**
     * 
     */
    private QcScriptRunner() {
    }

    // XXX Get test v script name sorted out...
    public QcScriptRunner(String testName, InvocationMode mode) {
        this();
        _scriptName = testName;
        _mode = mode;
    }
//    public QcScriptRunner(File script, OutputStream output, InputStream input) {
//        _script = script;
//        _outputStream = output;
//        _inputStream = input;
//    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.qcaas.qc.QcServiceIfc#getServiceMetadata()
     */
    @Override
    public QcMetadataResponse getServiceMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.qcaas.qc.QcServiceIfc#getTestMetadata()
     */
    @Override
    public QcMetadataResponse getTestMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.qcaas.qc.QcServiceIfc#performQc(gov.noaa.pmel.qcaas.ws.QcInvocationRequest)
     */
    @Override
    public QcInvocationResponse performQc(QcInvocationRequest request) throws QcServiceException {
        switch (_mode) {
            case STDIN:
                return runQcScriptWithStdIn(request);
            case FILE:
            default:
                return runQcScriptWithFileInput(request);
        }
    }

    public QcInvocationResponse runQcScriptWithStdIn(QcInvocationRequest request) throws QcServiceException {
        String[] commands = {_scriptName};
        ProcessBuilder builder = new ProcessBuilder(commands);
//        builder.directory(new File("C:/windows/system32"));
        File errFile;
        try {
            errFile = getTempFile(".err");
            logger.info("errFile:"+errFile);
        } catch (IOException iex) {
            throw new QcServiceException("Error creating script error output: " + iex, iex);
        }
        
        QcInvocationResponse qcResponse;
        
        try {
            Process process = builder.start();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream inputStream = getBaInputStream(request);
    
            OutputStream stdin = process.getOutputStream();
            InputStream stdout = process.getInputStream();
            InputStream stderr = process.getErrorStream();
    
            try ( BufferedReader pOut = new BufferedReader(new InputStreamReader(stdout));
                  BufferedWriter pIn = new BufferedWriter(new OutputStreamWriter(stdin));
                  BufferedReader pErr = new BufferedReader(new InputStreamReader(stderr)); ) {
    
                new Thread(() -> {
                    String read;
                    try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(outputStream));) {
                        while ((read = pOut.readLine()) != null) {
                            output.write(read);
                            output.newLine();
                            output.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
        
                new Thread(() -> {
                    String read;
                    try (BufferedWriter error = new BufferedWriter(new FileWriter(errFile));) {
                        while ((read = pErr.readLine()) != null) {
                            error.write(read);
                            error.newLine();
                            error.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
        
                new Thread(() -> {
                        try (Scanner scanner = new Scanner(inputStream)) {
                            String line;
                            while (scanner.hasNextLine()) {
                                line = scanner.nextLine();
                                pIn.write(line);
                                pIn.newLine();
                                pIn.flush();
                            }
                            pIn.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }).start();
            
                System.out.println("Waiting");
                int result = process.waitFor();
                System.out.println("result: " + result);
            } catch (InterruptedException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
            qcResponse = new ObjectMapper().readValue(outputStream.toByteArray(), QcInvocationResponse.class);
            return qcResponse;
        } catch (IOException iox) {
            throw new QcServiceException("There was an error running the Qc Script:" + iox.getMessage(), iox);
        }
    }
    
    /**
     * @param request
     * @return
     * @throws JsonProcessingException 
     */
    private ByteArrayInputStream getBaInputStream(QcInvocationRequest request) throws JsonProcessingException {
        // TODO Auto-generated method stub
        byte[] bytes = new ObjectMapper().writeValueAsString(request).getBytes();
        return new ByteArrayInputStream(bytes);
    }

    public QcInvocationResponse runQcScriptWithFileInput(QcInvocationRequest request) throws QcServiceException {
        logger.info(request);
        String[] scriptArgs = new String[4];
        try {
            String qcScript = _scriptName; // getQcScript(_scriptName, request);
            logger.info("Running QC script: " + qcScript);
            File dataFile = writeDataFile(request);
            int idx=0;
            scriptArgs[idx++] = "-f";
            scriptArgs[idx++] = dataFile.getAbsolutePath();
            scriptArgs[idx++] = "-o";
            File flaggedFile = getFlaggedFile(request, dataFile);
            scriptArgs[idx++] = flaggedFile.getAbsolutePath();
            File outputFile = getOutputFile(request, dataFile);
            File errorFile = getErrorFile(request, dataFile);
            ScriptRunner runner = new ScriptRunner(new File(qcScript), outputFile, errorFile);
            int exit = runner.runScript(scriptArgs);
            if ( exit == 0 ) {
                QcInvocationResponse response = getFlaggedData(flaggedFile);
    //                    QcInvocationResponse.builder()
    //                    .flaggedData(getFlaggedData(flaggedFile))
    //                    .build();
                return response;
            } else {
                String errorOut = FileUtils.readFully(runner.getErrorFile());
                String errMsg = new StringBuilder()
                        .append("Non zero result (")
                        .append(exit)
                        .append(") running test " )
                        .append( _scriptName )
                        .append( " on request " )
                        .append( request.requestId())
                        .append("\nError Output:")
                        .append(errorOut)
                        .toString();
                throw new IllegalStateException(errMsg);
            }
        } catch (Exception ex) {
            logger.warn(ex, ex);
            throw new QcServiceException(ex);
        }
    }

    /**
     * @param request
     * @return
     * @throws PropertyNotFoundException 
     */
    public static String getQcScript(String testName, QcInvocationRequest request) {
        String baseScriptNameProperty = "qcaas.qc.script";
        String scriptNameProperty = baseScriptNameProperty + 
                (StringUtils.emptyOrNull(testName) ? "" : "." + testName);
        String scriptName = ApplicationConfiguration.getProperty(scriptNameProperty, null);
        if ( StringUtils.emptyOrNull(scriptName)) {
            try {
                scriptName = ApplicationConfiguration.getProperty(baseScriptNameProperty);
            } catch (PropertyNotFoundException ex) {
                throw new RuntimeException("No QC script defined.  You must specify qcaas.qc.script or qcaas.qc.script.[test_name] configuration property.");
            }
        }
        if ( ! scriptName.startsWith("/") &&
             ! scriptName.startsWith(SCRIPTS_DIR)) {
            scriptName = SCRIPTS_DIR + scriptName;
        }
        return scriptName;
    }

    /**
     * @param outputFile
     * @return
     * @throws IOException 
     */
    private QcInvocationResponse getFlaggedData(File flaggedFile) throws IOException {
        QcInvocationResponse flaggedData = new ObjectMapper().readValue(flaggedFile, QcInvocationResponse.class);
        return flaggedData;
    }

    /**
     * @param request
     * @param dataFile 
     * @return
     */
    private File getFlaggedFile(QcInvocationRequest request, File dataFile) {
        return getRelatedFile(dataFile, ".flagged");
    }

    /**
     * @param request
     * @param dataFile 
     * @return
     */
    private static File getOutputFile(QcInvocationRequest request, File dataFile) {
        return getRelatedFile(dataFile, ".out");
    }
    
    private static File getErrorFile(QcInvocationRequest request, File dataFile) {
        return getRelatedFile(dataFile, ".err");
    }
    
    private static File getRelatedFile(File forFile, String relation) {
        String dataFilePath = forFile.getAbsolutePath();
        String dotRelation = relation.startsWith(".") ? relation : "."+relation;
        String outputFilePath = dataFilePath.substring(0, dataFilePath.lastIndexOf('.')) + dotRelation;
        return new File(outputFilePath);
    }

    /**
     * @param request
     * @return
     * @throws IOException 
     */
    private static File writeDataFile(QcInvocationRequest request) throws IOException {
        File dataFile = getTempFile(".data");
        new ObjectMapper().writeValue(dataFile, request); // .data());
        return dataFile;
    }
    
    private static File getTempFile(String extension) throws IOException {
        String fullExt = extension.startsWith(".") ? extension : "." + extension;
        File tmpFile = File.createTempFile("qc_", fullExt);
        return tmpFile;
    }

    /**
     * @param request
     * @param _testName
     * @param scriptMode
     * @return
     */
    public static QcScriptRunner runnerFor(QcInvocationRequest request, String testName, InvocationMode scriptMode) {
        QcScriptRunner runner = new QcScriptRunner(testName, scriptMode);
        return runner;
    }

}
