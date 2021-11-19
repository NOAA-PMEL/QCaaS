package gov.noaa.pmel.qcaas;

public class QcServiceException extends Exception {

    /* default */
    private static final long serialVersionUID = 6516770681720718898L;

    public QcServiceException() { super(); }

    public QcServiceException(String message, Throwable cause) { super(message, cause); }

    public QcServiceException(String message) { super(message); }

    public QcServiceException(Throwable cause) { super(cause); }

}
