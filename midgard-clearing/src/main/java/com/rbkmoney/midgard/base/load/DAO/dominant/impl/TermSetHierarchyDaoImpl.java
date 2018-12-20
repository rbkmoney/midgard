package com.rbkmoney.midgard.base.load.DAO.dominant.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.DAO.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.TermSetHierarchy;
import org.jooq.generated.feed.tables.records.TermSetHierarchyRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.TermSetHierarchy.TERM_SET_HIERARCHY;

@Component
public class TermSetHierarchyDaoImpl extends AbstractGenericDao implements DomainObjectDao<TermSetHierarchy, Integer> {

    public TermSetHierarchyDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(TermSetHierarchy termSetHierarchy) throws DaoException {
        TermSetHierarchyRecord termSetHierarchyRecord = getDslContext().newRecord(TERM_SET_HIERARCHY, termSetHierarchy);
        Query query = getDslContext().insertInto(TERM_SET_HIERARCHY).set(termSetHierarchyRecord).returning(TERM_SET_HIERARCHY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer termSetHierarchyId) throws DaoException {
        Query query = getDslContext().update(TERM_SET_HIERARCHY).set(TERM_SET_HIERARCHY.CURRENT, false)
                .where(TERM_SET_HIERARCHY.TERM_SET_HIERARCHY_REF_ID.eq(termSetHierarchyId).and(TERM_SET_HIERARCHY.CURRENT));
        execute(query);
    }
}
