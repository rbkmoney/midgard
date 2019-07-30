package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.enums.ClearingTrxType;
import org.jooq.generated.midgard.tables.pojos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.generated.midgard.enums.ClearingTrxType.PAYMENT;
import static org.jooq.generated.midgard.enums.ClearingTrxType.REFUND;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingDataTransferHandler implements Handler<ClearingProcessingEvent> {

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ClearingCashFlowDao cashFlowDao;

    private final ClearingEventInfoDao eventInfoDao;

    @Value("${clearing-service.package-size}")
    private int packageSize;

    private static final int INIT_PACKAGE_NUMBER = 0;

    @Override
    public void handle(ClearingProcessingEvent event) throws Exception {
        Long clearingId = event.getClearingId();
        log.info("Transfer data to clearing adapter {} with clearing id {} get started",
                event.getClearingAdapter().getAdapterName(), clearingId);
        try {
            ClearingAdapterSrv.Iface adapter = event.getClearingAdapter().getAdapter();
            String uploadId = adapter.startClearingEvent(clearingId);
            int packagesCount = getClearingTransactionPackagesCount(clearingId);
            log.info("Total number of packages for the clearing event {}: {}", clearingId, packagesCount);

            List<ClearingDataPackageTag> tagList = new ArrayList<>();
            if (packagesCount == 0) {
                log.info("No transactions found for clearing");
                ClearingDataRequest request = getEmptyClearingDataPackage(clearingId);
                ClearingDataResponse response = adapter.sendClearingDataPackage(uploadId, request);
                tagList.add(response.getClearingDataPackageTag());
            } else {
                for (int packageNumber = INIT_PACKAGE_NUMBER; packageNumber < packagesCount; packageNumber++) {
                    ClearingDataRequest request = getClearingTransactionPackage(clearingId, packageNumber);
                    ClearingDataResponse response = adapter.sendClearingDataPackage(uploadId, request);
                    processAdapterFailureTransactions(response.getFailureTransactions(), clearingId);
                    tagList.add(response.getClearingDataPackageTag());
                }
            }

            adapter.completeClearingEvent(uploadId, clearingId, tagList);
            eventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.COMPLETE);
            log.info("Transfer data to clearing adapter {} with clearing id {} was finished",
                    event.getClearingAdapter().getAdapterName(), clearingId);
        } catch (ClearingAdapterException ex) {
            log.error("Error occurred while processing clearing event {}", clearingId, ex);
            eventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.ADAPTER_FAULT);
            throw ex;
        } catch (TException ex) {
            log.error("Data transfer error while processing clearing event {}", clearingId, ex);
            eventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.ADAPTER_FAULT);
            throw new Exception(ex);
        }
    }

    private ClearingDataRequest getClearingTransactionPackage(Long clearingId, int packageNumber) {
        List<ClearingEventTransactionInfo> trxEventInfo = getActualClearingTransactionsInfo(clearingId, packageNumber);
        ClearingDataRequest dataPackage = new ClearingDataRequest();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(packageNumber + 1);
        dataPackage.setFinalPackage(trxEventInfo.size() == packageSize ? false : true);
        dataPackage.setTransactions(getTransactionList(trxEventInfo, clearingId, packageNumber));
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
                //TODO: на резонный вопрос почему throwable отвечаю - по качану
                processFailureTransaction(info, th);
            }
        }
        return transactions;
    }

    private void processAdapterFailureTransactions(List<Transaction> failureTransactions, Long clearingId) {
        if (failureTransactions != null) {
            failureTransactions.forEach(transaction -> processAdapterFailureTransaction(transaction, clearingId));
        }
    }

    private void processAdapterFailureTransaction(Transaction transaction, Long clearingId) {
        try {
            FailureTransaction failureTransaction = getFailureTransaction(transaction, clearingId);
            log.error("Error transaction was received from a clearing adapter for clearing event {}. " +
                    "Transaction info: {}", clearingId, failureTransaction);
            transactionsDao.saveFailureTransaction(failureTransaction);
        } catch (Exception ex) {
            log.error("Received error when processing failure transaction", ex);
        }
    }

    private void processFailureTransaction(ClearingEventTransactionInfo info, Throwable th) {
        try {
            saveFailureTransaction(info, th);
        } catch (Exception ex) {
            log.error("Received error when processing failure transaction", ex);
        }
    }

    private void saveFailureTransaction(ClearingEventTransactionInfo info, Throwable th) throws Exception {
        switch (info.getTransactionType()) {
            case PAYMENT:
                log.error("Error was caught while clearing processed {} transaction with invoice_id {} and payment id {}",
                        info.getTransactionType(), info.getInvoiceId(), info.getPaymentId());
                transactionsDao.saveFailureTransaction(getFailureTransaction(info, th, PAYMENT));
                break;
            case REFUND:
                log.error("Error was caught while clearing processed {} transaction with invoice_id {}, payment id {} " +
                                "and refund id {}", info.getTransactionType(), info.getInvoiceId(), info.getPaymentId(),
                        info.getRefundId());
                transactionsDao.saveFailureTransaction(getFailureTransaction(info, th, REFUND));
                break;
            default:
                throw new Exception("Transaction type " + info.getTransactionType() + " not found");
        }
    }

    private FailureTransaction getFailureTransaction(Transaction transaction, Long clearingId) {
        FailureTransaction failureTransaction = new FailureTransaction();
        GeneralTransactionInfo transactionInfo = transaction.getGeneralTransactionInfo();
        failureTransaction.setClearingId(clearingId);
        failureTransaction.setTransactionId(transactionInfo.getTransactionId());
        failureTransaction.setInvoiceId(transactionInfo.getInvoiceId());
        failureTransaction.setPaymentId(transactionInfo.getPaymentId());
        failureTransaction.setErrorReason(transaction.getComment());
        failureTransaction.setTransactionType(ClearingTrxType.valueOf(transactionInfo.getTransactionType()));
        return failureTransaction;
    }

    private FailureTransaction getFailureTransaction(ClearingEventTransactionInfo info,
                                                     Throwable th,
                                                     ClearingTrxType type) {
        FailureTransaction failureTransaction = new FailureTransaction();
        failureTransaction.setClearingId(info.getClearingId());
        failureTransaction.setTransactionId(info.getTransactionId());
        failureTransaction.setInvoiceId(info.getInvoiceId());
        failureTransaction.setPaymentId(info.getPaymentId());
        failureTransaction.setErrorReason(th.getMessage());
        failureTransaction.setTransactionType(type);
        return failureTransaction;
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

    private int getClearingTransactionPackagesCount(long clearingId) {
        int packagesCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);
        return (int) Math.ceil((double) packagesCount / packageSize);
    }

    private ClearingDataRequest getEmptyClearingDataPackage(Long clearingId) {
        ClearingDataRequest dataPackage = new ClearingDataRequest();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(1);
        dataPackage.setFinalPackage(true);
        dataPackage.setTransactions(new ArrayList<>());
        return dataPackage;
    }

}
