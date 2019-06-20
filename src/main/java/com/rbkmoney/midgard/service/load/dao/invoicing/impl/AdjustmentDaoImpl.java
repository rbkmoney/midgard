package com.rbkmoney.midgard.service.load.dao.invoicing.impl;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.AdjustmentDao;
import org.jooq.Query;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.Adjustment;
import org.jooq.generated.feed.tables.records.AdjustmentRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Adjustment.ADJUSTMENT;

@Component
public class AdjustmentDaoImpl extends AbstractGenericDao implements AdjustmentDao {

    private final RowMapper<Adjustment> adjustmentRowMapper;

    @Autowired
    public AdjustmentDaoImpl(DataSource dataSource) {
        super(dataSource);
        adjustmentRowMapper = new RecordRowMapper<>(ADJUSTMENT, Adjustment.class);
    }

    @Override
    public Long save(Adjustment adjustment) throws DaoException {
        AdjustmentRecord record = getDslContext().newRecord(ADJUSTMENT, adjustment);
        Query query = getDslContext().insertInto(ADJUSTMENT)
                .set(record)
                .onConflict(ADJUSTMENT.INVOICE_ID, ADJUSTMENT.SEQUENCE_ID, ADJUSTMENT.CHANGE_ID)
                .doNothing()
                .returning(ADJUSTMENT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    @Override
    public Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws DaoException {
        Query query = getDslContext().selectFrom(ADJUSTMENT)
                .where(ADJUSTMENT.INVOICE_ID.eq(invoiceId)
                        .and(ADJUSTMENT.PAYMENT_ID.eq(paymentId))
                        .and(ADJUSTMENT.ADJUSTMENT_ID.eq(adjustmentId))
                        .and(ADJUSTMENT.CURRENT));

        return fetchOne(query, adjustmentRowMapper);
    }

    @Override
    public void updateCommissions(Long adjId) throws DaoException {
        MapSqlParameterSource params = new MapSqlParameterSource("adjId", adjId).addValue("objType", PaymentChangeType.adjustment.name());
        this.getNamedParameterJdbcTemplate().update(
                "UPDATE feed.adjustment SET fee = (SELECT feed.get_adjustment_fee(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :adjId AND obj_type = CAST(:objType as feed.payment_change_type)), " +
                        "provider_fee = (SELECT feed.get_adjustment_provider_fee(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :adjId AND obj_type = CAST(:objType as feed.payment_change_type)), " +
                        "external_fee = (SELECT feed.get_adjustment_external_fee(feed.cash_flow.*) FROM feed.cash_flow WHERE obj_id = :adjId AND obj_type = CAST(:objType as feed.payment_change_type)) " +
                        "WHERE  id = :adjId",
                params);
    }

    @Override
    public void updateNotCurrent(String invoiceId, String paymentId, String adjustmentId) throws DaoException {
        Query query = getDslContext().update(ADJUSTMENT).set(ADJUSTMENT.CURRENT, false)
                .where(ADJUSTMENT.INVOICE_ID.eq(invoiceId)
                        .and(ADJUSTMENT.PAYMENT_ID.eq(paymentId)
                        .and(ADJUSTMENT.ADJUSTMENT_ID.eq(adjustmentId))
                        .and(ADJUSTMENT.CURRENT)));
        execute(query);
    }

}
