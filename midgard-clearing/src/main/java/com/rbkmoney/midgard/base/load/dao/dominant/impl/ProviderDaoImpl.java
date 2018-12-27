package com.rbkmoney.midgard.base.load.dao.dominant.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.dao.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Provider;
import org.jooq.generated.feed.tables.records.ProviderRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Provider.PROVIDER;

@Component
public class ProviderDaoImpl extends AbstractGenericDao implements DomainObjectDao<Provider, Integer> {

    public ProviderDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Provider provider) throws DaoException {
        ProviderRecord providerRecord = getDslContext().newRecord(PROVIDER, provider);
        Query query = getDslContext().insertInto(PROVIDER).set(providerRecord).returning(PROVIDER.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer providerId) throws DaoException {
        Query query = getDslContext().update(PROVIDER).set(PROVIDER.CURRENT, false)
                .where(PROVIDER.PROVIDER_REF_ID.eq(providerId).and(PROVIDER.CURRENT));
        execute(query);
    }
}
