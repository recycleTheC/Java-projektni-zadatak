package hr.java.projekt.util.validation.oib;

public class OIBValidationException extends Exception {
    public OIBValidationException() {
    }

    public OIBValidationException(String message) {
        super(message);
    }

    public OIBValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public OIBValidationException(Throwable cause) {
        super(cause);
    }

    public OIBValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
