/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import gov.noaa.pmel.tws.auth.HttpRequestValidator;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.ApplicationConfiguration.ConfigurationException;
import gov.noaa.pmel.tws.util.Logging;
import gov.noaa.pmel.tws.util.StringUtils;
import gov.noaa.pmel.tws.util.config.ConfigurationProperty;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author kamb
 *
 */
public class SignedMsgVerifier implements Filter {

    private static final String MODULE_NAME = "qcaas";
    
	private static Logger logger;
    private boolean doMessageValidation;
	
	@Override
	public void destroy() { // nothing to do
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
		HttpServletRequest hReq = (HttpServletRequest)request;
		HttpServletResponse hResp = (HttpServletResponse)response;
        if ( doMessageValidation ) {
    		logger.debug("verify message: " + hReq.getRequestURL().toString());
    		try {
    			HttpRequestValidator.authenticateRequest(hReq);
    		} catch (GeneralSecurityException e) {
    			e.printStackTrace();
    			logger.warn(e, e);
    			hResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    			return;
    		} catch (Exception e) {
    			e.printStackTrace();
    			logger.warn(e, e);
    			hResp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
    		}
        } else {
            logger.debug("Skipping message validation.");
        }
		chain.doFilter(hReq, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
        System.out.println(MODULE_NAME+": message verifier init");
        String envConfig = System.getProperty(MODULE_NAME+"config.dir");
        System.out.println(MODULE_NAME+": Environment-specified config dir: " + envConfig);
        try {
            if ( StringUtils.emptyOrNull(envConfig)) {
                System.out.println(MODULE_NAME+": using default config dir.");
                ApplicationConfiguration.Initialize(MODULE_NAME);
            } else {
                File configDir = new File(envConfig);
                System.out.println(MODULE_NAME+": using specified configuration dir: " + configDir.getAbsolutePath());
                if ( !configDir.exists()) { throw new IllegalStateException(MODULE_NAME+": configuration dir does not exist! Exiting."); }
                if ( !configDir.canRead()) { throw new IllegalStateException(MODULE_NAME+": unable to read configuration dir! Exiting."); }
                ApplicationConfiguration.Initialize(configDir, MODULE_NAME);
            }
        } catch (ConfigurationException ex) {
            logger.warn("Exception initializing application configuration:"+ ex + ". This may or may not be a problem.", ex);
        }
        logger = Logging.getLogger(SignedMsgVerifier.class);
		logger.debug("init");
        Logging.showLogFiles(logger);
        ConfigurationProperty validationProp = ApplicationConfiguration.getConfigurationProperty("qcaas.request.validate");
        if ( validationProp != null ) {
            doMessageValidation = Boolean.parseBoolean(validationProp.value());
            if ( doMessageValidation ) {
                logger.info("******* Do message validation: " + doMessageValidation + 
                            " set in property: " + validationProp);
            } else {
                logger.warn("******* Message validation OFF!");
                logger.warn("******* Message validation set in property: " + validationProp);
            }
        } else {
            logger.warn("******* No message validation configuration property set. " +
                        "Messages will be validated by default unless there are other configuration problems.");
        }
        if ( doMessageValidation && !HttpRequestValidator.checkConfiguration()) {
            System.err.println("HttpRequestValidator configuration problem.");
            throw new ServletException("Problem with HttpRequestValidator");
        }
	}

}
