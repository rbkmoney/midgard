package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.exception.AdapterNotFoundException;
import com.rbkmoney.midgard.service.clearing.handlers.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.rbkmoney.midgard.service.clearing.utils.ClearingAdaptersUtils.getClearingAdapter;

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

    private final Handler createClearingEventHandler;

    private final List<ClearingAdapter> adapters;

    @Override
    public void startClearingEvent(ClearingEvent clearingEvent) throws ProviderNotFound {
        if (clearingEvent == null) {
            log.error("The command from external system is empty");
        } else {
            log.info("The command to execute the event with outer id {} for provider {} was received",
                    clearingEvent.getEventId(), clearingEvent.getProviderId());
            executeClearingEvent(clearingEvent);
        }
    }

    private void executeClearingEvent(ClearingEvent clearingEvent) throws ProviderNotFound {
        try {
            Long eventId = clearingEvent.getEventId();
            if (clearingEventInfoDao.getClearingEvent(eventId) == null) {
                getClearingAdapter(adapters, clearingEvent.getProviderId());

                createClearingEventHandler.handle(clearingEvent);
            } else {
                log.warn("For a event with id " + eventId + " a clearing event already exists");
            }
        } catch (AdapterNotFoundException ex) {
            log.error("Error in identification of a provider", ex);
            throw new ProviderNotFound();
        } catch (Exception ex) {
            log.error("Error preparing clearing data for provider with id " + clearingEvent.getProviderId(), ex);
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

    @Override
    public void resendClearingFile(int providerId, long eventId) throws NoClearingEvent, TException {
        log.info("Resend clearing file for provider id {} and event id {}. Update event status get started",
                providerId, eventId);
        clearingEventInfoDao.updateClearingStatus(eventId, providerId, ClearingEventStatus.STARTED);
        log.info("Resend clearing file for provider id {} and event id {}. Update event status finished",
                providerId, eventId);
    }


}
