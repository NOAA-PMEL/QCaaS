/**
 * 
 */
package gov.noaa.pmel.qcaas.qc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class QcServiceImpl implements QcServiceIfc {

    private static final Logger logger = LogManager.getLogger(QcServiceImpl.class);

    private static final String RANDOM = "random";
    private static final String SCRIPT = "script";
    private String _testName = RANDOM;
    /**
     * 
     */
    public QcServiceImpl() {
    }

    public QcServiceImpl(String testName) {
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
        if ( SCRIPT.equals(_testName)) {
            return runQcScript(request);
        } else {
            return randomFlagging(request);
        }
    }
    public QcInvocationResponse rangeChecking(QcInvocationRequest request) throws QcServiceException {
        logger.info(request);
        try {
            QcInvocationResponseBuilder<?, ?> response = QcInvocationResponse.builder();
            QcServiceDataBuilder responseData = QcServiceData.builder();
            QcServiceData requestData = request.data();
            List<VariableDefinition> requestVariables = requestData.variables();
            List<VariableDefinition> flagVariables = new ArrayList<VariableDefinition>(requestVariables.size()*2);
            for (VariableDefinition v : requestVariables) {
//                flagVariables.add(v);
                flagVariables.add(v.toBuilder().standardName(StandardName.builder()
                                                                 .name(v.standardName().name()+"_QC")
                                                                 .build())
                                                    .dataType(DataType.FLAG)
                                                    .build());
            }
            List<VariableDefinition> responseVariables = new ArrayList<VariableDefinition>(requestVariables.size()*2);
            responseVariables.addAll(requestVariables);
            responseVariables.addAll(flagVariables);
            responseData.variables(responseVariables);
            for (DataRow dataRow : requestData.rows()) {
                DataRow.DataRowBuilder responseRow = dataRow.toBuilder();
                for (int i = 0; i < dataRow.size(); i++) {
                    Double d = dataRow.getDoubleValue(i);
//                    responseRow.addValue(d);
                    Character flag = rangeCheck(requestVariables.get(i), d);
                    responseRow.addValue(flag);
                }
                responseData.addRow(responseRow.build());
            }
            response.flaggedData(responseData.build());
            return response.build();
        } catch (Exception ex) {
            throw new QcServiceException(ex);
        }
        
    }
    /*
    alkalinity={"standard_name":"total_alkalinity","units":["umol/kg","umol/mL","mg/L","mL/L"], "min_question_value":"250", "min_accept_value":"800", "max_accept_value":"2500", "max_question_value":"2600"}
    ctd_oxygen={"standard_name":"oxygen_in_water","units":["umol/kg","mg/L","mL/L"], "min_question_value":"0", "min_accept_value":"0", "max_accept_value":"500", "max_question_value":"525"}
    ctd_temperature={"standard_name":"water_temperature","units":["degrees C"], "min_question_value":"-2.5", "min_accept_value":"-2.0", "max_accept_value":"32", "max_question_value":"38"}
    nitrate={"standard_name":"nitrate_in_water","units":["umol/kg","umol/mL","mg/L","mL/L"], "min_question_value":"-0.5", "min_accept_value":"0", "max_accept_value":"48", "max_question_value":"50"}
    oxygen={"standard_name":"oxygen_in_water","units":["umol/kg","mg/L","mL/L"], "min_question_value":"0", "min_accept_value":"0", "max_accept_value":"500", "max_question_value":"525"}
    ph_total={"standard_name":"pH_total", "min_question_value":"7", "min_accept_value":"7.2", "max_accept_value":"8.6", "max_question_value":"9"} 
    phosphate={"standard_name":"phosphate_in_water","units":["umol/kg","umol/mL","mg/L","mL/L"], "min_question_value":"-0.5", "min_accept_value":"0", "max_accept_value":"3.85", "max_question_value":"4"}
    sea_surface_temperature={"standard_name":"SST","units":["degrees C"], "min_question_value":"-2.5", "min_accept_value":"-1.5", "max_accept_value":"31", "max_question_value":"38"}
    silicate={"standard_name":"silicate_in_water","units":["umol/kg","mg/L","mL/L"], "min_question_value":"-2", "min_accept_value":"0", "max_accept_value":"240", "max_question_value":"250"}
    Temperature_atm={"standard_name":"air_temperature_at_sea_level", "file_std_unit":"degrees C","units":["deg C"],"min_question_value":"-50", "min_accept_value":"-40", "max_accept_value":"40", "max_question_value":"50"}
    */
    
    Map<String, DataRange> dataRanges = buildDataRanges();
    private static Map<String, DataRange> buildDataRanges() {
        Map<String, DataRange> ranges = new HashMap<>();
        ranges.put("total_alkalinity",new DataRange(250, 800, 2500, 2600));
		ranges.put("oxygen_in_water",new DataRange(0, 0, 500, 525));
		ranges.put("water_temperature",new DataRange(-2.5, -2.0, 32, 38));
		ranges.put("nitrate_in_water",new DataRange(-0.5, 0, 48, 50));
		ranges.put("oxygen_in_water",new DataRange(0, 0, 500, 525));
		ranges.put("pH_total", new DataRange(7, 7.2, 8.6, 9)); 
		ranges.put("phosphate_in_water",new DataRange(-0.5, 0, 3.85, 4));
		ranges.put("SST",new DataRange(-2.5, -1.5, 31, 38));
		ranges.put("silicate_in_water",new DataRange(-2, 0, 240, 250));
		ranges.put("air_temperature_at_sea_level", new DataRange(-50, -40, 40, 50));
		return ranges;
    }

    char FLAG_NOT_CHECKED = '1';

    /**
     * @param variableDefinition
     * @param d
     * @return
     */
    private char rangeCheck(VariableDefinition variableDefinition, Double d) {
        DataRange varRange = dataRanges.get(variableDefinition.standardName().name());
        if ( varRange != null ) {
            return varRange.check(d);
        }
        return FLAG_NOT_CHECKED;
    }

    public QcInvocationResponse randomFlagging(QcInvocationRequest request) throws QcServiceException {
        logger.info(request);
        try {
            QcInvocationResponseBuilder<?, ?> response = QcInvocationResponse.builder();
            QcServiceDataBuilder responseData = QcServiceData.builder();
            QcServiceData requestData = request.data();
            List<VariableDefinition> requestVariables = requestData.variables();
            List<VariableDefinition> flagVariables = new ArrayList<VariableDefinition>(requestVariables.size()*2);
            for (VariableDefinition v : requestVariables) {
//                flagVariables.add(v);
                flagVariables.add(v.toBuilder().standardName(StandardName.builder()
                                                                 .name(v.standardName().name()+"_QC")
                                                                 .build())
                                                    .dataType(DataType.FLAG)
                                                    .build());
            }
            List<VariableDefinition> responseVariables = new ArrayList<VariableDefinition>(requestVariables.size()*2);
            responseVariables.addAll(requestVariables);
            responseVariables.addAll(flagVariables);
            responseData.variables(responseVariables);
            for (DataRow dataRow : requestData.rows()) {
                DataRow.DataRowBuilder responseRow = dataRow.toBuilder();
                for (int i = 0; i < dataRow.size(); i++) {
                    Double d = dataRow.getDoubleValue(i);
//                    responseRow.addValue(d);
                    Character flag = randomFlag(d);
                    responseRow.addValue(flag);
                }
                responseData.addRow(responseRow.build());
            }
            response.flaggedData(responseData.build());
            return response.build();
        } catch (Exception ex) {
            throw new QcServiceException(ex);
        }
    }
    
    /**
     * @param d
     * @return
     */
    static Random random = new Random(System.currentTimeMillis());
    private static Character randomFlag(Double d) {
        int c = random.nextInt(10) + '0';
        return new Character((char) c);
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
