package com.rbkmoney.midgard.handler.preparing;

public interface ProcessTransactionHandler<T> {

    void handle(T trx, long clearingId, int providerId);

}
