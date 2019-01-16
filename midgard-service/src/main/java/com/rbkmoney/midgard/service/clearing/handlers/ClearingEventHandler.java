package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.ClearingAdapterException;
import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingDataPackage;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.enums.ClearingTrxType;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.generated.midgard.enums.ClearingTrxType.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingEventHandler implements Handler {

    private final TransactionsDao transactionsDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ClearingCashFlowDao cashFlowDao;

    // TODO: адаптеров может быть много. Реализовать выбор из нескольких согласно provider id
    private final ClearingAdapterSrv.Iface clearingAdapterService;

    @Value("${clearing-service.package-size}")
    private int packageSize;

    private static final int INIT_PACKAGE_NUMBER = 0;

    @Override
    public void handle(Long clearingId) {
        int packagesCount = getClearingTransactionPackagesCount(clearingId);
        for (int packageNumber = INIT_PACKAGE_NUMBER; packageNumber < packagesCount; packageNumber++) {
            ClearingDataPackage dataPackage = getClearingTransactionPackage(clearingId, packageNumber);
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
                cashFlowDao.get(clearingTransaction.getEventId().toString());
        return MappingUtils.transformClearingTransaction(clearingTransaction, cashFlowList);
    }

    private Transaction getRefundTransaction(ClearingEventTransactionInfo info) {
        ClearingRefund refund = clearingRefundDao.getRefund(info.getTransactionId());
        ClearingTransaction clearingTransaction =
                transactionsDao.getTransaction(refund.getInvoiceId(), refund.getPaymentId());
        List<ClearingTransactionCashFlow> cashFlowList =
                cashFlowDao.get(clearingTransaction.getEventId().toString());
        return MappingUtils.transformRefundTransaction(clearingTransaction, cashFlowList, refund);
    }

    private List<ClearingEventTransactionInfo> getActualClearingTransactionsInfo(Long clearingId, int packageNumber) {
        int rowFrom = packageNumber * packageSize;
        int rowTo = rowFrom + packageSize;
        return transactionsDao.getClearingTransactionsByClearingId(clearingId, rowFrom, rowTo);
    }

    private int getClearingTransactionPackagesCount(long clearingId) {
        int packagesCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);
        return (int) Math.floor((double) packagesCount / packageSize);
    }

}
