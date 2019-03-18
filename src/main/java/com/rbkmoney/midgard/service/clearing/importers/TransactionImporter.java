package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.payment.PaymentDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionImporter implements Importer {

    private final TransactionsDao transactionsDao;

    private final PaymentDao paymentDao;

    private final ClearingCashFlowDao dao;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    @Override
    public void getData(List<Integer> providerIds) throws DaoException {
        log.info("Transaction data import will start with event id {}", getLastTransactionEventId());

        try {
            while(pollPayments(getLastTransactionEventId(), providerIds) == poolSize);
        } catch (DaoException ex) {
            log.error("Error saving transaction import data", ex);
        }

        log.info("Transaction data import was finished");
    }

    private int pollPayments(long eventId, List<Integer> providerIds) throws DaoException {
        List<Payment> payments = paymentDao.getPayments(eventId, providerIds, poolSize);
        for (Payment payment : payments) {
            saveTransaction(payment);
        }
        return payments.size();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveTransaction(Payment payment) throws DaoException {
        ClearingTransaction transaction = MappingUtils.transformTransaction(payment);
        log.debug("Saving a transaction {}", transaction);
        transactionsDao.save(transaction);
        List<CashFlow> cashFlow = paymentDao.getCashFlow(payment.getId());
        saveCashFlow(payment, cashFlow);
    }

    private void saveCashFlow(Payment payment, List<CashFlow> cashFlow) {
        List<ClearingTransactionCashFlow> tranCashFlow = cashFlow.stream()
                .map(flow -> {
                    ClearingTransactionCashFlow transactionCashFlow =
                            MappingUtils.transformCashFlow(flow, payment.getEventId());
                    return transactionCashFlow;
                })
                .collect(Collectors.toList());
        dao.save(tranCashFlow);
    }

    private long getLastTransactionEventId() {
        ClearingTransaction clearingTransaction = transactionsDao.getLastTransaction();
        if (clearingTransaction == null) {
            log.warn("Event ID for clearing transactions was not found!");
            return 0L;
        } else {
            return clearingTransaction.getEventId();
        }
    }

}
