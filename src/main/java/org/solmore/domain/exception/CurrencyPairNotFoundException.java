package org.solmore.domain.exception;

public class CurrencyPairNotFoundException extends RuntimeException {
    public CurrencyPairNotFoundException(String message) {
        super(message);
    }
}
