package com.rbkmoney.midgard.service.clearing.dao.clearing_info;

import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.routines.PrepareTransactionData;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.jooq.generated.midgard.tables.records.ClearingEventInfoRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static org.jooq.generated.midgard.tables.ClearingEventInfo.CLEARING_EVENT_INFO;

/**
 * Класс для воаимодействия с таблицей clearing_event в базе данных.
 * Данная таблица хранит информацию о клиринговых событиях, которые происходили в системе
 */
@Slf4j
@Component
public class ClearingEventInfoDaoImpl extends AbstractGenericDao implements ClearingEventInfoDao {

    private final RowMapper<ClearingEventInfo> clearingEventsRowMapper =
            new RecordRowMapper<>(CLEARING_EVENT_INFO, ClearingEventInfo.class);

    public ClearingEventInfoDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(ClearingEventInfo clearingEvent) {
        log.info("Adding new clearing event for provider: {}", clearingEvent.getProviderId());
        ClearingEventInfoRecord record = getDslContext().newRecord(CLEARING_EVENT_INFO, clearingEvent);
        Query query = getDslContext().insertInto(CLEARING_EVENT_INFO).set(record).returning(CLEARING_EVENT_INFO.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        log.info("Clearing event for provider {} have been added", clearingEvent.getProviderId());
        return keyHolder.getKey().longValue();
    }

    @Override
    public ClearingEventInfo get(Long id) {
        log.debug("Getting a clearing event info with id {}", id);
        Query query = getDslContext().selectFrom(CLEARING_EVENT_INFO)
                .where(CLEARING_EVENT_INFO.ID.eq(id));
        ClearingEventInfo clearingEvent = fetchOne(query, clearingEventsRowMapper);
        log.debug("Extracted clearing event info by id: {}", clearingEvent);
        return clearingEvent;
    }

    @Override
    public ClearingEventInfo getClearingEvent(long eventId) {
        log.debug("Getting a clearing event info for event ID {}", eventId);
        Query query = getDslContext().selectFrom(CLEARING_EVENT_INFO)
                .where(CLEARING_EVENT_INFO.EVENT_ID.eq(eventId));
        ClearingEventInfo clearingEvent = fetchOne(query, clearingEventsRowMapper);
        log.debug("Extracted clearing event info by event id: {}", clearingEvent);
        return clearingEvent;
    }

    @Override
    public ClearingEventInfo getLastClearingEvent(int providerId) {
        log.debug("Getting the last clearing event for provider ID {}", providerId);
        Query query = getDslContext().selectFrom(CLEARING_EVENT_INFO)
                .where(CLEARING_EVENT_INFO.PROVIDER_ID.eq(providerId))
                .orderBy(CLEARING_EVENT_INFO.ID.desc())
                .limit(1);
        ClearingEventInfo clearingEvent = fetchOne(query, clearingEventsRowMapper);
        log.debug("Extracted last clearing event info by provider id {}: {}", providerId, clearingEvent);
        return clearingEvent;
    }

    @Override
    public void updateClearingStatus(Long clearingId, ClearingEventStatus status) {
        Query query = getDslContext().update(CLEARING_EVENT_INFO)
                .set(CLEARING_EVENT_INFO.STATUS, status)
                .where(CLEARING_EVENT_INFO.ID.eq(clearingId));
        execute(query);
    }

    @Override
    public void updateClearingStatus(Long eventId, Integer providerId, ClearingEventStatus status) {
        Query query = getDslContext().update(CLEARING_EVENT_INFO)
                .set(CLEARING_EVENT_INFO.STATUS, status)
                .where(CLEARING_EVENT_INFO.EVENT_ID.eq(eventId))
                .and(CLEARING_EVENT_INFO.PROVIDER_ID.eq(providerId));
        execute(query);
    }

    @Override
    public List<ClearingEventInfo> getAllClearingEvents(ClearingEventStatus status) {
        Query query = getDslContext().selectFrom(CLEARING_EVENT_INFO)
                .where(CLEARING_EVENT_INFO.STATUS.eq(status));
        return fetch(query, clearingEventsRowMapper);
    }

    @Override
    public Long prepareTransactionData(long clearingId, int providerId) {
        PrepareTransactionData prepareTransactionData = new PrepareTransactionData();
        prepareTransactionData.setSrcClearingId(clearingId);
        prepareTransactionData.setSrcProviderId(providerId);
        executeProc(prepareTransactionData);
        return clearingId;
    }

}
