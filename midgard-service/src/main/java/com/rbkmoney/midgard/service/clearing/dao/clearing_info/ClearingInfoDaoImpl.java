package com.rbkmoney.midgard.service.clearing.dao.clearing_info;

import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
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
public class ClearingInfoDaoImpl extends AbstractGenericDao implements ClearingInfoDao {

    private final RowMapper<ClearingEvent> clearingEventsRowMapper;

    public ClearingInfoDaoImpl(DataSource dataSource) {
        super(dataSource);
        clearingEventsRowMapper = new RecordRowMapper<>(CLEARING_EVENT, ClearingEvent.class);
    }

    @Override
    public Long save(ClearingEvent clearingEvent) throws DaoException {
        log.debug("Adding new clearing event for provider: {}", clearingEvent.getProviderId());
        ClearingEventRecord record = getDslContext().newRecord(CLEARING_EVENT, clearingEvent);
        Query query = getDslContext().insertInto(CLEARING_EVENT).set(record);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        log.debug("Clearing event for provider {} have been added", clearingEvent.getProviderId());
        return keyHolder.getKey().longValue();
    }

    @Override
    public ClearingEvent get(String id) throws DaoException {
        log.debug("Getting a clearing event info with id {}", id);
        Query query = getDslContext().selectFrom(CLEARING_EVENT)
                .where(CLEARING_EVENT.ID.eq(Long.parseLong(id)));
        ClearingEvent clearingEvent = fetchOne(query, clearingEventsRowMapper);
        log.debug("Clearing event: {}", clearingEvent);
        return clearingEvent;
    }

    @Override
    public ClearingEvent getClearingEvent(long eventId) throws DaoException {
        log.debug("Getting a clearing event info for event ID {}", eventId);
        Query query = getDslContext().selectFrom(CLEARING_EVENT)
                .where(CLEARING_EVENT.EVENT_ID.eq(eventId));
        ClearingEvent clearingEvent = fetchOne(query, clearingEventsRowMapper);
        log.debug("Clearing event: {}", clearingEvent);
        return clearingEvent;
    }

    @Override
    public void updateClearingStatus(Long clearingId, ClearingEventStatus state) throws DaoException {
        Query query = getDslContext().update(CLEARING_EVENT)
                .set(CLEARING_EVENT.STATUS, state)
                .where(CLEARING_EVENT.ID.eq(clearingId));
        execute(query);
    }

    @Override
    public List<ClearingEvent> getClearingEventsByStatus(ClearingEventStatus state) throws DaoException {
        Query query = getDslContext().selectFrom(CLEARING_EVENT)
                .where(CLEARING_EVENT.STATUS.eq(state));
        return fetch(query, clearingEventsRowMapper);
    }

}
