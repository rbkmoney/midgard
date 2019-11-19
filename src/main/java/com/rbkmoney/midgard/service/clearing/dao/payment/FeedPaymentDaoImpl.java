package com.rbkmoney.midgard.service.clearing.dao.payment;

import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.jooq.generated.feed.enums.PaymentStatus;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.DEFAULT_TRX_VERSION;
import static org.jooq.generated.feed.tables.CashFlow.CASH_FLOW;
import static org.jooq.generated.feed.tables.Payment.PAYMENT;
import static org.jooq.generated.midgard.tables.ClearingTransaction.CLEARING_TRANSACTION;

@Component
public class FeedPaymentDaoImpl extends AbstractGenericDao implements PaymentDao {

    private final RowMapper<Payment> paymentRowMapper;

    private final RowMapper<CashFlow> cashFlowRowMapper;

    public FeedPaymentDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
        paymentRowMapper = new RecordRowMapper<>(PAYMENT, Payment.class);
        cashFlowRowMapper = new RecordRowMapper<>(CASH_FLOW, CashFlow.class);
    }

    @Override
    public List<Payment> getPayments(long sourceRowId, List<Integer> providerIds, int poolSize) {
        Query query = getDslContext().select(PAYMENT.fields())
                .from(PAYMENT)
                .leftJoin(CLEARING_TRANSACTION)
                    .on(CLEARING_TRANSACTION.SOURCE_ROW_ID.greaterThan(sourceRowId))
                    .and(CLEARING_TRANSACTION.PROVIDER_ID.in(providerIds))
                    .and(PAYMENT.INVOICE_ID.eq(CLEARING_TRANSACTION.INVOICE_ID))
                    .and(PAYMENT.PAYMENT_ID.eq(CLEARING_TRANSACTION.PAYMENT_ID))
                    .and(CLEARING_TRANSACTION.TRX_VERSION.eq(DEFAULT_TRX_VERSION))
                .where(PAYMENT.ID.greaterThan(sourceRowId))
                .and(PAYMENT.STATUS.eq(PaymentStatus.captured))
                .and(PAYMENT.ROUTE_PROVIDER_ID.in(providerIds))
                .and(CLEARING_TRANSACTION.SOURCE_ROW_ID.isNull());
        return fetch(query, paymentRowMapper);
    }

    @Override
    public List<CashFlow> getCashFlow(long objId) {
        Query query = getDslContext().selectFrom(CASH_FLOW)
                .where(CASH_FLOW.OBJ_ID.eq(objId));
        return fetch(query, cashFlowRowMapper);
    }

}
