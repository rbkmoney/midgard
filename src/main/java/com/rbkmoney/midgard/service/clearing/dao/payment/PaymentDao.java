package com.rbkmoney.midgard.service.clearing.dao.payment;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;

import java.util.List;

public interface PaymentDao {

    List<Payment> getPayments(long eventId, List<Integer> providerIds) throws DaoException;

    List<CashFlow> getCashFlow(long objId) throws DaoException;

}
