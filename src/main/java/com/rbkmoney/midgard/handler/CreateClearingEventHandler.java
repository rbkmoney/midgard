package com.rbkmoney.midgard.handler;

import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.ProviderNotFound;
import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.utils.ClearingAdaptersUtils;
import com.rbkmoney.midgard.exception.AdapterNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.jooq.generated.midgard.enums.ClearingEventStatus.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateClearingEventHandler implements Handler<ClearingEvent> {

    private final ClearingEventInfoDao clearingEventInfoDao;

    private final Handler<ClearingProcessingEvent> prepareClearingDataHandler;

    private final List<ClearingAdapter> adapters;

    @Override
    @Transactional
    public void handle(ClearingEvent clearingEvent) throws ProviderNotFound {
        int providerId = clearingEvent.getProviderId();
        long eventId = clearingEvent.getEventId();
        try {
            // Подготовка транзакций для клиринга
            log.info("Starting clearing event for provider id {} started", providerId);
            ClearingAdapter clearingAdapter = ClearingAdaptersUtils.getClearingAdapter(adapters, clearingEvent.getProviderId());
            Long clearingId = createNewClearingEvent(eventId, providerId);
            prepareClearingDataHandler.handle(new ClearingProcessingEvent(clearingAdapter, clearingId));
            clearingEventInfoDao.updateClearingStatus(clearingId, STARTED);
            log.info("Clearing event {} was created. Clearing data for provider id {} prepared",
                    clearingId, providerId);
        } catch (AdapterNotFoundException ex) {
            log.error("Error in identification of a provider {} for event id {}", providerId, eventId, ex);
            throw new ProviderNotFound();
        } catch (Exception ex) {
            log.error("Error during clearing event {} for provider id {} execution", eventId, providerId, ex);
        }
    }

    private Long createNewClearingEvent(long eventId, int providerId) {
        log.trace("Creating new clearing event for provider {} by event ", providerId, eventId);
        ClearingEventInfo clearingEvent = new ClearingEventInfo();
        clearingEvent.setProviderId(providerId);
        clearingEvent.setEventId(eventId);
        clearingEvent.setStatus(CREATED);
        return clearingEventInfoDao.save(clearingEvent);
    }

}