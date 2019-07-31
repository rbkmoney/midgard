package com.rbkmoney.midgard.service.clearing.handlers.failure;

public interface FailureTransactionHandler<T, D> {

    void handleTransaction(T transaction, D additionalInfo);

}
