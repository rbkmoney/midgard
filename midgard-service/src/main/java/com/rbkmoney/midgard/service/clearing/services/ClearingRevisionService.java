package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.service.clearing.handlers.ClearingRevisionHandler;
import com.rbkmoney.midgard.service.clearing.helpers.clearing_info.ClearingInfoHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.ClearingEvent;
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

    private final ClearingInfoHelper clearingInfoHelper;

    private final ClearingRevisionHandler revisionHandler;

    @Override
    @Scheduled(fixedDelayString = "${clearing-service.revision}")
    public void process() {

        log.info("Clearing revision process get started");
        List<ClearingEvent> clearingEvents = clearingInfoHelper.getAllExecuteClearingEvents();
        List<Long> clearingIds = clearingEvents.stream()
                .map(event -> event.getId())
                .collect(Collectors.toList());

        log.debug("Active clearing event IDs: {}", clearingIds);

        for (ClearingEvent clearingEvent : clearingEvents) {
            //TODO: передать в обработчик ID события, которое нужно проверить
            revisionHandler.handle(clearingEvent.getId());
        }
        log.info("Clearing revision process was finished");
    }

}
