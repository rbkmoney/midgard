package com.rbkmoney.midgard.clearing.helpers.DAO;

import com.rbkmoney.midgard.clearing.data.enums.Bank;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.ClearingDao;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.RecordRowMapper;
import org.jooq.Query;
import org.jooq.generated.midgard.enums.ClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingEvent;
import org.jooq.generated.midgard.tables.records.ClearingEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import javax.sql.DataSource;

import java.util.List;

import static org.jooq.generated.midgard.tables.ClearingEvent.CLEARING_EVENT;

/**
 * Класс для воаимодействия с таблицей clearing_event в базе данных.
 * Данная таблица хранит информацию о клиринговых событиях, которые происходили в системе
 */
public class ClearingInfoDao extends AbstractGenericDao implements ClearingDao<ClearingEvent> {

    /** Логгер */
    private static final Logger log = LoggerFactory.getLogger(ClearingInfoDao.class);
    /** Маппер */
    private final RowMapper<ClearingEvent> clearingEventsRowMapper;

    public ClearingInfoDao(DataSource dataSource) {
        super(dataSource);
        clearingEventsRowMapper = new RecordRowMapper<>(CLEARING_EVENT, ClearingEvent.class);
    }

    @Override
    public Long save(ClearingEvent clearingEvent) {
        log.debug("Adding new clearing event for bank: {}", clearingEvent.getBankName());
        ClearingEventRecord record = getDslContext().newRecord(CLEARING_EVENT, clearingEvent);
        Query query = getDslContext().insertInto(CLEARING_EVENT).set(record);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        log.debug("Clearing event for bank {} have been added", clearingEvent.getBankName());
        return keyHolder.getKey().longValue();
    }

    @Override
    public ClearingEvent get(String id) {
        log.debug("Getting a clearing event info with id {}", id);
        Query query = getDslContext().selectFrom(CLEARING_EVENT)
                .where(CLEARING_EVENT.ID.eq(Long.parseLong(id)));
        ClearingEvent clearingEvents = fetchOne(query, clearingEventsRowMapper);
        log.debug("Clearing event: {}", clearingEvents);
        return clearingEvents;
    }

    /**
     * Получение последнего клирингового эвента
     *
     * @param bank банк, для которого необходимо произвести поиск
     * @param states список состояний, среди которого необходимо произвести поиск
     * @return последний эвент клиринга
     */
    public ClearingEvent getLastClearingEvent(Bank bank, List<ClearingState> states) {
        Query query = getDslContext().selectFrom(CLEARING_EVENT)
                .where((CLEARING_EVENT.BANK_NAME.eq(bank.name())).and(CLEARING_EVENT.STATE.in(states)))
                .orderBy(CLEARING_EVENT.DATE.desc())
                .limit(1);
        ClearingEvent event = fetchOne(query, clearingEventsRowMapper);
        return event;
    }

    /**
     * Обновление состояния для клирингового события
     *
     * @param clearingId ID клирингового события
     * @param state состояние
     */
    public void updateClearingState(Long clearingId, ClearingState state) {
        Query query = getDslContext().update(CLEARING_EVENT)
                .set(CLEARING_EVENT.STATE, state)
                .where(CLEARING_EVENT.ID.eq(clearingId));
        execute(query);
    }

}
