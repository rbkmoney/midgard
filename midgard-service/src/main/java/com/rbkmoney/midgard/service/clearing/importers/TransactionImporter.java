package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.payment.PaymentDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import com.rbkmoney.midgard.service.config.props.AdapterProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionImporter implements Importer {

    private final TransactionsDao transactionsDao;

    private final PaymentDao paymentDao;

    private final ClearingCashFlowDao dao;

    private final List<AdapterProps> adaptersProps;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    @Override
    public void getData() {
        long eventId = getLastTransactionEventId();
        log.info("Transaction data import will start with event id {}", eventId);

        List<Integer> providerIds = adaptersProps.stream()
                .map(adapterProps -> adapterProps.getProviderId())
                .collect(Collectors.toList());

        int obtainPaymentsSize;
        do {
            obtainPaymentsSize = pollPayments(eventId, providerIds);
        } while(obtainPaymentsSize == poolSize);
        log.info("Transaction data import have finished");
    }

    private int pollPayments(long eventId, List<Integer> providerIds) {
        List<Payment> payments = paymentDao.getPayments(eventId, providerIds, poolSize);
        for (Payment payment : payments) {
            saveTransaction(payment);
            List<CashFlow> cashFlow = paymentDao.getCashFlow(payment.getId());
            saveCashFlow(payment, cashFlow);
        }
        return payments.size();
    }

    private void saveCashFlow(Payment payment, List<CashFlow> cashFlow) {
        List<ClearingTransactionCashFlow> tranCashFlow = cashFlow.stream()
                .map(flow -> {
                    ClearingTransactionCashFlow transactionCashFlow = MappingUtils.transformCashFlow(flow);
                    transactionCashFlow.setSourceEventId(payment.getEventId());
                    return transactionCashFlow;
                })
                .collect(Collectors.toList());
        dao.save(tranCashFlow);
    }

    private void saveTransaction(Payment payment) {
        ClearingTransaction transaction = MappingUtils.transformTransaction(payment);
        log.debug("Saving a transaction {}", transaction);
        transactionsDao.save(transaction);
    }

    private long getLastTransactionEventId() {
        Long eventId = transactionsDao.getLastTransactionEventId();
        if (eventId == null) {
            log.warn("Event ID for clearing transactions was not found!");
            return 0L;
        } else {
            return eventId;
        }
    }

}
