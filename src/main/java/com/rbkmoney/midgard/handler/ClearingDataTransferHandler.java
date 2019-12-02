package com.rbkmoney.midgard.handler;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.data.ClearingDataPackage;
import com.rbkmoney.midgard.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.handler.failure.FailureTransactionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.enums.ClearingEventStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingDataTransferHandler implements Handler<ClearingProcessingEvent> {

    private final TransactionsDao transactionsDao;

    private final ClearingTransactionPackageHandler clearingTransactionPackageHandler;

    private final ClearingEventInfoDao eventInfoDao;

    private final FailureTransactionHandler<Transaction, Long> adapterFailureTransactionHandler;

    @Value("${clearing-service.package-size}")
    private int packageSize;

    @Override
    public void handle(ClearingProcessingEvent event) throws Exception {
        Long clearingId = event.getClearingId();
        int providerId = event.getClearingAdapter().getAdapterId();
        log.info("Transfer data to clearing adapter {} with clearing id {} get started",
                event.getClearingAdapter().getAdapterName(), clearingId);
        try {
            ClearingAdapterSrv.Iface adapter = event.getClearingAdapter().getAdapter();
            String uploadId = adapter.startClearingEvent(clearingId);
            List<ClearingDataPackageTag> tagList = new ArrayList<>();
            long lastRowId = 0L;
            Integer clearingTrxCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);

            if (clearingTrxCount == null || clearingTrxCount == 0) {
                log.info("No transactions found for clearing");
                ClearingDataRequest request = getEmptyClearingDataPackage(clearingId);
                ClearingDataResponse response = adapter.sendClearingDataPackage(uploadId, request);
                tagList.add(response.getClearingDataPackageTag());
            } else {
                int packageNumber = 1;
                ClearingDataPackage clearingDataPackage;
                do {
                    log.info("Start sending package {} for clearing event {}", packageNumber, clearingId);
                    clearingDataPackage =
                            clearingTransactionPackageHandler.getClearingPackage(clearingId, providerId, lastRowId, packageNumber);
                    ClearingDataResponse response =
                            adapter.sendClearingDataPackage(uploadId, clearingDataPackage.getClearingDataRequest());
                    processAdapterFailureTransactions(response.getFailureTransactions(), clearingId, packageNumber);
                    tagList.add(response.getClearingDataPackageTag());

                    lastRowId = clearingDataPackage.getLastRowId();
                    packageNumber++;
                    log.info("Finish sending package {} for clearing event {}", packageNumber, clearingId);
                } while (clearingDataPackage != null
                        && clearingDataPackage.getClearingDataRequest() != null
                        && clearingDataPackage.getClearingDataRequest().getTransactions() != null
                        && clearingDataPackage.getClearingDataRequest().getTransactions().size() == packageSize);
            }

            adapter.completeClearingEvent(uploadId, clearingId, tagList);
            eventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.COMPLETE);
            log.info("Transfer data to clearing adapter {} with clearing id {} was finished",
                    event.getClearingAdapter().getAdapterName(), clearingId);
        } catch (ClearingAdapterException ex) {
            log.error("Error occurred while processing clearing event {}", clearingId, ex);
            eventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.ADAPTER_FAULT);
            throw ex;
        } catch (TException ex) {
            log.error("Data transfer error while processing clearing event {}", clearingId, ex);
            eventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.ADAPTER_FAULT);
            throw new Exception(ex);
        } catch (Exception ex) {
            log.error("Received exception while sending clearing data to adapter", ex);
        } catch (Throwable th) {
            log.error("Received throwable while sending clearing data to adapter", th);
        }
    }

    private void processAdapterFailureTransactions(List<Transaction> failureTransactions,
                                                   Long clearingId,
                                                   int packageNumber) {
        log.info("Start processing failure transactions for package id {} and clearing id {}", packageNumber, clearingId);
        if (failureTransactions != null) {
            failureTransactions.forEach(transaction ->
                    adapterFailureTransactionHandler.handleTransaction(transaction, clearingId));
            log.info("Finish processing failure transactions for package id {} and clearing id {}", packageNumber, clearingId);
        } else {
            log.info("List of failure transactions for package id {} and clearing id {} is empty", packageNumber, clearingId);
        }
    }

    private ClearingDataRequest getEmptyClearingDataPackage(Long clearingId) {
        ClearingDataRequest dataPackage = new ClearingDataRequest();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(1);
        dataPackage.setFinalPackage(true);
        dataPackage.setTransactions(new ArrayList<>());
        return dataPackage;
    }

}
