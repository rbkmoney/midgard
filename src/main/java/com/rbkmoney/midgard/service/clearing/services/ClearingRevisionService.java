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

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.rbkmoney.midgard.service.clearing.utils.ClearingAdaptersUtils.getClearingAdapter;

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

    //TODO: переделать в сервис именованных блокировок
    private final static ReentrantLock lock = new ReentrantLock();

    @Override
    @Scheduled(fixedDelayString = "${clearing-service.revision}")
    public void process() {
        log.info("Clearing revision process get started");
        if (lock.tryLock()) {
            try {
                lock.lock();
                List<ClearingEventInfo> startedEvents = eventInfoDao.getAllClearingEvents(ClearingEventStatus.STARTED);
                log.debug("Count of started clearing event is {}", startedEvents.size());
                runRevision(startedEvents, clearingDataTransferHandler);

                List<ClearingEventInfo> executeEvents = eventInfoDao.getAllClearingEvents(ClearingEventStatus.EXECUTE);
                log.debug("Count of executed clearing event is {}", executeEvents.size());
                runRevision(executeEvents, eventStateRevisionHandler);

            } finally {
                lock.unlock();
            }
        } else {
            log.debug("Clearing revision is running. New task is not started");
        }

        log.info("Clearing revision is finished");

    }

    private void runRevision(List<ClearingEventInfo> events, Handler<ClearingProcessingEvent> handler) {
        try {
            for (ClearingEventInfo event : events) {
                ClearingAdapter clearingAdapter = getClearingAdapter(adapters, event.getProviderId());
                ClearingProcessingEvent processingEvent =
                        new ClearingProcessingEvent(clearingAdapter, event.getId());
                handler.handle(processingEvent);
            }
        } catch (Exception ex) {
            log.error("Error during a revision", ex);
        }
    }

}
