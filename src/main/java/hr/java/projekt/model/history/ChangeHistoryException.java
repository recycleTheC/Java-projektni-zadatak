package hr.java.projekt.model.history;

public class ChangeHistoryException extends RuntimeException{
    public ChangeHistoryException() {
    }

    public ChangeHistoryException(String message) {
        super(message);
    }

    public ChangeHistoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChangeHistoryException(Throwable cause) {
        super(cause);
    }

    public ChangeHistoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
