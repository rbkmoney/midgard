package com.rbkmoney.midgard.base.clearing.helpers;

import com.rbkmoney.midgard.ClearingDataPackage;
import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.Merchant;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.base.clearing.helpers.dao.TransactionsDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.MerchantDao;
import com.rbkmoney.midgard.base.clearing.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.ClearingTrxEventState;
import org.jooq.generated.midgard.tables.pojos.ClearingMerchant;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionEventInfo;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jooq.generated.midgard.enums.TransactionClearingState.ACTIVE;
import static org.jooq.generated.midgard.enums.TransactionClearingState.FAILED;
import static org.jooq.generated.midgard.enums.TransactionClearingState.READY;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionHelper {

    private final TransactionsDao transactionsDao;

    private final MerchantDao merchantDao;

    @Value("${clearing-service.package-size}")
    private int packageSize;

    public void saveTransactions(List<ClearingTransaction> transactions) {
        transactions.forEach(this::saveTransaction);
    }

    public void saveTransaction(ClearingTransaction transaction) {
        log.debug("Saving a transaction {}...", transaction);
        ClearingTransaction tmpTransaction = transactionsDao.get(transaction.getTransactionId());
        if (tmpTransaction != null) {
                transactionsDao.save(transaction);
        } else if (!Utils.compareTransactions(transaction, tmpTransaction)) {
            log.warn("Duplicate transactions! Source transaction: {}, arrived transaction: {}",
                    transaction, tmpTransaction);
            //TODO: до конца непонятно как корректно сравнивать транзакции, но учитывая,
            //      что в целевую таблицу добавился event_id возможно это и не нужно
        } else {
            log.debug("The transaction {} was found in the database", tmpTransaction.getTransactionId());
        }
    }

    public ClearingTransaction getTransaction(String transactionId) {
        return transactionsDao.get(transactionId);
    }

    public List<ClearingTransaction> getActualClearingTransactions(ClearingEvent clearingEvent) {
        //TODO: поправить тип в протоколе
        Integer providerId = Integer.parseInt(clearingEvent.getProviderId());
        LocalDateTime dateTo = clearingEvent.getDateTo() == null ?
                LocalDateTime.now() : LocalDateTime.parse(clearingEvent.getDateTo());
        if (clearingEvent.getDateFrom() == null) {
            return transactionsDao.getTransactionsByProviderId(providerId, dateTo, Arrays.asList(READY, FAILED));
        } else {
            LocalDateTime dateFrom = LocalDateTime.parse(clearingEvent.getDateFrom());
            return transactionsDao.getTransactionsByProviderId(providerId, dateFrom, dateTo, Arrays.asList(READY, FAILED));
        }

    }

    public List<ClearingTransaction> getAllActualClearingTransactions() {
        return transactionsDao.getClearingTransactions(LocalDateTime.now(), Arrays.asList(READY, FAILED));
    }

    public void saveAllFailureTransactionByOneReason(List<ClearingTransaction> transactions,
                                                     Long clearingId,
                                                     String reason) {
        for (ClearingTransaction transaction : transactions) {
            saveFailureTransaction(transaction.getTransactionId(), clearingId, reason);
        }
    }

    public void saveFailureTransaction(String transactionId, Long clearingId, String reason) {
        FailureTransaction failureTransaction = new FailureTransaction();
        failureTransaction.setTransactionId(transactionId);
        failureTransaction.setClearingId(clearingId);
        failureTransaction.setReason(reason);
        transactionsDao.saveFailureTransaction(failureTransaction);
    }

    public void saveClearingTransactionsInfo(List<ClearingTransaction> activeTrx,
                                             List<ClearingTransaction> failedTrx,
                                             Long clearingId) {
        saveSentClearingTransactionsInfo(activeTrx, clearingId);
        saveRefusedClearingTransactionsInfo(failedTrx, clearingId);
    }

    private void saveSentClearingTransactionsInfo(List<ClearingTransaction> transactions, Long clearingId) {
        for (ClearingTransaction transaction : transactions) {
            saveSentClearingTranInfo(clearingId, transaction.getTransactionId());
        }
    }

    private void saveRefusedClearingTransactionsInfo(List<ClearingTransaction> transactions, Long clearingId) {
        for (ClearingTransaction transaction : transactions) {
            saveRefusedClearingTranInfo(clearingId, transaction.getTransactionId());
        }
    }

    public void saveSentClearingTranInfo(Long clearingId, String transactionId) {
        saveClearingTransactionInfo(clearingId, transactionId, ClearingTrxEventState.PROCESSED);
    }

    public void saveRefusedClearingTranInfo(Long clearingId, String transactionId) {
        saveClearingTransactionInfo(clearingId, transactionId, ClearingTrxEventState.REFUSED);
    }

    private void saveClearingTransactionInfo(Long clearingId,
                                             String transactionId,
                                             ClearingTrxEventState state) {
        ClearingTransactionEventInfo transactionInfo = new ClearingTransactionEventInfo();
        transactionInfo.setClearingId(clearingId);
        transactionInfo.setTransactionId(transactionId);
        transactionInfo.setState(state);
        transactionsDao.saveClearingTransactionInfo(transactionInfo);
    }

    public void updateClearingTransactionsState(List<ClearingTransaction> activeTrx,
                                                List<ClearingTransaction> failedTrx,
                                                Long clearingId) {
        updateClearingTransactionsToActiveState(activeTrx, clearingId);
        updateClearingTransactionsToFailedState(failedTrx, clearingId);
    }

    private void updateClearingTransactionsToActiveState(List<ClearingTransaction> transactions, Long clearingId) {
        for (ClearingTransaction transaction : transactions) {
            transactionsDao.setClearingTransactionMetaInfo(transaction.getTransactionId(), clearingId, ACTIVE);
        }
    }

    private void updateClearingTransactionsToFailedState(List<ClearingTransaction> transactions, Long clearingId) {
        for (ClearingTransaction transaction : transactions) {
            transactionsDao.setClearingTransactionMetaInfo(transaction.getTransactionId(), clearingId, FAILED);
        }
    }

    public ClearingDataPackage getClearingTransactionPackage(Long clearingId, int packageNumber) {
        List<ClearingTransactionEventInfo> trxEventInfo = getActualClearingTransactionsInfo(clearingId, packageNumber);
        ClearingDataPackage dataPackage = new ClearingDataPackage();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(packageNumber);
        dataPackage.setFinalPackage(trxEventInfo.size() == packageSize ? false : true);

        List<Transaction> transactions = new ArrayList<>();
        List<Merchant> merchants = new ArrayList<>();
        for (ClearingTransactionEventInfo info : trxEventInfo) {
            ClearingTransaction clearingTransaction = getTransaction(info.getTransactionId());
            transactions.add(Utils.transformTransaction(clearingTransaction));
            ClearingMerchant clearingMerchant = merchantDao.get(info.getMerchantId());
            merchants.add(Utils.transaformMerchant(clearingMerchant));
        }

        dataPackage.setMerchants(merchants);
        dataPackage.setTransactions(transactions);
        return dataPackage;
    }

    private List<ClearingTransactionEventInfo> getActualClearingTransactionsInfo(Long clearingId,
                                                                                 int packageNumber) {
        int rowFrom = packageNumber * packageSize;
        int rowTo = rowFrom + packageSize;
        return transactionsDao.getClearingTransactionsByClearingId(clearingId,
                ClearingTrxEventState.PROCESSED, rowFrom, rowTo);
    }

    public int getClearingTransactionPackagesCount(long clearingId) {
        int packagesCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);
        return (int) Math.floor(packagesCount / packageSize);
    }

    public long getMaxTransactionEventId() {
        Long eventId = transactionsDao.getMaxTransactionEventId();
        if (eventId == null) {
            log.warn("Event ID for clearing transactions was not found!");
            return 0L;
        } else {
            return eventId;
        }
    }

}
