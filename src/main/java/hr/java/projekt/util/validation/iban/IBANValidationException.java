package hr.java.projekt.util.validation.iban;

public class IBANValidationException extends Exception{
    public IBANValidationException() {
    }

    public IBANValidationException(String message) {
        super(message);
    }

    public IBANValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IBANValidationException(Throwable cause) {
        super(cause);
    }

    public IBANValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
