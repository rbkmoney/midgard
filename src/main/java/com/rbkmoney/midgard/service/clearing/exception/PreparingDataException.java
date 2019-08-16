package com.rbkmoney.midgard.service.clearing.exception;

public class PreparingDataException extends RuntimeException {

    public PreparingDataException(String message) {
        super(message);
    }

    public PreparingDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public PreparingDataException(Throwable cause) {
        super(cause);
    }

    public PreparingDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
