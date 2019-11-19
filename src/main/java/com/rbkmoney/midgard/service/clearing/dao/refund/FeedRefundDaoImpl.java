package com.rbkmoney.midgard.service.clearing.dao.refund;

import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.jooq.generated.feed.enums.RefundStatus;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.DEFAULT_TRX_VERSION;
import static org.jooq.generated.feed.Tables.PAYMENT;
import static org.jooq.generated.feed.Tables.REFUND;
import static org.jooq.generated.midgard.tables.ClearingRefund.CLEARING_REFUND;

@Component
public class FeedRefundDaoImpl extends AbstractGenericDao implements RefundDao {

    private final RowMapper<Refund> refundRowMapper;

    public FeedRefundDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
        refundRowMapper = new RecordRowMapper<>(REFUND, Refund.class);
    }

    @Override
    public List<Refund> getRefunds(long sourceRowId, List<Integer> providerIds, int poolSize) {
        Query query = getDslContext().select(REFUND.fields())
                .from(REFUND)
                .join(PAYMENT).on(
                        REFUND.INVOICE_ID.eq(PAYMENT.INVOICE_ID)
                                .and(REFUND.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                                .and(PAYMENT.ROUTE_PROVIDER_ID.in(providerIds))
                                .and(REFUND.ID.greaterThan(sourceRowId))
                                .and(REFUND.STATUS.eq(RefundStatus.succeeded))
                                .and(PAYMENT.CURRENT)
                                .and(REFUND.CURRENT)
                )
                .leftJoin(CLEARING_REFUND).on(
                        CLEARING_REFUND.SOURCE_ROW_ID.greaterThan(sourceRowId)
                                //.and(CLEARING_REFUND.PROVIDER_ID.in(providerIds))
                                .and(REFUND.INVOICE_ID.eq(CLEARING_REFUND.INVOICE_ID))
                                .and(REFUND.PAYMENT_ID.eq(CLEARING_REFUND.PAYMENT_ID))
                                .and(REFUND.REFUND_ID.eq(CLEARING_REFUND.REFUND_ID))
                                .and(CLEARING_REFUND.TRX_VERSION.eq(DEFAULT_TRX_VERSION))
                )
                .where(CLEARING_REFUND.SOURCE_ROW_ID.isNull())
                .orderBy(REFUND.ID)
                //.orderBy(REFUND.SEQUENCE_ID)
                .limit(poolSize);
        return fetch(query, refundRowMapper);
    }

}
