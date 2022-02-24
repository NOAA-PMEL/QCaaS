/**
 * 
 */
package gov.noaa.pmel.qcaas.qc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.noaa.pmel.qcaas.DataRow;
import gov.noaa.pmel.qcaas.DataType;
import gov.noaa.pmel.qcaas.QcServiceData;
import gov.noaa.pmel.qcaas.QcServiceData.QcServiceDataBuilder;
import gov.noaa.pmel.qcaas.QcServiceException;
import gov.noaa.pmel.qcaas.StandardName;
import gov.noaa.pmel.qcaas.VariableDefinition;
import gov.noaa.pmel.qcaas.ws.QcInvocationRequest;
import gov.noaa.pmel.qcaas.ws.QcInvocationResponse;
import gov.noaa.pmel.qcaas.ws.QcInvocationResponse.QcInvocationResponseBuilder;
import gov.noaa.pmel.qcaas.ws.QcMetadataResponse;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.process.ScriptRunner;

/**
 * @author kamb
 *
 */
public class QcScriptRunner implements QcServiceIfc {

    private static final Logger logger = LogManager.getLogger(QcScriptRunner.class);

    private static final String RANDOM = "random";
    private static final String SCRIPT = "script";
    private String _testName = RANDOM;
    /**
     * 
     */
    public QcScriptRunner() {
    }

    public QcScriptRunner(String testName) {
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
        String[] scriptArgs = new String[2];
        try {
            String qcScript = ApplicationConfiguration.getProperty("qcaas.qc.script", "bin/qcrunner");
            logger.info("Running QC script: " + qcScript);
            File dataFile = writeDataFile(request);
            scriptArgs[0] = dataFile.getAbsolutePath();
            File flaggedFile = getFlaggedFile(request, dataFile);
            scriptArgs[1] = flaggedFile.getAbsolutePath();
            File outputFile = getOutputFile(request, dataFile);
            File errorFile = getErrorFile(request, dataFile);
            ScriptRunner runner = new ScriptRunner(new File(qcScript), outputFile, errorFile);
            int exit = runner.runScript(scriptArgs);
            QcInvocationResponse response = QcInvocationResponse.builder()
                    .flaggedData(getFlaggedData(flaggedFile))
                    .build();
            return response;
        } catch (Exception ex) {
            throw new QcServiceException(ex);
        }
    }

    /**
     * @param outputFile
     * @return
     * @throws IOException 
     */
    private QcServiceData getFlaggedData(File flaggedFile) throws IOException {
        QcServiceData flaggedData = new ObjectMapper().readValue(flaggedFile, QcServiceData.class);
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
        new ObjectMapper().writeValue(dataFile, request.data());
        return dataFile;
    }

}
