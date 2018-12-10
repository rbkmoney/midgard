package com.rbkmoney.midgard.load.DAO.dominant.impl;

import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.load.DAO.dominant.iface.DomainObjectDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Inspector;
import org.jooq.generated.feed.tables.records.InspectorRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Inspector.INSPECTOR;

@Component
public class InspectorDaoImpl extends AbstractGenericDao implements DomainObjectDao<Inspector, Integer> {

    public InspectorDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Inspector inspector) throws DaoException {
        InspectorRecord inspectorRecord = getDslContext().newRecord(INSPECTOR, inspector);
        Query query = getDslContext().insertInto(INSPECTOR).set(inspectorRecord).returning(INSPECTOR.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer inspectorId) throws DaoException {
        Query query = getDslContext().update(INSPECTOR).set(INSPECTOR.CURRENT, false)
                .where(INSPECTOR.INSPECTOR_REF_ID.eq(inspectorId).and(INSPECTOR.CURRENT));
        execute(query);
    }
}
