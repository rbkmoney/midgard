package com.rbkmoney.midgard.service.load.dao.dominant.impl;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.load.dao.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
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
