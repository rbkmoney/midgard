package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.generated.midgard.enums.ClearingTrxType.PAYMENT;
import static org.jooq.generated.midgard.enums.ClearingTrxType.REFUND;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingDataTransferHandler implements Handler<ClearingProcessingEvent> {

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ClearingCashFlowDao cashFlowDao;

    private final ClearingEventInfoDao eventInfoDao;

    @Value("${clearing-service.package-size}")
    private int packageSize;

    private static final int INIT_PACKAGE_NUMBER = 0;

    @Override
    public void handle(ClearingProcessingEvent event) throws Exception {
        Long clearingId = event.getClearingId();
        log.info("Transfer data to clearing adapter {} with clearing id {} get started",
                event.getClearingAdapter().getAdapterName(), clearingId);
        try {
            ClearingAdapterSrv.Iface adapter = event.getClearingAdapter().getAdapter();
            String uploadId = adapter.startClearingEvent(clearingId);
            int packagesCount = getClearingTransactionPackagesCount(clearingId);
            log.info("Total number of packages for the clearing event {}: {}", clearingId, packagesCount);

            List<ClearingDataPackageTag> tagList = new ArrayList<>();
            if (packagesCount == 0) {
                log.info("No transactions found for clearing");
                ClearingDataPackage dataPackage = getEmptyClearingDataPackage(clearingId);
                ClearingDataPackageTag tag = adapter.sendClearingDataPackage(uploadId, dataPackage);
                tagList.add(tag);
            } else {
                for (int packageNumber = INIT_PACKAGE_NUMBER; packageNumber < packagesCount; packageNumber++) {
                    ClearingDataPackage dataPackage = getClearingTransactionPackage(clearingId, packageNumber);
                    ClearingDataPackageTag tag = adapter.sendClearingDataPackage(uploadId, dataPackage);
                    tagList.add(tag);
                }
            }

            adapter.completeClearingEvent(uploadId, clearingId, tagList);
            eventInfoDao.updateClearingStatus(clearingId, ClearingEventStatus.EXECUTE);
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
        }
    }

    private ClearingDataPackage getClearingTransactionPackage(Long clearingId, int packageNumber) {
        List<ClearingEventTransactionInfo> trxEventInfo = getActualClearingTransactionsInfo(clearingId, packageNumber);
        ClearingDataPackage dataPackage = new ClearingDataPackage();
        dataPackage.setClearingId(clearingId);
        // TODO: возможно LONG слишком большое значение для номера пакета. По-хорошему переделать на INT
        dataPackage.setPackageNumber((long) packageNumber + 1);
        dataPackage.setFinalPackage(trxEventInfo.size() == packageSize ? false : true);

        List<Transaction> transactions = new ArrayList<>();
        for (ClearingEventTransactionInfo info : trxEventInfo) {
            if (PAYMENT.equals(info.getTransactionType())) {
                transactions.add(getTransaction(info, clearingId, packageNumber));
            } else if (REFUND.equals(info.getTransactionType())) {
                transactions.add(getRefundTransaction(info, clearingId, packageNumber));
            }
        }

        dataPackage.setTransactions(transactions);
        return dataPackage;
    }

    private Transaction getTransaction(ClearingEventTransactionInfo info, Long clearingId, int packageNumber) {
        ClearingTransaction clearingTransaction = transactionsDao.get(info.getTransactionId());
        log.info("Transaction with invoice id {} and transaction id {} will added to package {} " +
                "for clearing event {}", clearingTransaction.getInvoiceId(), clearingTransaction.getTransactionId(),
                packageNumber, clearingId);
        List<ClearingTransactionCashFlow> cashFlowList =
                cashFlowDao.get(clearingTransaction.getSequenceId());
        return MappingUtils.transformClearingTransaction(clearingTransaction, cashFlowList);
    }

    private Transaction getRefundTransaction(ClearingEventTransactionInfo info, Long clearingId, int packageNumber) {
        ClearingRefund refund = clearingRefundDao.getRefund(info.getTransactionId());
        log.info("Refund transaction with invoice id {} and transaction id {} will added to package {} " +
                "for clearing event {}", refund.getInvoiceId(), refund.getTransactionId(), packageNumber, clearingId);
        ClearingTransaction clearingTransaction =
                transactionsDao.getTransaction(refund.getInvoiceId(), refund.getPaymentId());
        List<ClearingTransactionCashFlow> cashFlowList = cashFlowDao.get(refund.getSequenceId());
        return MappingUtils.transformRefundTransaction(clearingTransaction, cashFlowList, refund);
    }

    private List<ClearingEventTransactionInfo> getActualClearingTransactionsInfo(Long clearingId, int packageNumber) {
        int rowFrom = packageNumber * packageSize;
        int rowTo = rowFrom + packageSize;
        return transactionsDao.getClearingTransactionsByClearingId(clearingId, rowFrom, rowTo);
    }

    private int getClearingTransactionPackagesCount(long clearingId) {
        int packagesCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);
        return (int) Math.ceil((double) packagesCount / packageSize);
    }

    private ClearingDataPackage getEmptyClearingDataPackage(Long clearingId) {
        ClearingDataPackage dataPackage = new ClearingDataPackage();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(1);
        dataPackage.setFinalPackage(true);
        dataPackage.setTransactions(new ArrayList<>());
        return dataPackage;
    }

}
