package com.rbkmoney.midgard.dao.refund;

import com.rbkmoney.midgard.dao.AbstractGenericDao;
import com.rbkmoney.midgard.dao.RecordRowMapper;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import com.rbkmoney.midgard.domain.enums.TransactionClearingState;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingRefund;
import com.rbkmoney.midgard.domain.tables.records.ClearingRefundRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.midgard.domain.Tables.CLEARING_TRANSACTION;
import static com.rbkmoney.midgard.domain.enums.TransactionClearingState.FAILED;
import static com.rbkmoney.midgard.domain.enums.TransactionClearingState.READY;
import static com.rbkmoney.midgard.domain.tables.ClearingRefund.CLEARING_REFUND;

@Slf4j
@Component
public class ClearingRefundDaoImpl extends AbstractGenericDao implements ClearingRefundDao {

    private final RowMapper<ClearingRefund> clearingRefundRowMapper;

    private static final int DEFAULT_TRX_VERSION = 1;

    public ClearingRefundDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
        clearingRefundRowMapper = new RecordRowMapper<>(CLEARING_REFUND, ClearingRefund.class);
    }

    @Override
    public Long save(ClearingRefund refund) {
        log.debug("Adding new clearing refund: {}", refund);
        ClearingRefundRecord record = getDslContext().newRecord(CLEARING_REFUND, refund);
        Query query = getDslContext().insertInto(CLEARING_REFUND)
                .set(record)
                .onConflict(
                        CLEARING_REFUND.INVOICE_ID,
                        CLEARING_REFUND.PAYMENT_ID,
                        CLEARING_REFUND.REFUND_ID,
                        CLEARING_REFUND.TRX_VERSION
                )
                .doNothing()
                .returning(CLEARING_REFUND.SEQUENCE_ID);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        log.debug("Clearing refund with invoice id '{}', sequence id '{}' and change id '{}' was added",
                refund.getInvoiceId(), refund.getSequenceId(), refund.getChangeId());
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
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
    public List<ClearingRefund> getReadyClearingRefunds(int providerId, int packageSize) {
        Query query = getDslContext().select(CLEARING_REFUND.fields())
                .from(CLEARING_REFUND)
                .join(CLEARING_TRANSACTION).on(CLEARING_TRANSACTION.INVOICE_ID.eq(CLEARING_REFUND.INVOICE_ID))
                    .and(CLEARING_TRANSACTION.PAYMENT_ID.eq(CLEARING_REFUND.PAYMENT_ID))
                    .and(CLEARING_TRANSACTION.TRX_VERSION.eq(DEFAULT_TRX_VERSION))
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
