package com.rbkmoney.midgard.base.load.DAO.dominant.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.DAO.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.PaymentMethod;
import org.jooq.generated.feed.tables.records.PaymentMethodRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.PaymentMethod.PAYMENT_METHOD;

@Component
public class PaymentMethodDaoImpl extends AbstractGenericDao implements DomainObjectDao<PaymentMethod, String> {

    public PaymentMethodDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(PaymentMethod paymentMethod) throws DaoException {
        PaymentMethodRecord paymentMethodRecord = getDslContext().newRecord(PAYMENT_METHOD, paymentMethod);
        Query query = getDslContext().insertInto(PAYMENT_METHOD).set(paymentMethodRecord).returning(PAYMENT_METHOD.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(String paymentMethodId) throws DaoException {
        Query query = getDslContext().update(PAYMENT_METHOD).set(PAYMENT_METHOD.CURRENT, false)
                .where(PAYMENT_METHOD.PAYMENT_METHOD_REF_ID.eq(paymentMethodId).and(PAYMENT_METHOD.CURRENT));
        execute(query);
    }
}
