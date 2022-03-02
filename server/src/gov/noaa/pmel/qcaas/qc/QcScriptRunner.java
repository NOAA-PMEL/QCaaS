/**
 * 
 */
package gov.noaa.pmel.qcaas.qc;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
    private static final String SCRIPT = "script";
    private static final String STANDARD_SCRIPTS_DIR = "content/qcaas/bin/";
    private String _testName = RANDOM;
    private String SCRIPTS_DIR;
    /**
     * 
     */
    private QcScriptRunner() {
        SCRIPTS_DIR = ApplicationConfiguration.getProperty("qcaas.qc.script.dir", STANDARD_SCRIPTS_DIR);
        if ( ! SCRIPTS_DIR.endsWith("/")) {
            SCRIPTS_DIR += "/";
        }
    }

    public QcScriptRunner(String testName) {
        this();
        _testName = testName;
    }

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
        return runQcScript(request);
    }

    public QcInvocationResponse runQcScript(QcInvocationRequest request) throws QcServiceException {
        logger.info(request);
        String[] scriptArgs = new String[4];
        try {
            String qcScript = getQcScript(request);
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
                        .append( _testName )
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
    private String getQcScript(QcInvocationRequest request) {
        String baseScriptNameProperty = "qcaas.qc.script";
        String scriptNameProperty = baseScriptNameProperty + 
                (StringUtils.emptyOrNull(_testName) ? "" : "." + _testName);
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
    private File getOutputFile(QcInvocationRequest request, File dataFile) {
        return getRelatedFile(dataFile, ".out");
    }
    
    private File getErrorFile(QcInvocationRequest request, File dataFile) {
        return getRelatedFile(dataFile, ".err");
    }
    
    private File getRelatedFile(File forFile, String relation) {
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
    private File writeDataFile(QcInvocationRequest request) throws IOException {
        File dataFile = File.createTempFile("qc_", ".data");
        new ObjectMapper().writeValue(dataFile, request); // .data());
        return dataFile;
    }

}
