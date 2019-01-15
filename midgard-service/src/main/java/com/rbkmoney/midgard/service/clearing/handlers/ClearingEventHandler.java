package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.ClearingAdapterException;
import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingDataPackage;
import com.rbkmoney.midgard.service.clearing.data.enums.HandlerType;
import com.rbkmoney.midgard.service.clearing.helpers.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingEventHandler implements Handler {

    private final TransactionHelper transactionHelper;

    private final ClearingAdapterSrv.Iface clearingAdapterService;

    @Override
    public void handle(Long clearingId) {
        // TODO: перевести обработку клирингового эвента из сервиса сюда
        //       Получение количества пакетов, которое необходимо будет отправить
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
            } catch (TException ex) {
                log.error("Вata transfer error", ex);
            }
        }
    }

    @Override
    public boolean isInstance(HandlerType handler) {
        return HandlerType.CLEARING_EVENT == handler;
    }

}
