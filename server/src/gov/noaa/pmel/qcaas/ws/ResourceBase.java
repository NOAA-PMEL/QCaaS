/**
 * 
 */
package gov.noaa.pmel.qcaas.ws;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 * @author kamb
 *
 */
public abstract class ResourceBase {

    protected static String dumpRequest(HttpServletRequest request, String ... parameterNameValuePairs) {
        StringBuffer b = new StringBuffer(dumpRequest(request));
        int npv = parameterNameValuePairs.length;
        if ( npv > 0 ) { b.append(" params: "); }
        for (int i = 0; i < npv; i += 2) {
            b.append(parameterNameValuePairs[i]).append("=")
             .append((i<npv-1 ? parameterNameValuePairs[i+1] : "N/A"));
        }
        return b.toString();
    }
    protected static String dumpRequest(HttpServletRequest request) {
        StringBuffer b = new StringBuffer(request.getRequestURL());
        if ( request.getQueryString() != null ) {
            b.append("?").append(request.getQueryString());
        }
        b.append(" from: " ).append(request.getRemoteAddr());
        return b.toString();
    }

    protected static String fullDump(HttpServletRequest request) {
        StringBuffer b = new StringBuffer(dumpRequest(request));
        b.append("(").append(request.getRemoteHost()).append(")\n");
        Enumeration<String>headers = request.getHeaderNames();
        while ( headers.hasMoreElements()) {
            String name = headers.nextElement();
            String value = request.getHeader(name);
            b.append(name).append(":").append(value).append("\n");
        }
        return b.toString();
    }
}
