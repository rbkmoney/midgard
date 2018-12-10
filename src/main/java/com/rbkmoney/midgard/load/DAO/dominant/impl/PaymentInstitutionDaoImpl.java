package com.rbkmoney.midgard.load.DAO.dominant.impl;

import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.load.DAO.dominant.iface.DomainObjectDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.PaymentInstitution;
import org.jooq.generated.feed.tables.records.PaymentInstitutionRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.PaymentInstitution.PAYMENT_INSTITUTION;

@Component
public class PaymentInstitutionDaoImpl extends AbstractGenericDao implements DomainObjectDao<PaymentInstitution, Integer> {

    public PaymentInstitutionDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(PaymentInstitution paymentInstitution) throws DaoException {
        PaymentInstitutionRecord paymentInstitutionRecord = getDslContext().newRecord(PAYMENT_INSTITUTION, paymentInstitution);
        Query query = getDslContext().insertInto(PAYMENT_INSTITUTION).set(paymentInstitutionRecord).returning(PAYMENT_INSTITUTION.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer paymentInstitutionId) throws DaoException {
        Query query = getDslContext().update(PAYMENT_INSTITUTION).set(PAYMENT_INSTITUTION.CURRENT, false)
                .where(PAYMENT_INSTITUTION.PAYMENT_INSTITUTION_REF_ID.eq(paymentInstitutionId).and(PAYMENT_INSTITUTION.CURRENT));
        execute(query);
    }
}
