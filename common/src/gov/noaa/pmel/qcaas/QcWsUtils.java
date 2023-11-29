/**
 * 
 */
package gov.noaa.pmel.qcaas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author kamb
 *
 */
public class QcWsUtils {

    private static final Logger logger = LogManager.getLogger(QcWsUtils.class);
    
    /**
     * @param dataFile
     * @return
     */
        /**
     * @param dataFields 
         * @return
    // Special case to send the whole file...
    public static QcServiceData readFileData(File dataFile) throws Exception {
        return readFileData(dataFile, null);
    }
     */
    public static final String CST = "[,;\t]";
    
    public static final int MIN_HEADERS = 4; // XXX TODO: What should this be?
    
    public static QcServiceData readFileData(File dataFile, 
                                             Collection<String>selectedVars)
             throws Exception {
        return readFileData(dataFile, selectedVars, Collections.emptyList());
    }
    
    public static QcServiceData readFileData(File dataFile, 
                                             Collection<String>selectedVars, 
                                             Collection<String>supplementalVars) 
             throws Exception {
        QcServiceData.QcServiceDataBuilder data = QcServiceData.builder();
        Collection<String> addedVars = new ArrayList<>(selectedVars.size()+supplementalVars.size());
        List<Integer> dataColumns = new ArrayList<>();
        try ( BufferedReader freader = new BufferedReader(new FileReader(dataFile))) {
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
                    if ( couldBeHeaderLine(row)) {
                        headers = trimTrailers(row);
                    }
                } catch (Exception ex) {
                    throw new QcServiceException("No header row found for file : " + dataFile, ex);
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
            for ( int idx = 0; idx < nheaders; idx += 1) {
                String header = headers[idx];
                if ( !selectedVars.contains(header)) {
                    continue;
                }
                logger.debug("Adding data variable " + header);
                addedVars.add(header);
                dataColumns.add(new Integer(idx));
                selectedVars.remove(header);
                String columnUnits = units[idx];
                data.addDataVariableDefinition(VariableDefinition.builder()
                                           .standardName(StandardName.builder()
                                                         .name(header)
                                                         .build())
                                           .addSupportedUnits(StandardName.builder()
                                                              .name(columnUnits)
                                                              .build())
                                           .build());
            }
            for ( int idx = 0; idx < nheaders; idx += 1) {
                String header = headers[idx];
                if ( !supplementalVars.contains(header)) {
                    continue;
                }
                logger.debug("Adding supplemental variable " + header);
                addedVars.add(header);
                dataColumns.add(new Integer(idx));
                selectedVars.remove(header);
                String columnUnits = units[idx];
                data.addSupplementalVariableDefinition(VariableDefinition.builder()
                                           .standardName(StandardName.builder()
                                                         .name(header)
                                                         .build())
                                           .addSupportedUnits(StandardName.builder()
                                                              .name(columnUnits)
                                                              .build())
                                           .build());
            }
            int rowNum = 0;
            if ( nextLine != null && nextLine.length <= nheaders ) {
                DataRow row = pullDataFields(String.valueOf(rowNum++), nextLine, dataColumns); // new DataRow(Arrays.asList(nextLine));
                data.addRow(row);
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
                DataRow row = pullDataFields(String.valueOf(rowNum++), values, dataColumns); // new DataRow(Arrays.asList(nextLine));
                data.addRow(row);
            }
        }
        if ( !selectedVars.isEmpty()) {
            logger.warn("The following variables were not found in data file " + dataFile + ". " + selectedVars);
        }
        return data.build();
    }

    /**
     * @param nextLine
     * @param dataColumns
     * @return
     */
    private static DataRow pullDataFields(String rowId, String[] values, List<Integer> dataColumns) {
        List<Object>dataFields = new ArrayList<>(dataColumns.size());
        for (Integer col : dataColumns) {
            dataFields.add(values[col.intValue()].trim());
        }
        return new DataRow(rowId, dataFields);
    }

    /**
     * @param row
     * @return
     */
    public static String[] trimTrailers(String[] row) {
        return trimTrailers(row, true);
    }
    
    private static String[] trimTrailers(String[] row, boolean failOnFlop) {
        List<String> trimmed = new ArrayList<>();
        boolean foundEmptyCell = false;
        for ( String value : row) {
            if ( value == null || value.trim().isEmpty()) {
                foundEmptyCell = true;
                continue;
            } else {
                if ( foundEmptyCell && failOnFlop ) {
                    throw new IllegalStateException("Found value in cell after empty cell.");
                }
            }
            trimmed.add(value);
        }
        return trimmed.toArray(new String[trimmed.size()]);
    }
    
    private static String[] trimTrailers(String[] row, int nHeaders) {
        if ( row.length > nHeaders ) {
            return Arrays.copyOf(row, nHeaders);
        } else {
            return row;
        }
    }

    /**
     * @param units
     * @param header
     * @return
     */
    public static String[] tryHeaderUnits(String[] headersCopy) {
        String[] units = new String[headersCopy.length];
        for ( int col = 0; col < headersCopy.length; col += 1 ) {
            String header = headersCopy[col];
            String colUnits = "";
            int idx = header.indexOf("_");
            if ( idx < 0 ) {
                logger.debug("No apparent units for column header " + header);
            } else if ( idx == header.length()-1 ) {
                logger.info("Trailing underscore for column header " + header);
            } else {
                colUnits = header.substring(idx+1); 
                if (couldBeUnits(colUnits)) {
                    String revisedHeader = header.substring(0, idx);
                    headersCopy[col] = revisedHeader;
                } else {
                    logger.info("Assuming not units: " + colUnits);
                    colUnits = "";
                }
            }
            units[col] = colUnits;
        }
        return units;
    }

    /**
     * @param colUnits
     * @return
     */
    private static boolean couldBeUnits(String colUnits) {
        String check = colUnits.toLowerCase();
        if ( "id".equals(check) ||
             check.contains("flag")) {
            return false;
        }
        return true;
    }

    /**
     * @param row
     * @return
     */
    public static boolean couldBeHeaderLine(String[] row) {
        for (String col : row) {
            if ( col == null || col.trim().isEmpty() || isNumeric(col.trim())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param value
     * @return
     */
    private static boolean isNumeric(String value) {
        try {
            double d = Double.parseDouble(value);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * @param nextLine
     * @return
     */
    public static boolean couldBeDataLine(String[] headers, String[] row) {
        if ( row == null ) {
            return false;
        }
        // Should really be equal, but has to be least as long
        // as sometimes there trailing empties
        if ( ! (row.length >= headers.length)) { 
            return false;
        }
        // skip the first few because they might be IDs of some sort
        // It would be nice if we had the units here, but that's what we're trying to find.
//        for (int idx = 5; idx < headers.length; idx++) {
//            String col = row[idx];
//            if ( ! (col == null ||
//                    col.trim().isEmpty() ||
//                    isNumeric(col))) {
//                return false;
//            }
//        }
//        return true;
        return ! couldBeHeaderLine(row);
    }
    private static boolean couldBeDataLine(String[] headers, String[] row, String[] units) {
        if ( row == null ) {
            return false;
        }
        // Should really be equal, but has to be least as long
        // as sometimes there trailing empties
        if ( ! (row.length == headers.length)) { 
            return false;
        }
        // skip the first few because they might be IDs of some sort
        // It would be nice if we had the units here, but that's what we're trying to find.
        for (int idx = 0; idx < headers.length; idx++) {
            String col = row[idx];
            boolean checkNumeric = ! units[idx].isEmpty();
            if ( ! (col == null ||
//                    col.trim().isEmpty() ||
                    (checkNumeric && isNumeric(col)))) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

}
