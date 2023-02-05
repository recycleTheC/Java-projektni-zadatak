/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.exceptions;

public class IBANValidationException extends RuntimeException {
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
