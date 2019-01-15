package com.rbkmoney.midgard.service.clearing.helpers.DAO;

import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.ClearingDao;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.RecordRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.generated.midgard.Tables;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.records.ClearingRefundRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.midgard.tables.ClearingRefund.CLEARING_REFUND;

@Slf4j
@Component
public class ClearingRefundDao extends AbstractGenericDao implements ClearingDao<ClearingRefund> {

    private final RowMapper<ClearingRefund> clearingRefundRowMapper;

    public ClearingRefundDao(DataSource dataSource) {
        super(dataSource);
        clearingRefundRowMapper = new RecordRowMapper<>(CLEARING_REFUND, ClearingRefund.class);
    }

    @Override
    public Long save(ClearingRefund clearingRefund) {
        log.debug("Adding new clearing refund: {}", clearingRefund);
        ClearingRefundRecord record = getDslContext().newRecord(CLEARING_REFUND, clearingRefund);
        Query query = getDslContext().insertInto(CLEARING_REFUND).set(record);
        int addedRows = execute(query);
        log.debug("New clearing refund with event id {} was added", clearingRefund.getEventId());
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

    public ClearingRefund getRefund(String transactionId) {
        Query query = getDslContext().selectFrom(CLEARING_REFUND)
                .where(CLEARING_REFUND.TRANSACTION_ID.eq(transactionId));
        ClearingRefund clearingRefund = fetchOne(query, clearingRefundRowMapper);
        log.debug("Refund with transaction id {} {}", transactionId, clearingRefund == null ? "not found" : "found");
        return clearingRefund;
    }


}
