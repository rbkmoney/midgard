package com.rbkmoney.midgard.service.clearing.helpers.transaction;

import com.rbkmoney.midgard.ClearingDataPackage;
import com.rbkmoney.midgard.FailureTransactionData;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;

import java.util.List;

public interface TransactionHelper {

    void saveTransaction(Payment payment);

    ClearingTransaction getTransaction(String transactionId);

    ClearingTransaction getTransaction(String invoiceId, String paymentId);

    ClearingRefund getRefundTransaction(String transactionId);

    ClearingDataPackage getClearingTransactionPackage(Long clearingId, int packageNumber);

    int getClearingTransactionPackagesCount(long clearingId);

    long getLastTransactionEventId();

    void saveFailureTransactions(long clearingEventId, List<FailureTransactionData> failureTransactions);

}
