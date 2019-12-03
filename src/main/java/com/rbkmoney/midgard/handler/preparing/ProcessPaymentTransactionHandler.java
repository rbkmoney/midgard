package com.rbkmoney.midgard.handler.preparing;

import com.rbkmoney.midgard.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.rbkmoney.midgard.domain.enums.TransactionClearingState;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventTransactionInfo;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingTransaction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessPaymentTransactionHandler implements ProcessTransactionHandler<ClearingTransaction> {

    private final TransactionsDao transactionsDao;

    @Override
    @Transactional
    public void handle(ClearingTransaction trx, long clearingId, int providerId) {
        ClearingEventTransactionInfo transactionInfo = MappingUtils.transformClearingTrx(clearingId, providerId, trx);
        transactionsDao.saveClearingEventTransactionInfo(transactionInfo);
        transactionsDao.updateClearingTransactionState(
                trx.getInvoiceId(),
                trx.getPaymentId(),
                trx.getTrxVersion(),
                clearingId,
                TransactionClearingState.ACTIVE
        );
    }

}
