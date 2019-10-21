package com.rbkmoney.midgard.service.clearing.handlers.preparing;

import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.transformClearingTrx;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessPaymentTransactionHandler implements ProcessTransactionHandler<ClearingTransaction> {

    private final TransactionsDao transactionsDao;

    @Override
    @Transactional
    public void handle(ClearingTransaction trx, long clearingId, int providerId) {
        ClearingEventTransactionInfo transactionInfo = transformClearingTrx(clearingId, providerId, trx);
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
