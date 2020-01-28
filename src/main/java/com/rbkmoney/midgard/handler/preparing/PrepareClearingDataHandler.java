package com.rbkmoney.midgard.handler.preparing;

import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.dao.refund.ClearingRefundDao;
import com.rbkmoney.midgard.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.utils.ClearingEventUtils;
import com.rbkmoney.midgard.utils.MappingUtils;
import com.rbkmoney.midgard.handler.Handler;
import com.rbkmoney.midgard.handler.failure.FailureTransactionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.rbkmoney.midgard.domain.enums.ClearingEventStatus;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventInfo;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventTransactionInfo;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingRefund;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.rbkmoney.midgard.domain.enums.ClearingEventStatus.STARTED;

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
        Long clearingId = clearingEvent.getClearingId();

        try {
            ClearingAdapter clearingAdapter = clearingEvent.getClearingAdapter();
            String adapterName = clearingAdapter.getAdapterName();
            log.info("Start preparing data for clearing event {} (bank: {})", clearingId, adapterName);
            prepapeData(new ClearingProcessingEvent(clearingAdapter, clearingId));
            clearingEventInfoDao.updateClearingStatus(clearingId, STARTED, clearingAdapter.getAdapterId());
            log.info("Clearing data for clearing event {} was prepared (bank: {})", clearingId, adapterName);
        } catch (Exception ex) {
            log.error("Received error while preparing clearing event {}", clearingId, ex);
        }
    }

    private void prepapeData(ClearingProcessingEvent clearingEvent) {
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
                    MappingUtils.transformClearingTrx(clearingId, providerId, trx),
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
                    MappingUtils.transformClearingRefund(clearingId, providerId, refund),
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
            int trxCount = transactionsDao.getProcessedClearingTransactionCount(clearingId, providerId);
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

                lastRowId = ClearingEventUtils.getLastRowId(failureTransactions);
            } while (failureTransactions != null && failureTransactions.size() == innerPackageSize);
        }
    }

}
