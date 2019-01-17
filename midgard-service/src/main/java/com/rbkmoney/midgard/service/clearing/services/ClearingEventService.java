package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.handlers.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.generated.midgard.enums.ClearingEventStatus.STARTED;

/** Сервис запуска клирингового события
 *
 * Примечание: сначала производится агрегация данных клиринговых транзакций для
 *             определенного провайдера, затем по этим данным сформировываются
 *             пачки и отправляются в адаптер
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ClearingEventService implements ClearingServiceSrv.Iface {

    private final ClearingEventInfoDao clearingEventInfoDao;

    private final Handler clearingEventHandler;

    @Override
    public void startClearingEvent(ClearingEvent clearingEvent) {
        long eventId = clearingEvent.getEventId();
        int providerId = clearingEvent.getProviderId();
        log.info("Starting clearing event for provider id {}", providerId);
        // Подготовка транзакций для клиринга
        Long clearingId = prepareClearingEvent(eventId, providerId);
        // Передача транзакций в клиринговый адаптер
        clearingEventHandler.handle(clearingId);
        log.info("Clearing event for provider id {} finished", providerId);
    }

    @Override
    public ClearingEventStateResponse getClearingEventState(long eventId) throws NoClearingEvent, TException {
        log.info("Getting the state of event {}", eventId);
        ClearingEventInfo clearingEvent = clearingEventInfoDao.getClearingEvent(eventId);
        ClearingEventStateResponse response = new ClearingEventStateResponse();
        if (clearingEvent != null) {
            response.setClearingId(clearingEvent.getId());
            response.setEventId(clearingEvent.getEventId());
            response.setProviderId(clearingEvent.getProviderId());
            response.setClearingState(ClearingEventState.valueOf(clearingEvent.getStatus().name()));
        }
        return response;
    }

    @Transactional
    public Long prepareClearingEvent(long eventId, int providerId) {
        Long clearingId = createNewClearingEvent(eventId, providerId);
        clearingEventInfoDao.prepareTransactionData(clearingId, providerId);
        return clearingId;
    }

    //TODO: рассмотреть вариант, когда для банка присутствует незавершенное клиринговое событие.
    private Long createNewClearingEvent(long eventId, int providerId) {
        log.trace("Creating new clearing event for provider {} by event ", providerId, eventId);
        ClearingEventInfo clearingEvent = new ClearingEventInfo();
        clearingEvent.setProviderId(providerId);
        clearingEvent.setEventId(eventId);
        clearingEvent.setStatus(STARTED);
        return clearingEventInfoDao.save(clearingEvent);
    }

}
