package com.rbkmoney.midgard.base.clearing.helpers.dao;

import com.rbkmoney.midgard.base.clearing.helpers.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.ClearingDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.RecordRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.generated.midgard.enums.ClearingEventState;
import org.jooq.generated.midgard.tables.pojos.ClearingEvent;
import org.jooq.generated.midgard.tables.records.ClearingEventRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import java.util.List;

import static org.jooq.generated.midgard.tables.ClearingEvent.CLEARING_EVENT;

/**
 * Класс для воаимодействия с таблицей clearing_event в базе данных.
 * Данная таблица хранит информацию о клиринговых событиях, которые происходили в системе
 */
@Slf4j
@Component
public class ClearingInfoDao extends AbstractGenericDao implements ClearingDao<ClearingEvent> {

    private final RowMapper<ClearingEvent> clearingEventsRowMapper;

    public ClearingInfoDao(DataSource dataSource) {
        super(dataSource);
        clearingEventsRowMapper = new RecordRowMapper<>(CLEARING_EVENT, ClearingEvent.class);
    }

    @Override
    public Long save(ClearingEvent clearingEvent) {
        log.debug("Adding new clearing event for provider: {}", clearingEvent.getProviderId());
        ClearingEventRecord record = getDslContext().newRecord(CLEARING_EVENT, clearingEvent);
        Query query = getDslContext().insertInto(CLEARING_EVENT).set(record);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        log.debug("Clearing event for provider {} have been added", clearingEvent.getProviderId());
        return keyHolder.getKey().longValue();
    }

    @Override
    public ClearingEvent get(String id) {
        log.debug("Getting a clearing event info with id {}", id);
        Query query = getDslContext().selectFrom(CLEARING_EVENT)
                .where(CLEARING_EVENT.ID.eq(Long.parseLong(id)));
        ClearingEvent clearingEvent = fetchOne(query, clearingEventsRowMapper);
        log.debug("Clearing event: {}", clearingEvent);
        return clearingEvent;
    }

    public ClearingEvent getClearingEvent(long eventId) {
        log.debug("Getting a clearing event info for event ID {}", eventId);
        Query query = getDslContext().selectFrom(CLEARING_EVENT)
                .where(CLEARING_EVENT.EVENT_ID.eq(eventId));
        ClearingEvent clearingEvent = fetchOne(query, clearingEventsRowMapper);
        log.debug("Clearing event: {}", clearingEvent);
        return clearingEvent;
    }

    public ClearingEvent getLastClearingEvent(String providerId, List<ClearingEventState> states) {
        Query query = getDslContext().selectFrom(CLEARING_EVENT)
                .where((CLEARING_EVENT.PROVIDER_ID.eq(providerId)).and(CLEARING_EVENT.STATE.in(states)))
                .orderBy(CLEARING_EVENT.DATE.desc())
                .limit(1);
        ClearingEvent event = fetchOne(query, clearingEventsRowMapper);
        return event;
    }

    public void updateClearingState(Long clearingId, ClearingEventState state) {
        Query query = getDslContext().update(CLEARING_EVENT)
                .set(CLEARING_EVENT.STATE, state)
                .where(CLEARING_EVENT.ID.eq(clearingId));
        execute(query);
    }

    public List<ClearingEvent> getClearingEventsByState(ClearingEventState state) {
        Query query = getDslContext().selectFrom(CLEARING_EVENT)
                .where(CLEARING_EVENT.STATE.eq(state));
        return fetch(query, clearingEventsRowMapper);
    }

}
