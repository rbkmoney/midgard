package com.rbkmoney.midgard.handler.failure;

public interface FailureTransactionHandler<T, D> {

    void handleTransaction(T transaction, D additionalInfo);

}
