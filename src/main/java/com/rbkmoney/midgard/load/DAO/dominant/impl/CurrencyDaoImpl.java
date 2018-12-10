package com.rbkmoney.midgard.load.DAO.dominant.impl;

import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.load.DAO.dominant.iface.DomainObjectDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Currency;
import org.jooq.generated.feed.tables.records.CurrencyRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Currency.CURRENCY;

@Component
public class CurrencyDaoImpl extends AbstractGenericDao implements DomainObjectDao<Currency, String> {

    public CurrencyDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Currency currency) throws DaoException {
        CurrencyRecord currencyRecord = getDslContext().newRecord(CURRENCY, currency);
        Query query = getDslContext().insertInto(CURRENCY).set(currencyRecord).returning(CURRENCY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(String currencyId) throws DaoException {
        Query query = getDslContext().update(CURRENCY).set(CURRENCY.CURRENT, false)
                .where(CURRENCY.CURRENCY_REF_ID.eq(currencyId).and(CURRENCY.CURRENT));
        execute(query);
    }
}
