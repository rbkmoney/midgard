package com.rbkmoney.midgard.service.clearing;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.exception.AdapterNotFoundException;
import com.rbkmoney.midgard.utils.ClearingAdaptersUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import com.rbkmoney.midgard.domain.enums.ClearingEventStatus;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventInfo;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.rbkmoney.midgard.domain.enums.ClearingEventStatus.CREATED;

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
        int providerId = clearingEvent.getProviderId();
        try {
            Long eventId = clearingEvent.getEventId();
            if (clearingEventInfoDao.getClearingEvent(eventId, providerId) == null) {
                ClearingAdaptersUtils.getClearingAdapter(adapters, providerId);
                log.info("Creating new clearing event for provider {} by event: {}", providerId, eventId);
                ClearingEventInfo clearingEventInfo = new ClearingEventInfo();
                clearingEventInfo.setProviderId(providerId);
                clearingEventInfo.setEventId(eventId);
                clearingEventInfo.setStatus(CREATED);
                Long clearingId = clearingEventInfoDao.save(clearingEventInfo);
                log.info("New clearing event for provider {} was created with number {} (event: {})",
                        providerId, clearingId, eventId);
            } else {
                log.warn("For a event with id " + eventId + " a clearing event already exists");
            }
        } catch (AdapterNotFoundException ex) {
            log.error("Error in identification of a provider", ex);
            throw new ProviderNotFound();
        } catch (Exception ex) {
            log.error("Error preparing clearing data for provider with id " + providerId, ex);
        }
    }

    @Override
    public ClearingEventStateResponse getClearingEventState(int providerId, long eventId)
            throws NoClearingEvent, TException {
        log.info("Getting the state of event {}", eventId);
        ClearingEventInfo clearingEvent = clearingEventInfoDao.getClearingEvent(eventId, providerId);
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
