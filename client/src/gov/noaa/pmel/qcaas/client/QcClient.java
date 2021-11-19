/**
 * 
 */
package gov.noaa.pmel.qcaas.client;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gov.noaa.pmel.qcaas.DataRow;
import gov.noaa.pmel.qcaas.QcServiceData;
import gov.noaa.pmel.qcaas.QcWsUtils;
import gov.noaa.pmel.qcaas.StandardName;
import gov.noaa.pmel.qcaas.VariableDefinition;
import gov.noaa.pmel.qcaas.ws.QcInvocationRequest;
import gov.noaa.pmel.qcaas.ws.QcInvocationRequest.QcInvocationRequestBuilder;
import gov.noaa.pmel.tws.client.impl.TwsClientImpl.NoopException;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.StringUtils;
import gov.noaa.pmel.tws.util.cli.CLClient;
import gov.noaa.pmel.tws.util.cli.CLCommand;
import gov.noaa.pmel.tws.util.cli.CLOption;
import gov.noaa.pmel.tws.util.cli.CLOptionException;
import gov.noaa.pmel.tws.util.cli.CLOptionValue;
import gov.noaa.pmel.tws.util.cli.CLOptions;
import gov.noaa.pmel.tws.util.cli.CommandProcessor;

/**
 * @author kamb
 *
 */
public class QcClient extends CLClient {

    private static Logger logger;
    
    private static final String DEFAULT_SERVICE_URL = "http://localhost:8288/qcs/tws/qc";
    
    private static CLOption opt_dataFile = CLOption.builder().name("data_file").flag("f").longFlag("datafile")
            .description("path to the data file").build();
    private static CLOption opt_serviceUrl = CLOption.builder().name("service_url").flag("s").longFlag("service")
            .description("URL base for QC service.").build();
    private static CLOption opt_qcTest = CLOption.builder().name("qc_test").flag("t").longFlag("test")
            .description("Name of QC test to perform.").build();
    
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
                                                .option(opt_dataFile)
                                                .option(opt_serviceUrl)
                                                .option(opt_qcTest)
//                                                .option(opt_noop)
//                                                .option(opt_verbose)
                                                .build();

    private CLOptions _clOptions;
    
    private QcWsClient _wsClient;
    
    /**
     * 
     */
    public QcClient() {
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
            String serviceUrl = _clOptions.optionValue(opt_serviceUrl, DEFAULT_SERVICE_URL);
            URL serviceEndpoint = new URL(serviceUrl);
            
            _wsClient = QcWsClient.builder().serviceEndpoint(serviceEndpoint).build();
            
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


    /**
     * @param command
     * @return
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    private Method getProcessingMethod(CLCommand command) {
        String methodName = getMethodName(command);
        Method processingMethod = null;
        Class<?> thisClass = this.getClass();
        try {
            processingMethod = thisClass.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException nsx) {
//            Method[] thisMethods = thisClass.getDeclaredMethods();
            Class<?> superClass = thisClass.getSuperclass();
            if ( CommandProcessor.class.isAssignableFrom(superClass)) {
                try {
                    processingMethod = superClass.getDeclaredMethod(methodName);
                } catch (NoSuchMethodException ns2) {
//                    Method[] superMethods = superClass.getDeclaredMethods();
                    throw new IllegalStateException("No processing method \"" + methodName + 
                                                    "\" found for command " + command.command() +
                                                    " in either " + thisClass.getName() + " or " + 
                                                    superClass.getName()); 
                }
            } else {
                throw new IllegalStateException("No processing method \"" + methodName + 
                                                "\" found for command " + command +
                                                " in " + thisClass.getName());
            }
        }
        return processingMethod;
    }

    /**
     * @param command
     * @return
     */
    private static String getMethodName(CLCommand command) {
        String methodName = command.methodName();
        if ( methodName == null ) {
            String commandName = command.command();
            char[] chars = ("do"+commandName).toCharArray();
            chars[2] = String.valueOf(chars[2]).toUpperCase().charAt(0);
            methodName = String.valueOf(chars);
        }
        return methodName;
    }
    
    public void doQc() throws Exception {
        logger.info("doQc");
        logger.debug(_clOptions);
        String test = _clOptions.get(opt_qcTest);
        QcInvocationRequest request = buildInvocationRequest();
        _wsClient.invokeQc(test, request);
    }

    /**
     * @return
     * @throws Exception 
     */
    private QcInvocationRequest buildInvocationRequest() throws Exception {
        QcInvocationRequestBuilder req = QcInvocationRequest.builder();
        String fileName = _clOptions.get(opt_dataFile);
        File dataFile = new File(fileName);
        req.data(QcWsUtils.readFileData(dataFile));
        return req.build();
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
          logger = LogManager.getLogger(QcClient.class);
          logger.debug("Running AdminClient");
          runCommand(filteredArgs.toArray(new String[filteredArgs.size()]));
      } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(-1);
      }
    }

}
