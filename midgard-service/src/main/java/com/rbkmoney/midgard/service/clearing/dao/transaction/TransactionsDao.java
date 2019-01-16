package com.rbkmoney.midgard.service.clearing.dao.transaction;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;

import java.util.List;

public interface TransactionsDao extends ClearingDao<ClearingTransaction> {

    ClearingTransaction getTransaction(String invoiceId, String paymentId) throws DaoException;

    void saveFailureTransaction(FailureTransaction failureTransaction) throws DaoException;

    List<ClearingEventTransactionInfo> getClearingTransactionsByClearingId(Long clearingId,
                                                                           int rowFrom,
                                                                           int rowTo) throws DaoException;

    Integer getProcessedClearingTransactionCount(long clearingId) throws DaoException;

    Long getLastTransactionEventId() throws DaoException;

}
