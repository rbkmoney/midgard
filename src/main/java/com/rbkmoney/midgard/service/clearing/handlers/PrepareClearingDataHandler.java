package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.ProviderNotFound;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.exception.AdapterNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.generated.midgard.enums.ClearingEventStatus.STARTED;

@Slf4j
@RequiredArgsConstructor
@Component
public class PrepareClearingDataHandler implements Handler<ClearingEvent> {

    private final ClearingEventInfoDao clearingEventInfoDao;

    @Override
    public void handle(ClearingEvent clearingEvent) throws ProviderNotFound {
        int providerId = clearingEvent.getProviderId();
        try {
            long eventId = clearingEvent.getEventId();
            log.info("Starting clearing event for provider id {} started", providerId);
            // Подготовка транзакций для клиринга
            prepareClearingEvent(eventId, providerId);
            log.info("Clearing data for provider id {} prepared", providerId);
        } catch (AdapterNotFoundException ex) {
            log.error("Error in identification of a provider", ex);
            throw new ProviderNotFound();
        } catch (Exception ex) {
            log.error("Error during clearing event execution", ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
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
