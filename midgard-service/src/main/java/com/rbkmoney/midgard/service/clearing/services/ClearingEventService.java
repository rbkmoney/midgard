package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.handlers.Handler;
import com.rbkmoney.midgard.service.clearing.helpers.clearing_info.ClearingInfoHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

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

    private final ClearingInfoHelper clearingInfoHelper;

    private final Handler clearingEventHandler;

    @Override
    public void startClearingEvent(ClearingEvent clearingEvent) {
        long eventId = clearingEvent.getEventId();
        String providerId = clearingEvent.getProviderId();
        log.info("Starting clearing event for provider id {}", providerId);
        // Подготовка транзакций для клиринга
        Long clearingId = clearingInfoHelper.prepareTransactionData(eventId, providerId);
        // Передача транзакций в клиринговый адаптер
        clearingEventHandler.handle(clearingId);
        log.info("Clearing event for provider id {} finished", providerId);
    }

    @Override
    public ClearingEventStateResponse getClearingEventState(long eventId) throws NoClearingEvent, TException {
        log.info("Getting the state of event {}", eventId);
        return clearingInfoHelper.getClearingEventState(eventId);
    }

}
