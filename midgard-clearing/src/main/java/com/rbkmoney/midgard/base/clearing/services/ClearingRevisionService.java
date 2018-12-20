package com.rbkmoney.midgard.base.clearing.services;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.base.clearing.helpers.ClearingInfoHelper;
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

    /** Клиент для работы с адаптером */
    private final ClearingAdapterSrv.Iface clearingAdapterService;
    /** Класс для работы с информацией о клиринговых событиях  */
    private final ClearingInfoHelper clearingInfoHelper;

    @Override
    @Scheduled(fixedDelayString = "${clearing-service.revision}")
    public void process() {
        log.info("Clearing revision process get started");
        List<ClearingEvent> clearingEvents = clearingInfoHelper.getAllExecuteClearingEvents();
        List<Long> clearingIds = clearingEvents.stream()
                .map(event -> event.getId())
                .collect(Collectors.toList());
        log.debug("Список активных клиринговых событий: {}", clearingIds);

        log.info("Clearing revision process was finished");
    }

}
