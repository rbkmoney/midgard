package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.service.clearing.handlers.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.midgard.service.clearing.utils.ClearingAdaptersUtils.getClearingAdapter;
import static org.jooq.generated.midgard.enums.ClearingEventStatus.*;

/** Сервис проверки статуса клиринговых событий
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

    private final Handler eventStateRevisionHandler;

    private final Handler clearingDataTransferHandler;

    private final List<ClearingAdapter> adapters;

    @Override
    @Scheduled(fixedDelayString = "${clearing-service.revision}")
    public void process() {
        log.info("Clearing revision process get started");

        List<ClearingEventInfo> startedEvents = eventInfoDao.getAllClearingEvents(STARTED);
        // ADAPTER_FAULT - это ошибка при взаимодействии с клиринговым адаптером.
        List<ClearingEventInfo> adapterFaultEvents = eventInfoDao.getAllClearingEvents(ADAPTER_FAULT).stream()
                .filter(event -> event.getDate().getDayOfYear() - LocalDateTime.now().getDayOfYear() == 0)
                .collect(Collectors.toList());
        startedEvents.addAll(adapterFaultEvents);
        log.info("Count of started clearing events is {}", startedEvents.size());
        startedEvents.forEach(event -> clearingRevision(event, clearingDataTransferHandler));

        List<ClearingEventInfo> executeEvents = eventInfoDao.getAllClearingEvents(EXECUTE);
        log.info("Count of executed clearing events is {}", executeEvents.size());
        executeEvents.forEach(event -> clearingRevision(event, eventStateRevisionHandler));

        log.info("Clearing revision is finished");
    }


    private void clearingRevision(ClearingEventInfo event, Handler<ClearingProcessingEvent> handler) {
        try {
            ClearingAdapter clearingAdapter = getClearingAdapter(adapters, event.getProviderId());
            ClearingProcessingEvent processingEvent = new ClearingProcessingEvent(clearingAdapter, event.getId());
            handler.handle(processingEvent);
        } catch (Exception ex) {
            log.error("Error during a revision", ex);
        }
    }

}
