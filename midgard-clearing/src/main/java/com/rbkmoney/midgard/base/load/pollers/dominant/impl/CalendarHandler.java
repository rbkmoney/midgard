package com.rbkmoney.midgard.base.load.pollers.dominant.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.rbkmoney.damsel.domain.CalendarObject;
import com.rbkmoney.midgard.base.load.DAO.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.load.DAO.dominant.impl.CalendarDaoImpl;
import com.rbkmoney.midgard.base.load.pollers.dominant.AbstractDominantHandler;
import com.rbkmoney.midgard.base.load.utils.JsonUtil;
import org.jooq.generated.feed.tables.pojos.Calendar;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CalendarHandler extends AbstractDominantHandler<CalendarObject, Calendar, Integer> {

    private final CalendarDaoImpl calendarDao;

    public CalendarHandler(CalendarDaoImpl calendarDao) {
        this.calendarDao = calendarDao;
    }

    @Override
    protected DomainObjectDao<Calendar, Integer> getDomainObjectDao() {
        return calendarDao;
    }

    @Override
    protected CalendarObject getObject() {
        return getDomainObject().getCalendar();
    }

    @Override
    protected Integer getObjectRefId() {
        return getObject().getRef().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetCalendar();
    }

    @Override
    public Calendar convertToDatabaseObject(CalendarObject calendarObject, Long versionId, boolean current) {
        Calendar calendar = new Calendar();
        calendar.setVersionId(versionId);
        calendar.setCalendarRefId(getObjectRefId());
        com.rbkmoney.damsel.domain.Calendar data = calendarObject.getData();
        calendar.setName(data.getName());
        calendar.setDescription(data.getDescription());
        calendar.setTimezone(data.getTimezone());
        if (data.isSetFirstDayOfWeek()) {
            calendar.setFirstDayOfWeek(data.getFirstDayOfWeek().getValue());
        }
        Map<Integer, Set<JsonNode>> holidaysJsonNodeMap = data.getHolidays().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue()
                                .stream()
                                .map(JsonUtil::tBaseToJsonNode)
                                .collect(Collectors.toSet())));
        calendar.setHolidaysJson(JsonUtil.objectToJsonString(holidaysJsonNodeMap));
        calendar.setCurrent(current);
        return calendar;
    }
}
