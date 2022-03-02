/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import gov.noaa.pmel.qcaas.qc.QcServiceIfc;
import gov.noaa.pmel.qcaas.qc.QcServiceImpl;
import gov.noaa.pmel.tws.util.ApplicationConfiguration;
import gov.noaa.pmel.tws.util.Logging;
import gov.noaa.pmel.tws.util.config.ConfigurationProperty;

/**
 * @author kamb
 *
 */
public class QcServiceFactory {

    private static final String DEFAULT_SERVICE_IMPL_CLASS = "gov.noaa.pmel.qcaas.qc.QcServiceImpl";
    
    private static Logger logger = Logging.getLogger(QcServiceFactory.class);

    /**
     * @param testName
     * @param qcRequest
     * @return
     */
    public static QcServiceIfc getQcService(String testName, QcInvocationRequest qcRequest) 
            throws IllegalStateException 
    {
        Class<?> serviceClass = getServiceClass(testName);
        try {
            QcServiceIfc impl;
            try {
                Constructor<QcServiceIfc> constructor = 
                    (Constructor<QcServiceIfc>) serviceClass.getConstructor(String.class);
                impl = constructor.newInstance(testName);
            } catch (NoSuchMethodException nome) {
                impl = (QcServiceIfc)serviceClass.newInstance();
            }
            logger.debug("Created service impl " + impl + " for test: " + testName);
            return impl;
        } catch (InstantiationException | IllegalAccessException  | 
                 IllegalArgumentException | InvocationTargetException iax) {
            throw new IllegalStateException("Exception creating QcService instance of " + serviceClass.getName(), iax);
        }
    }

    private static Class<?> getServiceClass(String testName) throws IllegalStateException {
        ConfigurationProperty serviceClassNameProprety = 
            ApplicationConfiguration.getConfigurationProperty("qcaas.qc.impl."+testName, 
                                                              DEFAULT_SERVICE_IMPL_CLASS);
        String serviceClassName = serviceClassNameProprety.value();
        logger.debug("Found service class " + serviceClassName + 
                     " for test " + testName + " from " + serviceClassNameProprety.source());
        try {
            Class<?> theClass = Class.forName(serviceClassName);
            if ( ! QcServiceIfc.class.isAssignableFrom(theClass)) {
                throw new IllegalStateException("Class " + serviceClassName + " does not implement QcServiceIfc.");
            }
            return theClass;
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Specified QcService Class " + serviceClassName + " not found!", ex);
        }
    }
}
