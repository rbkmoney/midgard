package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.handlers.ClearingRevisionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    private final ClearingRevisionHandler revisionHandler;

    @Override
    @Scheduled(fixedDelayString = "${clearing-service.revision}")
    public void process() {

        log.info("Clearing revision process get started");
        List<ClearingEventInfo> clearingEvents = eventInfoDao.getAllClearingEvents(ClearingEventStatus.EXECUTE);
        List<Long> clearingIds = clearingEvents.stream()
                .map(ClearingEventInfo::getId)
                .collect(Collectors.toList());

        log.debug("Active clearing event IDs: {}", clearingIds);

        for (ClearingEventInfo clearingEvent : clearingEvents) {
            revisionHandler.handle(clearingEvent.getId());
        }
        log.info("Clearing revision process was finished");
    }

}
