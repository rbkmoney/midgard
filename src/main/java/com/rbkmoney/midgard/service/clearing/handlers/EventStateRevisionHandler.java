package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingEventResponse;
import com.rbkmoney.midgard.ClearingEventState;
import com.rbkmoney.midgard.FailureTransactionData;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingProcessingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventStateRevisionHandler implements Handler<ClearingProcessingEvent> {

    private final TransactionsDao transactionsDao;

    private final ClearingEventInfoDao clearingEventInfoDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(ClearingProcessingEvent event) throws Exception {
        Long clearingId = event.getClearingId();

        try {
            ClearingAdapterSrv.Iface adapter = event.getClearingAdapter().getAdapter();
            ClearingEventResponse response = adapter.getBankResponse(clearingId);
            ClearingEventState clearingState = response.getClearingState();
            if (clearingState == ClearingEventState.SUCCESS || clearingState == ClearingEventState.FAILED) {
                setClearingEventState(clearingId, clearingState);
                List<FailureTransactionData> failureTransactions = response.getFailureTransactions();
                saveFailureTransactions(clearingId, failureTransactions);
            } else {
                log.debug("Clearing event with id {} not complete yet!", clearingId);
            }
        } catch (TException ex) {
            log.error("Error while getting response for clearing event with id " + clearingId, ex);
        }
    }

    private void setClearingEventState(long clearingEventId, ClearingEventState state) {
        switch (state) {
            case SUCCESS:
                clearingEventInfoDao.updateClearingStatus(clearingEventId, ClearingEventStatus.SUCCESS);
                break;
            case FAILED:
                clearingEventInfoDao.updateClearingStatus(clearingEventId, ClearingEventStatus.FAILED);
                break;
        }
    }

    private void saveFailureTransactions(long clearingEventId, List<FailureTransactionData> failureTransactions) {
        for (FailureTransactionData failureTransaction : failureTransactions) {
            FailureTransaction transaction = new FailureTransaction();
            transaction.setTransactionId(failureTransaction.getTransactionId());
            transaction.setClearingId(clearingEventId);
            transaction.setReason(failureTransaction.getComment());
            transactionsDao.saveFailureTransaction(transaction);
        }
    }

}
