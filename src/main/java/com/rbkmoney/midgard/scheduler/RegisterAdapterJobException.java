package com.rbkmoney.midgard.scheduler;

public class RegisterAdapterJobException extends RuntimeException {

    public RegisterAdapterJobException() {
        super();
    }

    public RegisterAdapterJobException(String message) {
        super(message);
    }

    public RegisterAdapterJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegisterAdapterJobException(Throwable cause) {
        super(cause);
    }
}
