package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.*;
import com.rbkmoney.midgard.service.clearing.decorators.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.exception.AdapterNotFoundException;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.tables.pojos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.generated.midgard.enums.ClearingTrxType.PAYMENT;
import static org.jooq.generated.midgard.enums.ClearingTrxType.REFUND;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingEventHandler implements Handler {

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ClearingCashFlowDao cashFlowDao;

    private final ClearingEventInfoDao clearingEventInfoDao;

    private final List<ClearingAdapter> adapters;

    @Value("${clearing-service.package-size}")
    private int packageSize;

    private static final int INIT_PACKAGE_NUMBER = 0;

    @Override
    public void handle(Long clearingId) {
        try {
            ClearingEventInfo clearingEventInfo = clearingEventInfoDao.get(clearingId);
            int providerId = clearingEventInfo == null ? 0 : clearingEventInfo.getProviderId();
            ClearingAdapter clearingAdapter = adapters.stream()
                    .filter(clrAdapter -> clrAdapter.getAdapterId() == providerId)
                    .findFirst()
                    .orElseThrow(() ->
                            new AdapterNotFoundException("Adapter with provider id " + providerId + " not found"));
            ClearingAdapterSrv.Iface adapter = clearingAdapter.getAdapter();

            String uploadId = adapter.startClearingEvent(clearingId);
            int packagesCount = getClearingTransactionPackagesCount(clearingId);
            List<ClearingDataPackageTag> tagList = new ArrayList<>();

            for (int packageNumber = INIT_PACKAGE_NUMBER; packageNumber < packagesCount; packageNumber++) {
                ClearingDataPackage dataPackage = getClearingTransactionPackage(clearingId, packageNumber);
                ClearingDataPackageTag tag = adapter.sendClearingDataPackage(uploadId, dataPackage);
                tagList.add(tag);
            }
            adapter.completeClearingEvent(uploadId, clearingId, tagList);

        } catch (ClearingAdapterException ex) {
            log.error("Error occurred while processing the package by the adapter", ex);
            //TODO: реализовать корректную обработку ошибки
        } catch (TException e) {
            log.error("Error communicating with adapter", e);
            // TODO: реализовать корректную обработку ошибки
        }
    }

    private ClearingDataPackage getClearingTransactionPackage(Long clearingId, int packageNumber) {
        List<ClearingEventTransactionInfo> trxEventInfo = getActualClearingTransactionsInfo(clearingId, packageNumber);
        ClearingDataPackage dataPackage = new ClearingDataPackage();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(packageNumber + 1);
        dataPackage.setFinalPackage(trxEventInfo.size() == packageSize ? false : true);

        List<Transaction> transactions = new ArrayList<>();
        for (ClearingEventTransactionInfo info : trxEventInfo) {
            if (PAYMENT.equals(info.getTransactionType())) {
                transactions.add(getTransaction(info));
            } else if (REFUND.equals(info.getTransactionType())) {
                transactions.add(getRefundTransaction(info));
            }
        }

        dataPackage.setTransactions(transactions);
        return dataPackage;
    }

    private Transaction getTransaction(ClearingEventTransactionInfo info) {
        ClearingTransaction clearingTransaction = transactionsDao.get(info.getTransactionId());
        List<ClearingTransactionCashFlow> cashFlowList =
                cashFlowDao.get(clearingTransaction.getEventId());
        return MappingUtils.transformClearingTransaction(clearingTransaction, cashFlowList);
    }

    private Transaction getRefundTransaction(ClearingEventTransactionInfo info) {
        ClearingRefund refund = clearingRefundDao.getRefund(info.getTransactionId());
        ClearingTransaction clearingTransaction =
                transactionsDao.getTransaction(refund.getInvoiceId(), refund.getPaymentId());
        List<ClearingTransactionCashFlow> cashFlowList =
                cashFlowDao.get(clearingTransaction.getEventId());
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

}
