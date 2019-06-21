package com.rbkmoney.midgard.service.load.dao.invoicing.impl;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.RefundDao;
import org.jooq.Query;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.feed.tables.records.RefundRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Refund.REFUND;

@Component
public class RefundDaoImpl extends AbstractGenericDao implements RefundDao {

    private final RowMapper<Refund> refundRowMapper;

    @Autowired
    public RefundDaoImpl(DataSource dataSource) {
        super(dataSource);
        refundRowMapper = new RecordRowMapper<>(REFUND, Refund.class);
    }

    @Override
    public Long save(Refund refund) throws DaoException {
        RefundRecord record = getDslContext().newRecord(REFUND, refund);
        Query query = getDslContext().insertInto(REFUND)
                .set(record)
                .onConflict(REFUND.INVOICE_ID, REFUND.SEQUENCE_ID, REFUND.CHANGE_ID)
                .doNothing()
                .returning(REFUND.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey() == null ? null: keyHolder.getKey().longValue();
    }

    @Override
    public Refund get(String invoiceId, String paymentId, String refundId) throws DaoException {
        Query query = getDslContext().selectFrom(REFUND)
                .where(REFUND.INVOICE_ID.eq(invoiceId)
                        .and(REFUND.PAYMENT_ID.eq(paymentId))
                        .and(REFUND.REFUND_ID.eq(refundId))
                        .and(REFUND.CURRENT));

        return fetchOne(query, refundRowMapper);
    }

    @Override
    public void updateCommissions(Long rfndId) throws DaoException {
        MapSqlParameterSource params = new MapSqlParameterSource("rfndId", rfndId).addValue("objType", PaymentChangeType.refund.name());
        this.getNamedParameterJdbcTemplate().update(
                "UPDATE feed.refund SET fee = (SELECT feed.get_refund_fee(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :rfndId AND obj_type = CAST(:objType as feed.payment_change_type)), " +
                        "provider_fee = (SELECT feed.get_refund_provider_fee(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :rfndId AND obj_type = CAST(:objType as feed.payment_change_type)), " +
                        "external_fee = (SELECT feed.get_refund_external_fee(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :rfndId AND obj_type = CAST(:objType as feed.payment_change_type)) " +
                        "WHERE  id = :rfndId",
                params);
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        Query query = getDslContext().update(REFUND).set(REFUND.CURRENT, false).where(REFUND.ID.eq(id));
        execute(query);
    }

}
