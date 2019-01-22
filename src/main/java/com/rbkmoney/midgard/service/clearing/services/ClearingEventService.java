package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.service.clearing.exception.AdapterNotFoundException;
import com.rbkmoney.midgard.service.clearing.handlers.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private final List<ClearingAdapter> adapters;

    @Override
    public void startClearingEvent(ClearingEvent clearingEvent) throws ProviderNotFound {
        if (clearingEvent == null) {
            log.error("Command from external system is empty");
        } else {
            Long eventId = clearingEvent.getEventId();
            if (checkEventExisting(eventId)) {
                log.error("For a event with id " + eventId + " a clearing event already exists");
            } else {
                runClearingEvent(clearingEvent);
            }
        }
    }

    private void runClearingEvent(ClearingEvent clearingEvent) throws ProviderNotFound {
        try {
            long eventId = clearingEvent.getEventId();
            int providerId = clearingEvent.getProviderId();
            log.info("Starting clearing event for provider id {}", providerId);
            ClearingAdapter clearingAdapter = adapters.stream()
                    .filter(clrAdapter -> clrAdapter.getAdapterId() == providerId)
                    .findFirst()
                    .orElseThrow(() ->
                            new AdapterNotFoundException("Adapter with provider id " + providerId + " not found"));
            // Подготовка транзакций для клиринга
            Long clearingId = prepareClearingEvent(eventId, providerId);
            // Передача транзакций в клиринговый адаптер
            ClearingProcessingEvent event = new ClearingProcessingEvent(clearingAdapter, clearingId);
            clearingEventHandler.handle(event);
            log.info("Clearing event for provider id {} finished", providerId);
        } catch (AdapterNotFoundException ex) {
            log.error("Error in identification a provider", ex);
            throw new ProviderNotFound();
        } catch (Exception ex) {
            log.error("Error during clearing event execution", ex);
        }
    }

    private boolean checkEventExisting(long eventId) {
        ClearingEventInfo eventInfo = clearingEventInfoDao.getClearingEvent(eventId);
        if (eventInfo == null) {
            return false;
        } else {
            return true;
        }
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

    private Long createNewClearingEvent(long eventId, int providerId) {
        log.trace("Creating new clearing event for provider {} by event ", providerId, eventId);
        ClearingEventInfo clearingEvent = new ClearingEventInfo();
        clearingEvent.setProviderId(providerId);
        clearingEvent.setEventId(eventId);
        clearingEvent.setStatus(STARTED);
        return clearingEventInfoDao.save(clearingEvent);
    }

}
