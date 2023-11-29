/**
 * 
 */
package gov.noaa.pmel.qcaas.client;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//import com.fasterxml.jackson.databind.ObjectMapper;

import gov.noaa.pmel.tws.client.impl.TwsClientImpl.NoopException;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.Logging;
import gov.noaa.pmel.tws.util.StringUtils;
import gov.noaa.pmel.tws.util.cli.CLClient;
import gov.noaa.pmel.tws.util.cli.CLCommand;
import gov.noaa.pmel.tws.util.cli.CLOption;
import gov.noaa.pmel.tws.util.cli.CLOptionValue;
import gov.noaa.pmel.tws.util.cli.CLOptions;

/**
 * @author kamb
 *
 */
public class DirectQcClient extends CLClient {

    private static Logger logger = LogManager.getLogger(DirectQcClient.class);
    
//    private static CLOption opt_jsonFile = CLOption.builder().name("json_file").flag("j").longFlag("jsonfile")
//            .description("path to the QcServiceRequest JSON file").build();
    private static CLOption opt_dataFile = CLOption.builder().name("data_file").flag("f").longFlag("datafile")
            .description("path to the observation data CSV file").build();
    private static CLOption opt_dataFields = CLOption.builder().name("data_fields").flag("d").longFlag("datafields")
            .description("comma-separated list of data column header names to be sent for QC.").build();
    private static CLOption opt_supplementalFields = CLOption.builder().name("supplemental_fields").flag("a").longFlag("supplemental")
            .requiredOption(false).description("comma-separated list of additional data column header names required for QC.").build();
    private static CLOption opt_outputFile = CLOption.builder().name("output_file").flag("o").longFlag("output")
            .description("path to response output file.").defaultValue("standard out").build();
//    private static CLOption opt_serviceUrl = CLOption.builder().name("service_url").flag("u").longFlag("url")
//            .description("Base URL for QC service.").defaultValue(DEFAULT_SERVICE_URL).build();
//    private static CLOption opt_qcTest = CLOption.builder().name("qc_test").flag("t").longFlag("test")
//            .defaultValue("SST").description("Name of QC test to perform.").build();
//    private static CLOption opt_dumpRequest = CLOption.builder().name("save_request").flag("s").longFlag("save")
//            .description("Write QcInvocationRequest JSON to specified file")
//            .defaultValue("qcrequest.js").build();
    
    private static CLOption opt_batch = CLOption.builder().name("batch").flag("y").longFlag("batch")
            .requiresValue(false)
            .description("batch mode: assume yes at prompts").build();
    private static CLOption opt_noop = CLOption.builder().name("no-op").flag("x").longFlag("noop")
            .requiresValue(false)
            .description("Do not perform requested operation.  Output options and parameters and exit.").build();
    private static CLOption opt_verbose = CLOption.builder().name("verbose").flag("v").longFlag("verbose")
            .requiresValue(false)
            .description("Verbose (limited) output.").build();
   
    @SuppressWarnings("unused") // found by reflection
    private static CLCommand cmd_qcData = CLCommand.builder().name("run_qc")
                                                .command("qc")
                                                .description("Invoke QC service on specified data.")
//                                                .option(opt_jsonFile)
                                                .option(opt_dataFile)
                                                .option(opt_outputFile)
                                                .option(opt_dataFields)
                                                .option(opt_supplementalFields)
//                                                .option(opt_serviceUrl)
//                                                .option(opt_qcTest)
//                                                .option(opt_dumpRequest)
//                                                .option(opt_noop)
//                                                .option(opt_verbose)
                                                .build();

    private CLOptions _clOptions;
    
    /**
     * 
     */
    public DirectQcClient() {
        // TODO Auto-generated constructor stub
    }

    public boolean confirm(String message) {
        if ( _clOptions.booleanValue(opt_batch, false)) {
            return true;
        }
        String fullMessage = "Confirm: " + message + " [yN]: ";
        System.out.print(fullMessage);
        Console console = System.console();
        if ( console == null ) { // running in IDE
            return getUserResponse(fullMessage, "N").toLowerCase().startsWith("y"); // use code below.
        }
        String answer = System.console().readLine();
        return answer != null && answer.startsWith("y");
    }
    private static String getUserResponse(String msg, String defaultValue) {
        try {
//            String prompt = msg + (defaultValue != null ? " [" + defaultValue + "]" : "") + " : ";
//            System.out.print(prompt);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String response = reader.readLine();
            if ( StringUtils.emptyOrNull(response)) {
                response = defaultValue;
            }
            return response;
        } catch (IOException iox) {
            throw new RuntimeException("Exception reading user input.", iox);
        }
    }
    @Override
    public void doCommand(CLCommand command, Map<CLOption, CLOptionValue> optionValues, List<String> arguments) {
        try {
            // checkOptions(command);  done in CLClient runCommand checkArgs()
            
            _clOptions = new CLOptions(command, optionValues, arguments);
//            System.out.println("clOptions:"+_clOptions);
            
            Method processingMethod = getProcessingMethod(command);
            processingMethod.invoke(this);
            
        } catch (InvocationTargetException itex) {
            if ( itex.getCause() == null ||
                 ! ( itex.getCause() instanceof NoopException )) {
                itex.printStackTrace();
            }
        } catch (NoopException nex) {
            System.out.println(nex.getMessage() + " - Exiting.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doQc() throws Exception {
        logger.info("doQc");
        logger.debug(_clOptions);
//        String test = _clOptions.get(opt_qcTest);
        String opt_dataFileName = _clOptions.get(opt_dataFile);
        String opt_dataVars = _clOptions.get(opt_dataFields);
        String opt_supplVars = _clOptions.get(opt_supplementalFields);
        File dataFile = new File(opt_dataFileName);
        String[] dataVarSplit = opt_dataVars.split(",");
        List<String> dataVars = Arrays.asList(dataVarSplit);
        String[] supplVarSplit = opt_supplVars.split(",");
        List<String> supplVars = Arrays.asList(supplVarSplit);
        
        TestDirectQc tdc = new TestDirectQc(dataFile, dataVars, supplVars);
        List<List<String>> data = tdc.doTest();
        
        System.out.println("Found " + data.size() + " rows.");
        
//        String jsonFileName = _clOptions.get(opt_jsonFile);
//        QcInvocationRequest request;
//        String jsonFileName = _clOptions.get(opt_jsonFile);
//        QcInvocationRequest request;
//        if ( ! StringUtils.emptyOrNull(jsonFileName)) {
//            File jsonFile = new File(jsonFileName);
//            request = new ObjectMapper().readValue(jsonFile, QcInvocationRequest.class);
//        } else {
//            String dataFileName = _clOptions.get(opt_dataFile);
//            String dataFields = _clOptions.get(opt_dataFields);
//            String supplFields = _clOptions.get(opt_supplementalFields);
//            Collection<String> selectedFields = null;
//            if ( dataFields != null ) {
//                selectedFields = extractSelectedHeadNames(dataFields);
//            }
//            Collection<String> supplementalFields = null;
//            if ( supplFields != null ) {
//                supplementalFields = extractSelectedHeadNames(supplFields);
//            }
//            request = buildInvocationRequest(dataFileName, 
//                                             selectedFields,
//                                             supplementalFields);
//        }
//        if ( _optionValues.containsKey(opt_dumpRequest)) {
//            String dumpRequest = _clOptions.optionValue(opt_dumpRequest, "qcrequest.js");
//            writeJson(request, dumpRequest);
//        }
//        QcInvocationResponse response = _wsClient.invokeQc(test, request);
//        String outputFile = _clOptions.optionValue(opt_outputFile, null);
//        if ( outputFile != null ) {
//            writeJson(response, outputFile);
//        } else {
//            new ObjectMapper().writeValue(System.out, response);
//        }
    }

    /**
     * @param response
     * @param outputFile
    private void writeJson(QcInvocationMessage response, String outputFile) throws Exception {
        try (PrintWriter out = new PrintWriter(new File(outputFile))) {
            out.println(new ObjectMapper().writeValueAsString(response));
        }
    }
     */

    /**
     * @param fields
     * @return
     */
    private Collection<String> extractSelectedHeadNames(String fields) {
        if ( fields == null || fields.isEmpty()) { return null; }
        String[] parts = fields.split("[,;]");
        Set<String> selectedNames = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if ( o1 == null || o1.isEmpty() || 
                     o2 == null || o1.isEmpty()) { return -1; }
                String s1 = o1.toLowerCase();
                if ( s1.indexOf('_') > 0 ) {
                    s1 = s1.substring(0,  s1.lastIndexOf('_'));
                }
                String s2 = o2.toLowerCase();
                if ( s2.indexOf('_') > 0 ) {
                    s2 = s2.substring(0,  s2.lastIndexOf('_'));
                }
                return s1.compareTo(s2);
            }
        });
        for (String field : parts) {
            if ( field != null && ! field.isEmpty()) {
                selectedNames.add(field);
            }
        }
        return selectedNames;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
          List<String> filteredArgs = preprocessArgs(args); // sets system property and removes -D args
          if ( filteredArgs.contains("-v")) {
              int i = 0;
              System.out.println("Arguments:");
              for (String arg: args) {
                  System.out.println(i++ + " : " + arg);
              }
          }
          ApplicationConfiguration.Initialize("qcaas");
          logger = Logging.getLogger(QcClient.class);
          logger.debug("Running AdminClient");
          runCommand(filteredArgs.toArray(new String[filteredArgs.size()]));
      } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(-1);
      }
    }

}
