package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.helpers.ClearingInfoHelper;
import com.rbkmoney.midgard.service.clearing.helpers.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClearingEventService implements ClearingServiceSrv.Iface {

    private final TransactionHelper transactionHelper;

    private final ClearingInfoHelper clearingInfoHelper;

    private final ClearingAdapterSrv.Iface clearingAdapterService;

    //TODO: необходимо развести методы сервиса по разным исполнителям и уменьшить связность
    @Override
    public void startClearingEvent(ClearingEvent clearingEvent) throws TException {
        long eventId = clearingEvent.getEventId();
        String providerId = clearingEvent.getProviderId();
        log.info("Starting clearing event for provider id {}", providerId);
        // Подготовка транзакций для клиринга
        Long clearingId = clearingInfoHelper.prepareTransactionData(providerId, eventId);
        // Получение клоличества пакетов, которое необходимо будет отправить
        // TODO: можно, конечно, запилить и в бесконечный while до момента окончания записей
        int packagesCount = transactionHelper.getClearingTransactionPackagesCount(clearingId);
        for (int packageNumber = 1; packageNumber <= packagesCount; packageNumber++) {
            ClearingDataPackage dataPackage = transactionHelper.getClearingTransactionPackage(clearingId, packageNumber);
            try {
                //TODO: нужно предварительно получить конкретный адаптер из списка
                clearingAdapterService.sendClearingDataPackage(dataPackage);
            } catch (ClearingAdapterException ex) {
                //TODO: придумать обработку ошибки
                log.error("Error occurred while processing the package by the adapter", ex);
            }
        }
        log.info("Clearing event for provider id {} finished", providerId);
    }

    @Override
    public ClearingEventStateResponse getClearingEventState(long eventId) throws NoClearingEvent, TException {
        log.info("Getting the state of event {}...", eventId);
        return clearingInfoHelper.getClearingEventByEventId(eventId);
    }

}
