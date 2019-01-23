package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingEventResponse;
import com.rbkmoney.midgard.ClearingEventState;
import com.rbkmoney.midgard.FailureTransactionData;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
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
public class ClearingRevisionHandler implements Handler<Long> {

    private final TransactionsDao transactionsDao;

    private final ClearingEventInfoDao clearingEventInfoDao;

    private final ClearingAdapterSrv.Iface clearingAdapterService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(Long clearingId) throws Exception {
        try {
            ClearingEventResponse response = clearingAdapterService.getBankResponse(clearingId);
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
