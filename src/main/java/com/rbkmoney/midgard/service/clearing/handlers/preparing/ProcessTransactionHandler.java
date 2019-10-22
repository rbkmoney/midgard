package com.rbkmoney.midgard.service.clearing.handlers.preparing;

public interface ProcessTransactionHandler<T> {

    void handle(T trx, long clearingId, int providerId);

}
