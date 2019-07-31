package com.rbkmoney.midgard.service.clearing.dao.payment;

import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import org.jooq.Query;
import org.jooq.generated.feed.enums.PaymentStatus;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import java.util.List;

import static org.jooq.generated.feed.tables.CashFlow.CASH_FLOW;
import static org.jooq.generated.feed.tables.Payment.PAYMENT;

@Component
public class FeedPaymentDaoImpl extends AbstractGenericDao implements PaymentDao {

    private final RowMapper<Payment> paymentRowMapper;

    private final RowMapper<CashFlow> cashFlowRowMapper;

    public FeedPaymentDaoImpl(DataSource dataSource) {
        super(dataSource);
        paymentRowMapper = new RecordRowMapper<>(PAYMENT, Payment.class);
        cashFlowRowMapper = new RecordRowMapper<>(CASH_FLOW, CashFlow.class);
    }

    @Override
    public List<Payment> getPayments(long sourceRowId, List<Integer> providerIds, int poolSize) {
        Query query = getDslContext().selectFrom(PAYMENT)
                .where(PAYMENT.ID.greaterThan(sourceRowId))
                .and(PAYMENT.STATUS.eq(PaymentStatus.captured))
                .and(PAYMENT.ROUTE_PROVIDER_ID.in(providerIds));
        return fetch(query, paymentRowMapper);
    }

    @Override
    public List<CashFlow> getCashFlow(long objId) {
        Query query = getDslContext().selectFrom(CASH_FLOW)
                .where(CASH_FLOW.OBJ_ID.eq(objId));
        return fetch(query, cashFlowRowMapper);
    }

}
