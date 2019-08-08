package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.ClearingDataRequest;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.handlers.failure.FailureTransactionHandler;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClearingTransactionPackageHandler implements ClearingPackageHandler {

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ClearingCashFlowDao cashFlowDao;

    private final FailureTransactionHandler<ClearingEventTransactionInfo, String> serviceFailureTransactionHandler;

    @Value("${clearing-service.package-size}")
    private int packageSize;

    @Override
    public ClearingDataRequest getClearingPackage(Long clearingId, int packageNumber) {
        log.info("Start processing the package {} for clearing event {}", packageNumber, clearingId);
        List<ClearingEventTransactionInfo> trxEventInfo = getActualClearingTransactionsInfo(clearingId, packageNumber);
        ClearingDataRequest dataPackage = new ClearingDataRequest();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(packageNumber + 1);
        dataPackage.setFinalPackage(trxEventInfo.size() != packageSize);
        dataPackage.setTransactions(getTransactionList(trxEventInfo, clearingId, packageNumber));
        log.info("Finish processing the package {} for clearing event {}", packageNumber, clearingId);
        return dataPackage;
    }

    private List<Transaction> getTransactionList(List<ClearingEventTransactionInfo> trxEventInfo,
                                                 Long clearingId,
                                                 int packageNumber) {
        List<Transaction> transactions = new ArrayList<>();
        for (ClearingEventTransactionInfo info : trxEventInfo) {
            try {
                transactions.add(getTransaction(info, clearingId, packageNumber));
            } catch (Throwable th) {
                serviceFailureTransactionHandler.handleTransaction(info, th.getMessage());
            }
        }
        return transactions;
    }

    private Transaction getTransaction(ClearingEventTransactionInfo info, Long clearingId, int packageNumber)
            throws Exception {
        switch (info.getTransactionType()) {
            case PAYMENT:
                return getClearingPayment(info, clearingId, packageNumber);
            case REFUND:
                return getClearingRefund(info, clearingId, packageNumber);
            default:
                throw new Exception("Transaction type " + info.getTransactionType() + " not found");
        }
    }

    private Transaction getClearingPayment(ClearingEventTransactionInfo info, Long clearingId, int packageNumber) {
        ClearingTransaction clearingTransaction = transactionsDao.getTransaction(info.getInvoiceId(), info.getPaymentId());
        log.info("Transaction with invoice id {} and payment id {} will added to package {} " +
                        "for clearing event {}", clearingTransaction.getInvoiceId(), clearingTransaction.getPaymentId(),
                packageNumber, clearingId);
        List<ClearingTransactionCashFlow> cashFlowList =
                cashFlowDao.get(clearingTransaction.getSequenceId());
        return MappingUtils.transformClearingTransaction(clearingTransaction, cashFlowList);
    }

    private Transaction getClearingRefund(ClearingEventTransactionInfo info, Long clearingId, int packageNumber) {
        ClearingRefund refund = clearingRefundDao.getRefund(info.getInvoiceId(), info.getPaymentId(), info.getRefundId());
        log.info("Refund transaction with invoice id {}, payment id {} and refund id {} will added to package {} " +
                        "for clearing event {}", refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId(),
                packageNumber, clearingId);
        ClearingTransaction clearingTransaction =
                transactionsDao.getTransaction(refund.getInvoiceId(), refund.getPaymentId());
        List<ClearingTransactionCashFlow> cashFlowList = cashFlowDao.get(refund.getSequenceId());
        return MappingUtils.transformRefundTransaction(clearingTransaction, cashFlowList, refund);
    }

    private List<ClearingEventTransactionInfo> getActualClearingTransactionsInfo(Long clearingId, int packageNumber) {
        int rowFrom = packageNumber * packageSize;
        int rowTo = rowFrom + packageSize;
        return transactionsDao.getClearingTransactionsByClearingId(clearingId, rowFrom, rowTo);
    }

}
