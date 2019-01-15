package com.rbkmoney.midgard.service.clearing.helpers.payment;

import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;

import java.util.List;

public interface PaymentHelper {

    List<Payment> getPayments(long eventId, List<Integer> providerIds, int poolSize);

    List<CashFlow> getCashFlow(long objId);

}
