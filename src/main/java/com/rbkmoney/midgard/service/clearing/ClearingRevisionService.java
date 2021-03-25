package com.rbkmoney.midgard.service.clearing;

import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.utils.ClearingAdaptersUtils;
import com.rbkmoney.midgard.handler.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.rbkmoney.midgard.domain.enums.ClearingEventStatus;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.midgard.domain.enums.ClearingEventStatus.*;

/**
 * Сервис проверки статуса клиринговых событий.
 *
 * Примечание: так как ответ от банка может поступить с задержкой необходимо с
 *             определенной периодчиностью опрашивать адаптер на предмет ответа;
 *             также возможен перезапуск события в случае какой-либо ошибки
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ClearingRevisionService implements GenericService {

    private final ClearingEventInfoDao eventInfoDao;

    private final Handler<ClearingProcessingEvent> eventStateRevisionHandler;

    private final Handler<ClearingProcessingEvent> prepareClearingDataHandler;

    private final Handler<ClearingProcessingEvent> clearingDataTransferHandler;

    private final List<ClearingAdapter> adapters;

    @Value("${clearing-service.retries-hour-count}")
    private long retriesHourCount;

    @Override
    @Scheduled(fixedDelayString = "${clearing-service.revision}")
    public void process() {
        log.info("Clearing revision process get started");

        processClearingEventsByStatus(CREATED, prepareClearingDataHandler);
        processClearingEventsByStatus(STARTED, clearingDataTransferHandler);
        processAdapterFaultClearingEvents();
        processClearingEventsByStatus(EXECUTE, eventStateRevisionHandler);

        log.info("Clearing revision is finished");
    }

    private void processAdapterFaultClearingEvents() {
        // ADAPTER_FAULT - это ошибка при взаимодействии с клиринговым адаптером.
        List<ClearingEventInfo> adapterFaultEvents =
                eventInfoDao.getAllClearingEventsByStatus(ADAPTER_FAULT).stream()
                .filter(event ->
                        event.getDate().plusHours(retriesHourCount).isAfter(LocalDateTime.now(Clock.systemUTC())))
                .collect(Collectors.toList());
        log.info("Count of 'ADAPTER FAULT' clearing events: {} ", adapterFaultEvents.size());
        adapterFaultEvents.forEach(event -> clearingRevision(event, clearingDataTransferHandler));
    }

    private void processClearingEventsByStatus(ClearingEventStatus status, Handler<ClearingProcessingEvent> handler) {
        List<ClearingEventInfo> clearingEvents = eventInfoDao.getAllClearingEventsByStatus(status);
        if (clearingEvents.size() > 0) {
            log.info("Count of '{}' clearing events: {}", status, clearingEvents.size());
            clearingEvents.forEach(event -> clearingRevision(event, handler));
        }
    }

    private void clearingRevision(ClearingEventInfo event, Handler<ClearingProcessingEvent> handler) {
        try {
            ClearingAdapter clearingAdapter = ClearingAdaptersUtils.getClearingAdapter(adapters, event.getProviderId());
            ClearingProcessingEvent processingEvent = new ClearingProcessingEvent(clearingAdapter, event.getEventId());
            handler.handle(processingEvent);
        } catch (Exception ex) {
            log.error("Error during a revision", ex);
        }
    }

}
