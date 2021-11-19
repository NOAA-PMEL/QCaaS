/**
 * 
 */
package gov.noaa.pmel.qcaas.qc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.noaa.pmel.qcaas.QcServiceData;
import gov.noaa.pmel.qcaas.QcServiceException;
import gov.noaa.pmel.qcaas.ws.QcInvocationRequest;
import gov.noaa.pmel.qcaas.ws.QcInvocationResponse;
import gov.noaa.pmel.qcaas.ws.QcMetadataResponse;
import gov.noaa.pmel.tws.util.process.ScriptRunner;

/**
 * @author kamb
 *
 */
public class QcServiceImpl implements QcServiceIfc {

    private static final Logger logger = LogManager.getLogger(QcServiceImpl.class);

    /**
     * 
     */
    public QcServiceImpl() {
        // TODO Auto-generated constructor stub
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
        logger.info(request);
        String[] scriptArgs = new String[2];
        try {
            File dataFile = writeDataFile(request);
            scriptArgs[0] = dataFile.getAbsolutePath();
            File flaggedFile = getFlaggedFile(request, dataFile);
            scriptArgs[1] = flaggedFile.getAbsolutePath();
            File outputFile = getOutputFile(request, dataFile);
            File errorFile = getErrorFile(request, dataFile);
            ScriptRunner runner = new ScriptRunner(new File("bin/qcrunner"), outputFile, errorFile);
            int exit = runner.runScript(scriptArgs);
            QcInvocationResponse response = QcInvocationResponse.builder()
                    .flaggedData(getFlaggedData(outputFile))
                    .build();
            return response;
        } catch (Exception ex) {
            throw new QcServiceException(ex);
        }
    }

    /**
     * @param outputFile
     * @return
     */
    private QcServiceData getFlaggedData(File outputFile) {
        // TODO Auto-generated method stub
        return null;
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
