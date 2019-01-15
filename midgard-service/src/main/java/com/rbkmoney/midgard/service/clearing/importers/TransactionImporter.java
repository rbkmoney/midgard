package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.helpers.clearing_cash_flow.ClearingCashFlowHelper;
import com.rbkmoney.midgard.service.clearing.helpers.payment.PaymentHelper;
import com.rbkmoney.midgard.service.clearing.helpers.transaction.TransactionHelper;
import com.rbkmoney.midgard.service.config.props.AdapterProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionImporter implements Importer {

    private final TransactionHelper transactionHelper;

    private final PaymentHelper paymentHelper;

    private final ClearingCashFlowHelper cashFlowHelper;

    private final List<AdapterProps> adaptersProps;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    @Override
    public void getData() {
        long eventId = transactionHelper.getLastTransactionEventId();
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
        List<Payment> payments = paymentHelper.getPayments(eventId, providerIds, poolSize);
        for (Payment payment : payments) {
            transactionHelper.saveTransaction(payment);
            List<CashFlow> cashFlow = paymentHelper.getCashFlow(payment.getId());
            cashFlowHelper.saveCashFlow(payment, cashFlow);
        }
        return payments.size();
    }

}
