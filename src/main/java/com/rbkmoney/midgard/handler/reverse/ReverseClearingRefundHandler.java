package com.rbkmoney.midgard.handler.reverse;

import com.rbkmoney.midgard.ClearingOperationInfo;
import com.rbkmoney.midgard.ClearingOperationType;
import com.rbkmoney.midgard.dao.refund.ClearingRefundDao;
import com.rbkmoney.midgard.domain.enums.TransactionClearingState;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingRefund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReverseClearingRefundHandler implements ReverseClearingOperationHandler {

    private final ClearingRefundDao clearingRefundDao;

    @Override
    public void reverseOperation(ClearingOperationInfo operationInfo) {
        log.info("Starting the reverse operation process (operationInfo: {})", operationInfo);
        ClearingRefund refund = clearingRefundDao.getRefund(
                operationInfo.getInvoiceId(),
                operationInfo.getPaymentId(),
                operationInfo.getRefundId(),
                operationInfo.getVersion()
        );
        refund.setId(null);
        refund.setClearingState(TransactionClearingState.READY);
        if (operationInfo.isSetAmount()) {
            refund.setAmount(operationInfo.getAmount());
        }
        refund.setTrxVersion(refund.getTrxVersion() + 1);
        refund.setComment("Added after using ReverseClearingOperation (refund) by " + LocalDateTime.now());
        refund.setIsReversed(true);
        clearingRefundDao.save(refund);
        log.info("The reverse operation process finished (operationInfo: {})", operationInfo);
    }

    @Override
    public boolean isAccept(ClearingOperationInfo clearingOperationInfo) {
        return clearingOperationInfo != null
                && clearingOperationInfo.getTransactionType() == ClearingOperationType.refund;
    }

}
