package com.rbkmoney.midgard.base.load.dao.dominant.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.dao.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Category;
import org.jooq.generated.feed.tables.records.CategoryRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Category.CATEGORY;

@Component
public class CategoryDaoImpl extends AbstractGenericDao implements DomainObjectDao<Category, Integer> {

    public CategoryDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Category category) throws DaoException {
        CategoryRecord categoryRecord = getDslContext().newRecord(CATEGORY, category);
        Query query = getDslContext().insertInto(CATEGORY).set(categoryRecord).returning(CATEGORY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer categoryId) throws DaoException {
        Query query = getDslContext().update(CATEGORY).set(CATEGORY.CURRENT, false)
                .where(CATEGORY.CATEGORY_REF_ID.eq(categoryId).and(CATEGORY.CURRENT));
        execute(query);
    }
}
