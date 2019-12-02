package com.rbkmoney.midgard.handler.preparing;

import com.rbkmoney.midgard.dao.refund.ClearingRefundDao;
import com.rbkmoney.midgard.utils.MappingUtils;
import com.rbkmoney.midgard.dao.transaction.TransactionsDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.enums.TransactionClearingState;
import org.jooq.generated.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.tables.pojos.ClearingRefund;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessRefundTransactionHandler implements ProcessTransactionHandler<ClearingRefund> {

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    @Override
    @Transactional
    public void handle(ClearingRefund refund, long clearingId, int providerId) {
        ClearingEventTransactionInfo refundInfo = MappingUtils.transformClearingRefund(clearingId, providerId, refund);
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
