package com.rbkmoney.midgard.base.clearing.services;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.base.clearing.handlers.Handler;
import com.rbkmoney.midgard.base.clearing.helpers.ClearingInfoHelper;
import com.rbkmoney.midgard.base.clearing.helpers.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

/** Обрабогтчик запросов от внешней системы */
@Slf4j
@RequiredArgsConstructor
@Service
public class ClearingEventService implements ClearingServiceSrv.Iface {

    /** Вспомогательный класс для работы с транзакциями */
    private final TransactionHelper transactionHelper;
    /** Вспомогательный класс для работы с метаинформацией */
    private final ClearingInfoHelper clearingInfoHelper;
    /** Клиент для работы с адаптером */
    private final ClearingAdapterSrv.Iface clearingAdapterService;
    /** Вспомогательный класс для запуска миграции данных */
    private final Handler migrationDataHandler;

    //TODO: необходимо развести методы сервиса по разным исполнителям и уменьшить связность
    @Override
    public void startClearingEvent(ClearingEvent clearingEvent) throws TException {
        long eventId = clearingEvent.getEventId();
        String providerId = clearingEvent.getProviderId();
        log.info("Starting clearing event for provider id {}", providerId);
        // Запуск миграции данных
        startDataMigration();
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

    /** Запустить миграцию данных */
    private void startDataMigration() {
        migrationDataHandler.handle();
    }

    @Override
    public ClearingEventStateResponse getClearingEventState(long eventId) throws NoClearingEvent, TException {
        log.info("Getting the state of event {}...", eventId);
        return clearingInfoHelper.getClearingEventByEventId(eventId);
    }
}
