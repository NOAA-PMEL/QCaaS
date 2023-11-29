/**
 * 
 */
package gov.noaa.pmel.qcaas.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gov.noaa.pmel.qcaas.QcServiceException;
import gov.noaa.pmel.qcaas.QcWsUtils;
import gov.noaa.pmel.qcaas.qc.DataRange;
import static gov.noaa.pmel.qcaas.QcWsUtils.*;

/**
 * @author kamb
 *
 */
public class TestDirectQc {

    private static final Logger logger = LogManager.getLogger(TestDirectQc.class);

    private static final String COMMA = ",";
    private static final String SEMI = ";";
    private static final String TAB = "\t";
    
    private static double SST_min_questionable = 10;
    private static double SST_min_acceptable = 20;
    private static double SST_max_acceptable = 30;
    private static double SST_max_questionable = 40;

    private File _dataFile;
    private List<String> _dataVars;
    private List<String> _supplementalVars;
    
    public TestDirectQc(File dataFile, 
                         List<String>dataVars, 
                         List<String>supplementalVars)  {
        this._dataFile = dataFile;
        this._dataVars = dataVars;
        this._supplementalVars = supplementalVars;
    }
 
    public List<List<String>> doTest() throws Exception {
        DataRange test = new DataRange(SST_min_questionable, SST_min_acceptable, 
                                       SST_max_acceptable, SST_max_questionable);
        
        long t0 = System.nanoTime();
        List<List<String>> fileData = readFileData();
        long tRead = System.nanoTime();
        List<String> flags = new ArrayList<>(fileData.size());
        for (List<String> row : fileData) {
            String flag = test.check(row.get(0));
            flags.add(flag);
            row.add(flag);
        }
        long tFlag = System.nanoTime();
        writeFlaggedData(fileData);
        long tWrite = System.nanoTime();
        
        logTime(t0,tRead,tFlag,tWrite);
        
        return fileData; // flags;
    }
    
    /**
     * @param t0
     * @param tRead
     * @param tFlag
     * @param tWrite
     * @throws IOException 
     */
    private void logTime(long t0, long tRead, long tFlag, long tWrite) throws IOException {
        String timeFileName = _dataFile.getName() + ".time";
        File timeFile = new File(_dataFile.getParent(), timeFileName);
        long readTimeMs = (tRead - t0) / 1000000;
        long flagTimeMs = (tFlag - tRead) / 1000000;
        long writeTimeMs = (tWrite - tFlag) / 1000000;
        long totalTimeMs = (tWrite - t0) / 1000000;
        
        try (BufferedWriter out = new BufferedWriter(new FileWriter(timeFile))) {
            out.append("Read:\t" + readTimeMs + " ms\n");
            out.append("Flag:\t" + flagTimeMs + " ms\n");
            out.append("Write:\t" + writeTimeMs + " ms\n");
            out.append("Total:\t" + totalTimeMs + " ms\n");
        }
    }

    /**
     * @param fileData
     * @throws IOException 
     */
    private void writeFlaggedData(List<List<String>> fileData) throws IOException {
        String flaggedFileName = _dataFile.getName() + ".flagged";
        File flaggedFile = new File(_dataFile.getParent(), flaggedFileName);
        try (BufferedWriter out = new BufferedWriter(new FileWriter(flaggedFile))) {
            for (List<String> row : fileData) {
                String sep = "";
                for (String value : row) {
                    out.append(sep).append(value);
                    sep = COMMA;
                }
                out.append("\n");
            }
        }
        
    }

    private List<List<String>> readFileData() 
             throws Exception {
        List<List<String>> data = new ArrayList<List<String>>();
        List<Integer> dataColumns = new ArrayList<>();
        _dataVars = new ArrayList<>(_dataVars);
        _supplementalVars = new ArrayList<>(_supplementalVars);
        try ( BufferedReader freader = new BufferedReader(new FileReader(_dataFile))) {
            String line = null;
            String[] headers = null;
            do {
                try {
                    String rowLine = freader.readLine();
                    if ( rowLine == null ) {
                        throw new Exception("End of file reached.");
                    }
                    rowLine = rowLine.trim();
                    if ( rowLine.isEmpty() || rowLine.startsWith("#")) {
                        continue;
                    }
                    String[] row = rowLine.split(CST);
                    if ( row.length < MIN_HEADERS ) {
                        logger.debug("Skipping short row: " + rowLine);
                        continue;
                    }
                    if ( QcWsUtils.couldBeHeaderLine(row)) {
                        headers = QcWsUtils.trimTrailers(row);
                    }
                } catch (Exception ex) {
                    throw new QcServiceException("No header row found for file : " + _dataFile, ex);
                }
                
            } while ( headers == null );
            
            String nextRowLine = freader.readLine();
//            if ( nextRowLine == null ) {
//                throw new Exception("End of file reached.");
//            }
            nextRowLine = nextRowLine.trim();
//            if ( nextRowLine.isEmpty() || nextRowLine.startsWith("#")) {
//                nextRowLine = "#";
//            }
            String[] nextLine = nextRowLine.split(CST);
            String[] altHeaders = Arrays.copyOf(headers, headers.length);
            String[] units;
            if ( couldBeDataLine(headers, nextLine)) {
                units = tryHeaderUnits(altHeaders);
            } else {
                units = nextLine;
                nextLine = null;
            }
            int nheaders = headers.length;
            for ( int idx = 0; idx < nheaders && ! _dataVars.isEmpty(); idx += 1) {
                String header = headers[idx];
//                if ( !selectedVars.contains(header)) {
                if ( ! _dataVars.stream().anyMatch(header::equalsIgnoreCase)) {
                    continue;
                }
                logger.debug("Adding data variable " + header);
//                addedVars.add(header);
                dataColumns.add(new Integer(idx));
                _dataVars.remove(header);
                String columnUnits = units[idx];
//                data.addDataVariableDefinition(VariableDefinition.builder()
//                                           .standardName(StandardName.builder()
//                                                         .name(header)
//                                                         .build())
//                                           .addSupportedUnits(StandardName.builder()
//                                                              .name(columnUnits)
//                                                              .build())
//                                           .build());
            }
            for ( int idx = 0; idx < nheaders && ! _supplementalVars.isEmpty(); idx += 1) {
                String header = headers[idx];
//                if ( !supplementalVars.contains(header))  
                if ( ! _supplementalVars.stream().anyMatch(header::equalsIgnoreCase)) {
                        continue;
                }
                logger.debug("Adding supplemental variable " + header);
//                addedVars.add(header);
                dataColumns.add(new Integer(idx));
                _dataVars.remove(header);
                String columnUnits = units[idx];
//                data.addSupplementalVariableDefinition(VariableDefinition.builder()
//                                           .standardName(StandardName.builder()
//                                                         .name(header)
//                                                         .build())
//                                           .addSupportedUnits(StandardName.builder()
//                                                              .name(columnUnits)
//                                                              .build())
//                                           .build());
            }
            int rowNum = 0;
            if ( nextLine != null && nextLine.length <= nheaders ) {
                List<String> row = pullDataFields(String.valueOf(rowNum++), nextLine, dataColumns); // new DataRow(Arrays.asList(nextLine));
                data.add(row);
            }
            while ((line = freader.readLine()) != null) {
                line = line.trim();
                if ( line.startsWith("#")) {
                    logger.info("Found comment line in data: " + line);
                    continue;
                }
                String[] values = line.split("[,;\t]"); // trimTrailers(line.split("[,;\t]"), false);
//                if ( ! couldBeDataLine(headers, values, units)) {
//                    logger.warn("Found non-data row while parsing data: " + line);
//                    continue;
//                }
                if ( values.length < headers.length ) {
                    logger.info("short line at row " + rowNum + ":" + line);
                    continue;
                }
                List<String> row = pullDataFields(String.valueOf(rowNum++), values, dataColumns); // new DataRow(Arrays.asList(nextLine));
                data.add(row);
            }
        }
        if ( !_dataVars.isEmpty()) {
            logger.warn("The following variables were not found in data file " + _dataFile + ". " + _dataVars);
        }
        return data;
    }
    
    
    private static List<String> pullDataFields(String rowId, String[] values, List<Integer> dataColumns) {
        List<String>dataFields = new ArrayList<>(dataColumns.size());
        for (Integer col : dataColumns) {
            dataFields.add(values[col.intValue()].trim());
        }
        return dataFields;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            // read file
            // extract variable
            // get flags
            // write file
                                            
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO: handle exception
        }

    }

}
