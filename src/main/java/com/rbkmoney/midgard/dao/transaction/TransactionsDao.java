package com.rbkmoney.midgard.dao.transaction;

import com.rbkmoney.midgard.dao.ClearingDao;
import org.jooq.generated.enums.TransactionClearingState;
import org.jooq.generated.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.tables.pojos.ClearingTransaction;
import org.jooq.generated.tables.pojos.FailureTransaction;

import java.util.List;

public interface TransactionsDao extends ClearingDao<ClearingTransaction, String> {

    ClearingTransaction getTransaction(String invoiceId, String paymentId, Integer trxVersion);

    void saveFailureTransaction(FailureTransaction failureTransaction);

    List<ClearingEventTransactionInfo> getClearingTransactionsByClearingId(
            Long clearingId, int providerId, long lastRowNumber, int limit
    );

    Integer getProcessedClearingTransactionCount(long clearingId);

    ClearingTransaction getLastTransaction();

    List<ClearingTransaction> getReadyClearingTransactions(int providerId, int packageSize);

    void saveClearingEventTransactionInfo(ClearingEventTransactionInfo transactionInfo);

    void updateClearingTransactionState(String invoiceId, String paymentId, int version,
                                        long clearingId, TransactionClearingState state);

}
