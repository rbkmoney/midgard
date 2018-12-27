package com.rbkmoney.midgard.base.clearing.helpers.dao;

import com.rbkmoney.midgard.base.clearing.helpers.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.RecordRowMapper;
import org.jooq.Query;
import org.jooq.generated.feed.enums.PaymentStatus;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import java.util.List;

import static org.jooq.generated.feed.tables.Payment.PAYMENT;

@Component
public class PaymentDao extends AbstractGenericDao {

    private final RowMapper<Payment> paymentRowMapper;

    public PaymentDao(DataSource dataSource) {
        super(dataSource);
        paymentRowMapper = new RecordRowMapper<>(PAYMENT, Payment.class);
    }

    public List<Payment> getPayments(long eventId, int poolSize) {
        Query query = getDslContext().selectFrom(PAYMENT)
                .where(PAYMENT.EVENT_ID.greaterThan(eventId))
                .and(PAYMENT.STATUS.eq(PaymentStatus.captured))
                .orderBy(PAYMENT.EVENT_ID).limit(poolSize);
        return fetch(query, paymentRowMapper);
    }

}
