package com.rbkmoney.midgard.service.clearing.handlers.failure;

import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;
import org.springframework.stereotype.Component;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.getFailureTransaction;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdapterFailureTransactionHandler implements FailureTransactionHandler<Transaction, Long> {

    private final TransactionsDao transactionsDao;

    @Override
    public void handleTransaction(Transaction transaction, Long clearingId) {
        try {
            FailureTransaction failureTransaction = getFailureTransaction(transaction, clearingId);
            log.error("Error transaction was received from a clearing adapter for clearing event {}. " +
                    "Transaction info: {}", clearingId, failureTransaction);
            transactionsDao.saveFailureTransaction(failureTransaction);
        } catch (Exception ex) {
            log.error("Received error when processing failure transaction", ex);
        }
    }

}
