package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingEventResponse;
import com.rbkmoney.midgard.ClearingEventState;
import com.rbkmoney.midgard.FailureTransactionData;
import com.rbkmoney.midgard.service.clearing.helpers.clearing_info.ClearingInfoHelper;
import com.rbkmoney.midgard.service.clearing.helpers.transaction.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingRevisionHandler implements Handler {

    private final TransactionHelper transactionHelper;

    private final ClearingInfoHelper clearingInfoHelper;

    private final ClearingAdapterSrv.Iface clearingAdapterService;

    @Override
    @Transactional
    public void handle(Long clearingId) {
        try {
            ClearingEventResponse response = clearingAdapterService.getBankResponse(clearingId);
            ClearingEventState clearingState = response.getClearingState();
            if (clearingState == ClearingEventState.SUCCESS || clearingState == ClearingEventState.FAILED) {
                clearingInfoHelper.setClearingEventState(clearingId, clearingState);
                List<FailureTransactionData> failureTransactions = response.getFailureTransactions();
                transactionHelper.saveFailureTransactions(clearingId, failureTransactions);
            } else {
                log.debug("Clearing event with id {} not complete yet!", clearingId);
            }
        } catch (TException ex) {
            log.error("Error while getting response for clearing event with id " + clearingId, ex);
        }
    }

}
