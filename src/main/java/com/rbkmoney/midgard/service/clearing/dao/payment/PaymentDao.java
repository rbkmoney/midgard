package com.rbkmoney.midgard.service.clearing.dao.payment;

import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;

import java.util.List;

public interface PaymentDao {

    List<Payment> getPayments(long sourceRowId, List<Integer> providerIds, int poolSize);

    List<CashFlow> getCashFlow(long objId);

}
