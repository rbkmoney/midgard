package com.rbkmoney.midgard.service.clearing.dao.clearing_refund;

import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.records.ClearingRefundRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.generated.midgard.Tables.CLEARING_TRANSACTION;
import static org.jooq.generated.midgard.enums.TransactionClearingState.FAILED;
import static org.jooq.generated.midgard.enums.TransactionClearingState.READY;
import static org.jooq.generated.midgard.tables.ClearingRefund.CLEARING_REFUND;

@Slf4j
@Repository
public class ClearingRefundDaoImpl extends AbstractGenericDao implements ClearingRefundDao {

    private final RowMapper<ClearingRefund> clearingRefundRowMapper;

    public ClearingRefundDaoImpl(HikariDataSource dataSource) {
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
    public ClearingRefund getRefund(String invoiceId, String paymentId, String refundId, Integer trxVersion) {
        Query query = getDslContext().selectFrom(CLEARING_REFUND)
                .where(CLEARING_REFUND.INVOICE_ID.eq(invoiceId))
                .and(CLEARING_REFUND.PAYMENT_ID.eq(paymentId))
                .and(CLEARING_REFUND.REFUND_ID.eq(refundId)
                .and(CLEARING_REFUND.TRX_VERSION.eq(trxVersion)));
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

    @Override
    public ClearingRefund getLastActiveRefund() {
        Query query = getDslContext().selectFrom(CLEARING_REFUND)
                .where(CLEARING_REFUND.SOURCE_ROW_ID.isNotNull())
                .and(CLEARING_REFUND.CLEARING_STATE.in(READY, FAILED))
                .orderBy(CLEARING_REFUND.SOURCE_ROW_ID.asc())
                .limit(1);
        return fetchOne(query, clearingRefundRowMapper);
    }

    @Override
    public List<ClearingRefund> getReadyClearingRefunds(int providerId, int packageSize) {
        Query query = getDslContext().select(CLEARING_REFUND.fields())
                .from(CLEARING_REFUND)
                .join(CLEARING_TRANSACTION).on(CLEARING_TRANSACTION.INVOICE_ID.eq(CLEARING_REFUND.INVOICE_ID))
                    .and(CLEARING_TRANSACTION.PAYMENT_ID.eq(CLEARING_REFUND.PAYMENT_ID))
                    .and(CLEARING_TRANSACTION.TRX_VERSION.eq(1))
                    .and(CLEARING_TRANSACTION.PROVIDER_ID.eq(providerId))
                .where(CLEARING_REFUND.CLEARING_STATE.in(READY, FAILED))
                .limit(packageSize);
        return fetch(query, clearingRefundRowMapper);
    }

    @Override
    public void updateClearingRefundState(String invoiceId,
                                          String paymentId,
                                          String refundId,
                                          int version,
                                          long clearingId,
                                          Integer providerId,
                                          TransactionClearingState state) {
        Query query = getDslContext().update(CLEARING_REFUND)
                .set(CLEARING_REFUND.CLEARING_STATE, state)
                .set(CLEARING_REFUND.PROVIDER_ID, providerId)
                .set(CLEARING_REFUND.CLEARING_ID, clearingId)
                .where(CLEARING_REFUND.INVOICE_ID.eq(invoiceId)
                        .and(CLEARING_REFUND.PAYMENT_ID.eq(paymentId))
                        .and(CLEARING_REFUND.REFUND_ID.eq(refundId))
                        .and(CLEARING_REFUND.TRX_VERSION.eq(version)));

        execute(query);
    }

}
