package com.rbkmoney.midgard.base.load.dao.invoicing.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.RecordRowMapper;
import com.rbkmoney.midgard.base.load.dao.invoicing.iface.PaymentDao;
import org.jooq.Query;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.feed.tables.records.PaymentRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.Tables.PAYMENT;

@Component
public class PaymentDaoImpl extends AbstractGenericDao implements PaymentDao {

    private final RowMapper<Payment> paymentRowMapper;

    @Autowired
    public PaymentDaoImpl(DataSource dataSource) {
        super(dataSource);
        paymentRowMapper = new RecordRowMapper<>(PAYMENT, Payment.class);
    }

    @Override
    public Long save(Payment payment) throws DaoException {
        PaymentRecord paymentRecord = getDslContext().newRecord(PAYMENT, payment);
        Query query = getDslContext().insertInto(PAYMENT).set(paymentRecord).returning(PAYMENT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateCommissions(Long pmntId) throws DaoException {
        MapSqlParameterSource params = new MapSqlParameterSource("pmntId", pmntId).addValue("objType", PaymentChangeType.payment.name());
        this.getNamedParameterJdbcTemplate().update(
                "UPDATE feed.payment SET fee = (SELECT feed.get_payment_fee(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :pmntId AND obj_type = CAST(:objType as feed.payment_change_type)), " +
                        "provider_fee = (SELECT feed.get_payment_provider_fee(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :pmntId AND obj_type = CAST(:objType as feed.payment_change_type)), " +
                        "external_fee = (SELECT feed.get_payment_external_fee(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :pmntId AND obj_type = CAST(:objType as feed.payment_change_type)), " +
                        "guarantee_deposit = (SELECT feed.get_payment_guarantee_deposit(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :pmntId AND obj_type = CAST(:objType as feed.payment_change_type)) " +
                        "WHERE  id = :pmntId",
                params);
    }

    @Override
    public Payment get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT)
                .where(PAYMENT.INVOICE_ID.eq(invoiceId).and(PAYMENT.PAYMENT_ID.eq(paymentId)).and(PAYMENT.CURRENT));

        return fetchOne(query, paymentRowMapper);
    }

    @Override
    public void updateNotCurrent(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().update(PAYMENT).set(PAYMENT.CURRENT, false)
                .where(PAYMENT.INVOICE_ID.eq(invoiceId).and(PAYMENT.PAYMENT_ID.eq(paymentId).and(PAYMENT.CURRENT)));
        execute(query);
    }
}
