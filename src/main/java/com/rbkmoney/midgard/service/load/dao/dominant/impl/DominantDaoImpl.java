package com.rbkmoney.midgard.service.load.dao.dominant.impl;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.load.dao.dominant.iface.DominantDao;
import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.Tables.*;

@Component
public class DominantDaoImpl extends AbstractGenericDao implements DominantDao {

    public DominantDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long getLastVersionId() throws DaoException {
        Query query = getDslContext().select(DSL.max(DSL.field("version_id"))).from(
                getDslContext().select(CALENDAR.VERSION_ID.max().as("version_id")).from(CALENDAR)
                        .unionAll(getDslContext().select(CATEGORY.VERSION_ID.max().as("version_id")).from(CATEGORY))
                        .unionAll(getDslContext().select(CURRENCY.VERSION_ID.max().as("version_id")).from(CURRENCY))
                        .unionAll(getDslContext().select(INSPECTOR.VERSION_ID.max().as("version_id")).from(INSPECTOR))
                        .unionAll(getDslContext().select(PAYMENT_INSTITUTION.VERSION_ID.max().as("version_id")).from(PAYMENT_INSTITUTION))
                        .unionAll(getDslContext().select(PAYMENT_METHOD.VERSION_ID.max().as("version_id")).from(PAYMENT_METHOD))
                        .unionAll(getDslContext().select(PAYOUT_METHOD.VERSION_ID.max().as("version_id")).from(PAYOUT_METHOD))
                        .unionAll(getDslContext().select(PROVIDER.VERSION_ID.max().as("version_id")).from(PROVIDER))
                        .unionAll(getDslContext().select(PROXY.VERSION_ID.max().as("version_id")).from(PROXY))
                        .unionAll(getDslContext().select(TERMINAL.VERSION_ID.max().as("version_id")).from(TERMINAL))
                        .unionAll(getDslContext().select(TERM_SET_HIERARCHY.VERSION_ID.max().as("version_id")).from(TERM_SET_HIERARCHY))
        );
        return fetchOne(query, Long.class);
    }
}
