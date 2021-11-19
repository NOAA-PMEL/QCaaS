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
     * @return
     */
    public static QcServiceData readFileData(File dataFile) throws Exception {
        return readFileData(dataFile, null);
    }
    private static final String CST = "[,;\t]";
    
    private static final int MIN_HEADERS = 4; // XXX TODO: What should this be?
    
    public static QcServiceData readFileData(File dataFile, Collection<String>selectedVars) throws Exception {
        QcServiceData.QcServiceDataBuilder data = QcServiceData.builder();
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
            
            String[] nextLine = freader.readLine().split(CST);
            String[] altHeaders = Arrays.copyOf(headers, headers.length);
            String[] units;
            if ( isDataLine(nextLine)) {
                units = tryHeaderUnits(altHeaders);
            } else {
                units = nextLine;
                nextLine = null;
            }
            int nheaders = headers.length;
            for ( int idx = 0; idx < nheaders; idx += 1) {
                String header = headers[idx];
                String columnUnits = units[idx];
                data.addVariableDefinition(VariableDefinition.builder()
                                           .standardName(StandardName.builder()
                                                         .name(header)
                                                         .build())
                                           .addSupportedUnits(StandardName.builder()
                                                              .name(columnUnits)
                                                              .build())
                                           .build());
            }
            if ( nextLine != null && nextLine.length <= nheaders ) {
                DataRow row = new DataRow(Arrays.asList(nextLine));
                data.addRow(row);
            }
            while ((line = freader.readLine()) != null) {
                line = line.trim();
                if ( line.startsWith("#")) {
                    logger.info("Found comment line in data: " + line);
                    continue;
                }
                String[] values = trimTrailers(line.split("[,;\t]"), false);
                if ( ! isDataLine(values)) {
                    logger.warn("Found non-data row while parsing data: " + line);
                    continue;
                }
                DataRow row = new DataRow(Arrays.asList(values));
                data.addRow(row);
            }
        }
        return data.build();
    }

    /**
     * @param row
     * @return
     */
    private static String[] trimTrailers(String[] row) {
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
                trimmed.add(value);
            }
        }
        return trimmed.toArray(new String[trimmed.size()]);
    }

    /**
     * @param units
     * @param header
     * @return
     */
    private static String[] tryHeaderUnits(String[] headersCopy) {
        String[] units = new String[headersCopy.length];
        for ( int col = 0; col < headersCopy.length; col += 1 ) {
            String header = headersCopy[col];
            String colUnits = "";
            int idx = header.lastIndexOf("_");
            if ( idx < 0 ) {
                logger.debug("No apparent units for column header " + header);
            } else if ( idx == header.length()-1 ) {
                logger.info("Trailing underscore for column header " + header);
            } else {
                colUnits = header.substring(idx+1); 
                String revisedHeader = header.substring(0, idx);
                headersCopy[col] = revisedHeader;
            }
            units[col] = colUnits;
        }
        return units;
    }

    /**
     * @param row
     * @return
     */
    private static boolean couldBeHeaderLine(String[] row) {
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
    private static boolean isDataLine(String[] row) {
        for (String col : row) {
            if ( ! (col == null ||
                    col.trim().isEmpty() ||
                    isNumeric(col))) {
                return false;
            }
        }
        return true;
    }

}
