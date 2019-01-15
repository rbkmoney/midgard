package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.data.enums.ImporterType;
import com.rbkmoney.midgard.service.clearing.helpers.ClearingCashFlowHelper;
import com.rbkmoney.midgard.service.clearing.helpers.PaymentHelper;
import com.rbkmoney.midgard.service.clearing.helpers.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionImporter implements Importer {

    private final TransactionHelper transactionHelper;

    private final PaymentHelper paymentHelper;

    private final ClearingCashFlowHelper cashFlowHelper;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    @Override
    public void getData() {
        long eventId = transactionHelper.getLastTransactionEventId();
        log.info("Transaction data import will start with event id {}", eventId);
        List<Payment> payments;
        do {
            payments = paymentHelper.getPayments(eventId, poolSize);
            for (Payment payment : payments) {
                transactionHelper.saveTransaction(payment);
                List<CashFlow> cashFlow = paymentHelper.getCashFlow(payment.getId());
                cashFlowHelper.saveCashFlow(payment, cashFlow);
            }
        } while(payments.size() == poolSize);
        log.info("Transaction data import have finished");
    }

    @Override
    public boolean isInstance(ImporterType type) {
        return ImporterType.TRANSACTION == type;
    }

}
