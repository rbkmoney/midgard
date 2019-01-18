package com.rbkmoney.midgard.service.clearing.exception;

public class AdapterNotFoundException extends RuntimeException {

    public AdapterNotFoundException(String message) {
        super(message);
    }

    public AdapterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdapterNotFoundException(Throwable cause) {
        super(cause);
    }

    public AdapterNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
