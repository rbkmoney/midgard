package com.rbkmoney.midgard.handler;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingEventResponse;
import com.rbkmoney.midgard.ClearingEventState;
import com.rbkmoney.midgard.FailureTransactionData;
import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.dao.transaction.TransactionsDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import com.rbkmoney.midgard.domain.enums.ClearingEventStatus;
import com.rbkmoney.midgard.domain.tables.pojos.FailureTransaction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.rbkmoney.midgard.ClearingEventState.FAILED;
import static com.rbkmoney.midgard.ClearingEventState.SUCCESS;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventStateRevisionHandler implements Handler<ClearingProcessingEvent> {

    private final TransactionsDao transactionsDao;

    private final ClearingEventInfoDao clearingEventInfoDao;

    @Override
    @Transactional
    public void handle(ClearingProcessingEvent event) throws Exception {
        Long clearingId = event.getClearingId();
        log.info("Event state revision for clearing event with id {} get started", clearingId);
        try {
            ClearingAdapterSrv.Iface adapter = event.getClearingAdapter().getAdapter();
            ClearingEventResponse response = adapter.getBankResponse(clearingId);
            ClearingEventState clearingState = response.getClearingState();
            if (clearingState == SUCCESS || clearingState == FAILED) {
                setClearingEventState(response, event.getClearingAdapter().getAdapterId());
                List<FailureTransactionData> failureTransactions = response.getFailureTransactions();
                saveFailureTransactions(clearingId, failureTransactions);
            } else {
                log.info("Clearing event with id {} not complete yet!", clearingId);
            }
        } catch (TException ex) {
            log.error("Error while getting response for clearing event with id " + clearingId, ex);
            throw new Exception(ex);
        }
    }

    private void setClearingEventState(ClearingEventResponse response, int providerId) {
        long clearingId = response.getClearingId();
        ClearingEventState clearingState = response.getClearingState();
        if (clearingState == SUCCESS) {
            if (response.getFailureTransactions() == null || response.getFailureTransactions().isEmpty()) {
                clearingEventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.COMPLETE, providerId);
            } else {
                clearingEventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.COMPLETE_WITH_ERRORS, providerId);
            }
        } else if (clearingState == FAILED) {
            clearingEventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.FAILED, providerId);
        } else {
            log.info("For clearing event {} received state {}. No change of status will be made");
        }
    }

    private void saveFailureTransactions(long clearingEventId, List<FailureTransactionData> failureTransactions) {
        if (failureTransactions == null) {
            return;
        }
        for (FailureTransactionData failureTransaction : failureTransactions) {
            FailureTransaction transaction = new FailureTransaction();
            transaction.setTransactionId(failureTransaction.getTransactionId());
            transaction.setClearingId(clearingEventId);
            transaction.setErrorReason(failureTransaction.getComment());
            transactionsDao.saveFailureTransaction(transaction);
        }
    }

}
