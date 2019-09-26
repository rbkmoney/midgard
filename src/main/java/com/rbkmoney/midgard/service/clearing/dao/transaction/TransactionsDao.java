package com.rbkmoney.midgard.service.clearing.dao.transaction;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;

import java.util.List;

public interface TransactionsDao extends ClearingDao<ClearingTransaction, String> {

    ClearingTransaction getTransaction(String invoiceId, String paymentId, Integer trxVersion);

    void saveFailureTransaction(FailureTransaction failureTransaction);

    List<ClearingEventTransactionInfo> getClearingTransactionsByClearingId(Long clearingId, int providerId, long lastRowNumber, int limit);

    Integer getProcessedClearingTransactionCount(long clearingId);

    ClearingTransaction getLastTransaction();

    ClearingTransaction getLastActiveTransaction(int providerId);

    List<ClearingTransaction> getClearingTransactions(long lastSourceRowId, int providerId, int packageSize);

    void saveClearingEventTransactionInfo(ClearingEventTransactionInfo transactionInfo);

    void updateClearingTransactionState(String invoiceId, String paymentId, int version, long clearingId, TransactionClearingState state);

}
