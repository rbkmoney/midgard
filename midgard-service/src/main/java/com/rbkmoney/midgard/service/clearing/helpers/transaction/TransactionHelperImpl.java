package com.rbkmoney.midgard.service.clearing.helpers.transaction;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
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
public class TransactionHelperImpl implements TransactionHelper {

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ClearingCashFlowDao cashFlowDao;

    @Value("${clearing-service.package-size}")
    private int packageSize;

    @Override
    public void saveTransaction(Payment payment) {
        ClearingTransaction transaction = MappingUtils.transformTransaction(payment);
        log.debug("Saving a transaction {}", transaction);
        transactionsDao.save(transaction);
    }

    @Override
    public ClearingTransaction getTransaction(String transactionId) {
        return transactionsDao.get(transactionId);
    }

    @Override
    public ClearingTransaction getTransaction(String invoiceId, String paymentId) {
        return transactionsDao.getTransaction(invoiceId, paymentId);
    }

    @Override
    public ClearingRefund getRefundTransaction(String transactionId) {
        return clearingRefundDao.getRefund(transactionId);
    }

    @Override
    public ClearingDataPackage getClearingTransactionPackage(Long clearingId, int packageNumber) {
        List<ClearingTransactionEventInfo> trxEventInfo = getActualClearingTransactionsInfo(clearingId, packageNumber);
        ClearingDataPackage dataPackage = new ClearingDataPackage();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(packageNumber + 1);
        dataPackage.setFinalPackage(trxEventInfo.size() == packageSize ? false : true);

        List<Transaction> transactions = new ArrayList<>();
        for (ClearingTransactionEventInfo info : trxEventInfo) {
            if (info.getTransactionType().equals(ClearingTrxType.PAYMENT)) {
                ClearingTransaction clearingTransaction = getTransaction(info.getTransactionId());
                List<ClearingTransactionCashFlow> cashFlowList =
                        cashFlowDao.get(clearingTransaction.getEventId().toString());
                transactions.add(MappingUtils.transformClearingTransaction(clearingTransaction, cashFlowList));
            } else if (info.getTransactionType().equals(ClearingTrxType.REFUND)) {
                ClearingRefund refund = getRefundTransaction(info.getTransactionId());
                ClearingTransaction clearingTransaction = getTransaction(refund.getInvoiceId(), refund.getPaymentId());
                List<ClearingTransactionCashFlow> cashFlowList =
                        cashFlowDao.get(clearingTransaction.getEventId().toString());
                transactions.add(MappingUtils.transformRefundTransaction(clearingTransaction, cashFlowList, refund));
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

    @Override
    public int getClearingTransactionPackagesCount(long clearingId) {
        int packagesCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);
        return (int) Math.floor((double) packagesCount / packageSize);
    }

    @Override
    public long getLastTransactionEventId() {
        Long eventId = transactionsDao.getLastTransactionEventId();
        if (eventId == null) {
            log.warn("Event ID for clearing transactions was not found!");
            return 0L;
        } else {
            return eventId;
        }
    }

    @Override
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
