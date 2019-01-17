package com.rbkmoney.midgard.adapter.mts.sevices;

import com.rbkmoney.midgard.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ClearingAdapterService implements ClearingAdapterSrv.Iface {

    private static final int FIRST_PACKAGE = 1;

    @Override
    public String startClearingEvent(long clearingId) throws ClearingAdapterException, TException {
        //TODO: возможно придется выпилить так как запуск клиринга первым пакетом мне кажется более элегантным

        return "1";
    }

    @Override
    public ClearingDataPackageTag sendClearingDataPackage(String upload_id, ClearingDataPackage dataPackage) throws ClearingAdapterException, TException {
        log.info("Data package have received: {}", dataPackage);
        if (dataPackage == null) {
            log.error("Received empty data package!");
            return new ClearingDataPackageTag();
        }
        if (dataPackage.getPackageNumber() == FIRST_PACKAGE) {
            createXmlFile(dataPackage);
        }

        //TODO: нужно чтобы данные писались в один файл, поэтому при первом пакете нужно создать его таким образом,
        //TODO: чтобы второй пакет по сути создал тот же самый файл

        //TODO: важный момент с шифрованием - как сохранять поток если нельзя будет дозаписывать поблоково?

        if (dataPackage.isFinalPackage()) {
            closeXmlFile();
            sendFileToFtp();
        }
        log.info("Data package {} for clearing event {} processed", dataPackage.getPackageNumber(),
                dataPackage.getClearingId());
        return new ClearingDataPackageTag();
    }

    @Override
    public void completeClearingEvent(String upload_id, long clearing_id, List<ClearingDataPackageTag> tags) throws ClearingAdapterException, TException {

    }


    private void createXmlFile(ClearingDataPackage clearingDataPackage) {
        writeHeader(clearingDataPackage.getClearingId());
    }

    private void writeHeader(Long clearingId) {

    }

    private void closeXmlFile() {

    }

    private void sendFileToFtp() {

    }

    //TODO: подумать стоит ли добавить команду окончания??

    @Override
    public ClearingEventResponse getBankResponse(long clearingId) throws ClearingAdapterException, TException {
        ClearingEventResponse response = new ClearingEventResponse();
        //TODO: 1. выполнить поиск сформированного XML документа. Eсли он не нвйден, то вернуть ошибку

        //TODO: 2. выполнить поиск ответа банка. Если найден, то необходимо выполнить разбор файла,
        //TODO:    формирование списка сбойных транзакции и вернуть ответ об успешном событии. Иначе
        //TODO:    оставить в позиции "Выполняется"

        return response;
    }

}
