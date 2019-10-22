package com.rbkmoney.midgard.service.clearing.handlers.preparing;

import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.transformClearingRefund;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessRefundTransactionHandler implements ProcessTransactionHandler<ClearingRefund> {

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    @Override
    @Transactional
    public void handle(ClearingRefund refund, long clearingId, int providerId) {
        ClearingEventTransactionInfo refundInfo = transformClearingRefund(clearingId, providerId, refund);
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
    }

}
