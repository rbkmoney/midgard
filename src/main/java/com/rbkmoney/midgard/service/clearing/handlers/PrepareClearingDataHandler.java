package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.service.clearing.handlers.failure.FailureTransactionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.enums.ClearingTrxType;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.rbkmoney.midgard.service.clearing.utils.ClearingEventUtils.getLastRowId;

@Slf4j
@RequiredArgsConstructor
@Component
public class PrepareClearingDataHandler implements Handler<ClearingProcessingEvent> {

    private final ClearingEventInfoDao clearingEventInfoDao;

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    private final FailureTransactionHandler<ClearingEventTransactionInfo, String> serviceFailureTransactionHandler;

    @Value("${clearing-service.inner-package-size}")
    private int innerPackageSize;

    @Override
    @Transactional
    public void handle(ClearingProcessingEvent clearingEvent) {
        int providerId = clearingEvent.getClearingAdapter().getAdapterId();
        Long clearingId = clearingEvent.getClearingId();
        try {
            log.info("Preparing event for provider id {} started", providerId);

            prepareTransactionData(clearingId, providerId);

            log.info("Preparing event {} for provider id {} was finished", clearingId, providerId);
        } catch (Exception ex) {
            log.error("Error received while preparing clearing data", ex);
        }
    }

    private void prepareTransactionData(long clearingId, int providerId) {
        prepareClearingTransactions(clearingId, providerId);
        prepareClearingRefunds(clearingId, providerId);
        prepareAdapterFaultClearingEvent(clearingId, providerId);
    }

    private void prepareClearingTransactions(long clearingId, int providerId) {
        List<ClearingTransaction> clearingTransactions;
        do {
            ClearingTransaction lastActiveTransaction = transactionsDao.getLastActiveTransaction(providerId);
            clearingTransactions = transactionsDao.getClearingTransactions(
                    lastActiveTransaction == null ? 0 : lastActiveTransaction.getSourceRowId(),
                    providerId,
                    innerPackageSize
            );
            processClearingTransactions(clearingTransactions, clearingId, providerId);
        } while (clearingTransactions != null && clearingTransactions.size() == innerPackageSize);
    }

    private void processClearingTransactions(List<ClearingTransaction> clearingTransactions,
                                             long clearingId,
                                             int providerId) {
        for (ClearingTransaction trx : clearingTransactions) {
            try {
                ClearingEventTransactionInfo transactionInfo =
                        transformClearingTrx(clearingId, providerId, trx);
                transactionsDao.saveClearingEventTransactionInfo(transactionInfo);
                transactionsDao.updateClearingTransactionState(
                        trx.getInvoiceId(),
                        trx.getPaymentId(),
                        trx.getTrxVersion(),
                        clearingId,
                        TransactionClearingState.ACTIVE
                );
            } catch (Exception ex) {
                log.error("Error received while processing payment transaction with invoice id '{}', " +
                                "payment id '{}' and version '{}'", trx.getInvoiceId(), trx.getPaymentId(),
                        trx.getTrxVersion(), ex);
                serviceFailureTransactionHandler.handleTransaction(
                        transformClearingTrx(clearingId, providerId, trx),
                        "Failure in preparing stage. Stacktrace: \n" + ex.getStackTrace()
                );
            }
        }
    }

    private void prepareClearingRefunds(long clearingId, int providerId) {
        List<ClearingRefund> clearingRefunds;
        do {
            ClearingRefund lastActiveRefund = clearingRefundDao.getLastActiveRefund();
            clearingRefunds = clearingRefundDao.getClearingTransactions(
                    lastActiveRefund == null ? 0 : lastActiveRefund.getSourceRowId(),
                    innerPackageSize
            );
            processClearingRefunds(clearingRefunds, clearingId, providerId);
        } while (clearingRefunds != null && clearingRefunds.size() == innerPackageSize);
    }

    private void processClearingRefunds(List<ClearingRefund> clearingRefunds, long clearingId, int providerId) {
        for (ClearingRefund refund : clearingRefunds) {
            try {
                ClearingEventTransactionInfo refundInfo =
                        transformClearingRefund(clearingId, providerId, refund);
                transactionsDao.saveClearingEventTransactionInfo(refundInfo);
                clearingRefundDao.updateClearingRefundState(
                        refund.getInvoiceId(),
                        refund.getPaymentId(),
                        refund.getRefundId(),
                        refund.getTrxVersion(),
                        clearingId,
                        providerId,
                        TransactionClearingState.ACTIVE
                );
            } catch (Exception ex) {
                log.error("Error received while processing refund transaction with invoice id '{}', " +
                                "payment id '{}', refund id '{}' and version '{}'", refund.getInvoiceId(),
                        refund.getPaymentId(), refund.getRefundId(), refund.getTrxVersion(), ex);
                serviceFailureTransactionHandler.handleTransaction(
                        transformClearingRefund(clearingId, providerId, refund),
                        "Failure in preparing stage. Stacktrace: \n" + ex.getStackTrace()
                );
            }
        }
    }

    private void prepareAdapterFaultClearingEvent(long clearingId, int providerId) {
        List<ClearingEventInfo> adapterFaultClearingEvents =
                clearingEventInfoDao.getAllClearingEventsForProviderByStatus(providerId, ClearingEventStatus.ADAPTER_FAULT);
        for (ClearingEventInfo clearingEvent : adapterFaultClearingEvents) {
            int trxCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);
            if (trxCount == 0) {
                continue;
            }
            long lastRowId = 0L;
            List<ClearingEventTransactionInfo> failureTransactions;
            do {
                failureTransactions = transactionsDao.getClearingTransactionsByClearingId(
                        clearingEvent.getId(),
                        providerId,
                        lastRowId,
                        innerPackageSize
                );
                for (ClearingEventTransactionInfo failureTransaction : failureTransactions) {
                    failureTransaction.setClearingId(clearingId);
                    transactionsDao.saveClearingEventTransactionInfo(failureTransaction);
                }

                lastRowId = getLastRowId(failureTransactions);
            } while (failureTransactions != null && failureTransactions.size() == innerPackageSize);
        }
    }

    private ClearingEventTransactionInfo transformClearingTrx(long clearingId,
                                                              int providerId,
                                                              ClearingTransaction trx) {
        ClearingEventTransactionInfo eventTrxInfo = new ClearingEventTransactionInfo();
        eventTrxInfo.setClearingId(clearingId);
        eventTrxInfo.setTransactionType(ClearingTrxType.PAYMENT);
        eventTrxInfo.setInvoiceId(trx.getInvoiceId());
        eventTrxInfo.setPaymentId(trx.getPaymentId());
        eventTrxInfo.setTransactionId(trx.getTransactionId());
        eventTrxInfo.setTrxVersion(trx.getTrxVersion());
        eventTrxInfo.setRowNumber(trx.getSourceRowId());
        eventTrxInfo.setProviderId(providerId);
        return eventTrxInfo;
    }

    private ClearingEventTransactionInfo transformClearingRefund(long clearingId,
                                                                 int providerId,
                                                                 ClearingRefund refund) {
        ClearingEventTransactionInfo eventTrxInfo = new ClearingEventTransactionInfo();
        eventTrxInfo.setClearingId(clearingId);
        eventTrxInfo.setTransactionType(ClearingTrxType.REFUND);
        eventTrxInfo.setInvoiceId(refund.getInvoiceId());
        eventTrxInfo.setPaymentId(refund.getPaymentId());
        eventTrxInfo.setRefundId(refund.getRefundId());
        eventTrxInfo.setTransactionId(refund.getTransactionId());
        eventTrxInfo.setTrxVersion(refund.getTrxVersion());
        eventTrxInfo.setRowNumber(refund.getSourceRowId());
        eventTrxInfo.setProviderId(providerId);
        return eventTrxInfo;
    }

}
