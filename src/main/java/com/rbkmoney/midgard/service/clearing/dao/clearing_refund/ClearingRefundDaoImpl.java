package com.rbkmoney.midgard.service.clearing.dao.clearing_refund;

import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.records.ClearingRefundRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import static org.jooq.generated.midgard.tables.ClearingRefund.CLEARING_REFUND;

@Slf4j
@Repository
public class ClearingRefundDaoImpl extends AbstractGenericDao implements ClearingRefundDao {

    private final RowMapper<ClearingRefund> clearingRefundRowMapper;

    public ClearingRefundDaoImpl(DataSource dataSource) {
        super(dataSource);
        clearingRefundRowMapper = new RecordRowMapper<>(CLEARING_REFUND, ClearingRefund.class);
    }

    @Override
    public Long save(ClearingRefund clearingRefund) {
        log.debug("Adding new clearing refund: {}", clearingRefund);
        ClearingRefundRecord record = getDslContext().newRecord(CLEARING_REFUND, clearingRefund);
        Query query = getDslContext().insertInto(CLEARING_REFUND).set(record);

        int addedRows = execute(query);
        log.debug("New clearing refund with sequence id {} was added", clearingRefund.getSequenceId());
        return Long.valueOf(addedRows);
    }

    @Override
    public ClearingRefund get(String refundId) {
        log.debug("Getting a refund with refundId {}", refundId);
        Query query = getDslContext().selectFrom(CLEARING_REFUND)
                .where(CLEARING_REFUND.REFUND_ID.eq(refundId));
        ClearingRefund clearingRefund = fetchOne(query, clearingRefundRowMapper);
        log.debug("Refund with refund id {} {}", refundId, clearingRefund == null ? "not found" : "found");
        return clearingRefund;
    }

    @Override
    public ClearingRefund getRefund(String invoiceId, String paymentId, String refundId) {
        Query query = getDslContext().selectFrom(CLEARING_REFUND)
                .where(CLEARING_REFUND.INVOICE_ID.eq(invoiceId))
                .and(CLEARING_REFUND.PAYMENT_ID.eq(paymentId))
                .and(CLEARING_REFUND.REFUND_ID.eq(refundId));
        ClearingRefund clearingRefund = fetchOne(query, clearingRefundRowMapper);
        log.debug("Refund with invoice id {} and payment id {} {}", invoiceId, paymentId,
                clearingRefund == null ? "not found" : "found");
        return clearingRefund;
    }

    @Override
    public ClearingRefund getLastTransactionEvent() {
        Query query = getDslContext().selectFrom(CLEARING_REFUND)
                .where(CLEARING_REFUND.SOURCE_ROW_ID.isNotNull())
                .orderBy(CLEARING_REFUND.SOURCE_ROW_ID.desc())
                .limit(1);
        return fetchOne(query, clearingRefundRowMapper);
    }

}
