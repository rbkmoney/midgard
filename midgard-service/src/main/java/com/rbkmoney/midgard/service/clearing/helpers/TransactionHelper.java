package com.rbkmoney.midgard.service.clearing.helpers;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.midgard.enums.ClearingTrxType;
import org.jooq.generated.midgard.tables.pojos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionHelper {

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ClearingCashFlowDao cashFlowDao;

    @Value("${clearing-service.package-size}")
    private int packageSize;

    public void saveTransaction(Payment payment) {
        ClearingTransaction transaction = Utils.transformTransaction(payment);
        log.debug("Saving a transaction {}...", transaction);
        transactionsDao.save(transaction);
    }

    public ClearingTransaction getTransaction(String transactionId) {
        return transactionsDao.get(transactionId);
    }

    public ClearingTransaction getTransaction(String invoiceId, String paymentId) {
        return transactionsDao.getTransaction(invoiceId, paymentId);
    }

    public ClearingRefund getRefundTransaction(String transactionId) {
        return clearingRefundDao.getRefund(transactionId);
    }

    public ClearingDataPackage getClearingTransactionPackage(Long clearingId, int packageNumber) {
        List<ClearingTransactionEventInfo> trxEventInfo = getActualClearingTransactionsInfo(clearingId, packageNumber);
        ClearingDataPackage dataPackage = new ClearingDataPackage();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(packageNumber);
        dataPackage.setFinalPackage(trxEventInfo.size() == packageSize ? false : true);

        List<Transaction> transactions = new ArrayList<>();
        for (ClearingTransactionEventInfo info : trxEventInfo) {
            if (info.getTransactionType().equals(ClearingTrxType.PAYMENT)) {
                ClearingTransaction clearingTransaction = getTransaction(info.getTransactionId());
                List<ClearingTransactionCashFlow> cashFlowList =
                        cashFlowDao.get(clearingTransaction.getEventId().toString());
                transactions.add(Utils.transformClearingTransaction(clearingTransaction, cashFlowList));
            } else if (info.getTransactionType().equals(ClearingTrxType.REFUND)) {
                ClearingRefund refund = getRefundTransaction(info.getTransactionId());
                ClearingTransaction clearingTransaction = getTransaction(refund.getInvoiceId(), refund.getPaymentId());
                List<ClearingTransactionCashFlow> cashFlowList =
                        cashFlowDao.get(clearingTransaction.getEventId().toString());
                transactions.add(Utils.transformRefundTransaction(clearingTransaction, cashFlowList, refund));
            }
        }

        dataPackage.setTransactions(transactions);
        return dataPackage;
    }

    private List<ClearingTransactionEventInfo> getActualClearingTransactionsInfo(Long clearingId,
                                                                                 int packageNumber) {
        int rowFrom = packageNumber * packageSize;
        int rowTo = rowFrom + packageSize;
        return transactionsDao.getClearingTransactionsByClearingId(clearingId, rowFrom, rowTo);
    }

    public int getClearingTransactionPackagesCount(long clearingId) {
        int packagesCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);
        return (int) Math.floor((double) packagesCount / packageSize);
    }

    public long getLastTransactionEventId() {
        Long eventId = transactionsDao.getLastTransactionEventId();
        if (eventId == null) {
            log.warn("Event ID for clearing transactions was not found!");
            return 0L;
        } else {
            return eventId;
        }
    }

    public void saveFailureTransactions(long clearingEventId, List<FailureTransactionData> failureTransactions) {
        for (FailureTransactionData failureTransaction : failureTransactions) {
            saveFailureTransaction(clearingEventId, failureTransaction.getTransactionId(),
                    failureTransaction.getComment());
        }
    }

    private void saveFailureTransaction(Long clearingId, String transactionId, String reason) {
        FailureTransaction failureTransaction = new FailureTransaction();
        failureTransaction.setTransactionId(transactionId);
        failureTransaction.setClearingId(clearingId);
        failureTransaction.setReason(reason);
        transactionsDao.saveFailureTransaction(failureTransaction);
    }

}
