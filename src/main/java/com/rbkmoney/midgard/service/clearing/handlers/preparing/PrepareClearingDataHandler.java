package com.rbkmoney.midgard.service.clearing.handlers.preparing;

import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.service.clearing.handlers.Handler;
import com.rbkmoney.midgard.service.clearing.handlers.failure.FailureTransactionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.rbkmoney.midgard.service.clearing.utils.ClearingEventUtils.getLastRowId;
import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.transformClearingRefund;
import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.transformClearingTrx;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrepareClearingDataHandler implements Handler<ClearingProcessingEvent> {

    private final ClearingEventInfoDao clearingEventInfoDao;

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ProcessTransactionHandler<ClearingTransaction> processPaymentTransactionHandler;

    private final ProcessTransactionHandler<ClearingRefund> processRefundTransactionHandler;

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

            prepareClearingTransactions(clearingId, providerId);
            prepareClearingRefunds(clearingId, providerId);
            prepareAdapterFaultClearingEvent(clearingId, providerId);

            log.info("Preparing event {} for provider id {} was finished", clearingId, providerId);
        } catch (Exception ex) {
            log.error("Error received while preparing clearing data", ex);
        }
    }

    private void prepareClearingTransactions(long clearingId, int providerId) {
        log.info("Start preparing clearing transactions for provider id {} with clearing id {}",
                providerId, clearingId);
        List<ClearingTransaction> clearingTransactions;
        do {
            clearingTransactions = transactionsDao.getReadyClearingTransactions(providerId, innerPackageSize);
            clearingTransactions.forEach(trx -> processClearingTransaction(trx, clearingId, providerId));
        } while (clearingTransactions != null && clearingTransactions.size() == innerPackageSize);
        log.info("Finish preparing clearing transactions for provider id {} with clearing id {}",
                providerId, clearingId);
    }

    private void processClearingTransaction(ClearingTransaction trx, long clearingId, int providerId) {
        try {
            processPaymentTransactionHandler.handle(trx, clearingId, providerId);
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

    private void prepareClearingRefunds(long clearingId, int providerId) {
        List<ClearingRefund> clearingRefunds;
        do {
            clearingRefunds = clearingRefundDao.getReadyClearingRefunds(providerId, innerPackageSize);
            clearingRefunds.forEach(refund -> processClearingRefund(refund, clearingId, providerId));
        } while (clearingRefunds != null && clearingRefunds.size() == innerPackageSize);
    }

    private void processClearingRefund(ClearingRefund refund, long clearingId, int providerId) {
        try {
            processRefundTransactionHandler.handle(refund, clearingId, providerId);
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

    private void prepareAdapterFaultClearingEvent(long clearingId, int providerId) {
        List<ClearingEventInfo> adapterFaultClearingEvents =
                clearingEventInfoDao.getAllClearingEventsForProviderByStatus(
                        providerId,
                        ClearingEventStatus.ADAPTER_FAULT
                );
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

}
