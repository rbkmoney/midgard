package com.rbkmoney.midgard.base.load.DAO.dominant.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.DAO.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Proxy;
import org.jooq.generated.feed.tables.records.ProxyRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Proxy.PROXY;

@Component
public class ProxyDaoImpl extends AbstractGenericDao implements DomainObjectDao<Proxy, Integer> {

    public ProxyDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Proxy proxy) throws DaoException {
        ProxyRecord proxyRecord = getDslContext().newRecord(PROXY, proxy);
        Query query = getDslContext().insertInto(PROXY).set(proxyRecord).returning(PROXY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer proxyId) throws DaoException {
        Query query = getDslContext().update(PROXY).set(PROXY.CURRENT, false)
                .where(PROXY.PROXY_REF_ID.eq(proxyId).and(PROXY.CURRENT));
        execute(query);
    }
}
