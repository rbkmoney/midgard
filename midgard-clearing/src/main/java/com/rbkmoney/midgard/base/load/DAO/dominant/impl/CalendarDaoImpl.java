package com.rbkmoney.midgard.base.load.DAO.dominant.impl;


import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.DAO.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Calendar;
import org.jooq.generated.feed.tables.records.CalendarRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Calendar.CALENDAR;

@Component
public class CalendarDaoImpl extends AbstractGenericDao implements DomainObjectDao<Calendar, Integer> {

    public CalendarDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Calendar calendar) {
        CalendarRecord calendarRecord = getDslContext().newRecord(CALENDAR, calendar);
        Query query = getDslContext().insertInto(CALENDAR).set(calendarRecord).returning(CALENDAR.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer calendarId) throws DaoException {
        Query query = getDslContext().update(CALENDAR).set(CALENDAR.CURRENT, false)
                .where(CALENDAR.CALENDAR_REF_ID.eq(calendarId).and(CALENDAR.CURRENT));
        execute(query);
    }
}
