package com.rbkmoney.midgard.load.DAO.dominant.impl;

import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.load.DAO.dominant.iface.DomainObjectDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.PayoutMethod;
import org.jooq.generated.feed.tables.records.PayoutMethodRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.PayoutMethod.PAYOUT_METHOD;

@Component
public class PayoutMethodDaoImpl extends AbstractGenericDao implements DomainObjectDao<PayoutMethod, String> {

    public PayoutMethodDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(PayoutMethod payoutMethod) throws DaoException {
        PayoutMethodRecord payoutMethodRecord = getDslContext().newRecord(PAYOUT_METHOD, payoutMethod);
        Query query = getDslContext().insertInto(PAYOUT_METHOD).set(payoutMethodRecord).returning(PAYOUT_METHOD.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(String payoutMethodId) throws DaoException {
        Query query = getDslContext().update(PAYOUT_METHOD).set(PAYOUT_METHOD.CURRENT, false)
                .where(PAYOUT_METHOD.PAYOUT_METHOD_REF_ID.eq(payoutMethodId).and(PAYOUT_METHOD.CURRENT));
        execute(query);
    }
}
