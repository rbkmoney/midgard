package com.rbkmoney.midgard.dao.transaction;

import com.rbkmoney.midgard.dao.ClearingDao;
import com.rbkmoney.midgard.domain.enums.TransactionClearingState;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventTransactionInfo;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingTransaction;
import com.rbkmoney.midgard.domain.tables.pojos.FailureTransaction;

import java.util.List;

public interface TransactionsDao extends ClearingDao<ClearingTransaction, String> {

    ClearingTransaction getTransaction(String invoiceId, String paymentId, Integer trxVersion);

    void saveFailureTransaction(FailureTransaction failureTransaction);

    List<ClearingEventTransactionInfo> getClearingTransactionsByClearingId(
            Long clearingId, int providerId, long lastRowNumber, int limit
    );

    Integer getProcessedClearingTransactionCount(long clearingId, int providerId);

    ClearingTransaction getLastTransaction();

    List<ClearingTransaction> getReadyClearingTransactions(int providerId, int packageSize);

    void saveClearingEventTransactionInfo(ClearingEventTransactionInfo transactionInfo);

    void updateClearingTransactionState(String invoiceId, String paymentId, int version,
                                        long clearingId, TransactionClearingState state);

}
