package com.rbkmoney.midgard.service.clearing.dao.transaction;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;

import java.util.List;

public interface TransactionsDao extends ClearingDao<ClearingTransaction, String> {

    ClearingTransaction getTransaction(String invoiceId, String paymentId);

    void saveFailureTransaction(FailureTransaction failureTransaction);

    List<ClearingEventTransactionInfo> getClearingTransactionsByClearingId(Long clearingId, int rowFrom, int rowTo);

    Integer getProcessedClearingTransactionCount(long clearingId);

    ClearingTransaction getLastTransaction();

}
