package com.rbkmoney.midgard.base.clearing.helpers;

import com.rbkmoney.midgard.ClearingDataPackage;
import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.Merchant;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.TransactionsDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.MerchantDao;
import com.rbkmoney.midgard.base.clearing.utils.MidgardUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.ClearingTrxEventState;
import org.jooq.generated.midgard.tables.pojos.ClearingMerchant;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionEventInfo;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jooq.generated.midgard.enums.TransactionClearingState.ACTIVE;
import static org.jooq.generated.midgard.enums.TransactionClearingState.FAILED;
import static org.jooq.generated.midgard.enums.TransactionClearingState.READY;

/** Вспомогательный класс для работы с транзакциями для клиринга */
@Slf4j
@Component
public class TransactionHelper {

    /** Объект для работы с данными транзакций в БД */
    private final TransactionsDao transactionsDao;
    /** Объект для работы с данными мерчантов в БД */
    private final MerchantDao merchantDao;
    /** Размер пачки с данными транзацкии */
    @Value("${clearing-service.package-size}")
    private int packageSize;

    @Autowired
    public TransactionHelper(DataSource dataSource) {
        transactionsDao = new TransactionsDao(dataSource);
        merchantDao = new MerchantDao(dataSource);
    }

    /**
     * Сохранение списка транзакций
     *
     * @param transactions список транзакций
     */
    public void saveTransactions(List<ClearingTransaction> transactions) {
        transactions.forEach(this::saveTransaction);
    }

    /**
     * Сохранение транзакции
     *
     * @param transaction трназакция
     */
    public void saveTransaction(ClearingTransaction transaction) {
        log.debug("Saving a transaction {}...", transaction);
        ClearingTransaction tmpTransaction = transactionsDao.get(transaction.getTransactionId());
        if (tmpTransaction != null) {
            transactionsDao.save(transaction);
        } else {
            log.debug("The transaction {} was found in the database", tmpTransaction.getTransactionId());
            if (!MidgardUtils.compareTransactions(transaction, tmpTransaction)) {
                log.warn("Duplicate transactions! Source transaction: {}, arrived transaction: {}",
                        transaction, tmpTransaction);
                //TODO: до конца непонятно как корректно сравнивать транзакции
            }
        }
    }

    /**
     * Получение транзакции
     *
     * @param transactionId id транзакции
     * @return возвращает транзакцию
     */
    public ClearingTransaction getTransaction(String transactionId) {
        return transactionsDao.get(transactionId);
    }

    /**
     * Актуальный список клиринговых транзакций для банка
     *
     * @param clearingEvent клиринговое событие
     * @return список клиринговых транзакций
     */
    public List<ClearingTransaction> getActualClearingTransactions(ClearingEvent clearingEvent) {
        String providerId = clearingEvent.getProviderId();
        LocalDateTime dateTo = clearingEvent.getDateTo() == null ?
                LocalDateTime.now() : LocalDateTime.parse(clearingEvent.getDateTo());
        if (clearingEvent.getDateFrom() == null) {
            return transactionsDao.getTransactionsByProviderId(providerId, dateTo, Arrays.asList(READY, FAILED));
        } else {
            LocalDateTime dateFrom = LocalDateTime.parse(clearingEvent.getDateFrom());
            return transactionsDao.getTransactionsByProviderId(providerId, dateFrom, dateTo, Arrays.asList(READY, FAILED));
        }

    }

    /**
     * Актуальный список клиринговых транзакций
     *
     * @return список клиринговых транзакций
     */
    public List<ClearingTransaction> getAllActualClearingTransactions() {
        return transactionsDao.getClearingTransactions(LocalDateTime.now(), Arrays.asList(READY, FAILED));
    }

    /**
     * Сохранить все сбойные транзакции с одинаковой причиной сбоя
     *
     * @param transactions список транзакций
     * @param clearingId ID клирингового события
     * @param reason причина
     */
    public void saveAllFailureTransactionByOneReason(List<ClearingTransaction> transactions,
                                                     Long clearingId,
                                                     String reason) {
        for (ClearingTransaction transaction : transactions) {
            saveFailureTransaction(transaction.getTransactionId(), clearingId, reason);
        }
    }

    /**
     * Сохранить сбойную транзакцию
     *
     * @param transactionId ID транзакции
     * @param clearingId ID клирингового события
     * @param reason причина
     */
    public void saveFailureTransaction(String transactionId, Long clearingId, String reason) {
        FailureTransaction failureTransaction = new FailureTransaction();
        failureTransaction.setTransactionId(transactionId);
        failureTransaction.setClearingId(clearingId);
        failureTransaction.setReason(reason);
        transactionsDao.saveFailureTransaction(failureTransaction);
    }

    /**
     * Сохранить список клиринговых транзакций в рамках эвента
     *
     * @param activeTrx список транзакций, которые были переданы в адаптер
     * @param failedTrx список сбойных трназакций
     * @param clearingId ID клирингового события
     */
    public void saveClearingTransactionsInfo(List<ClearingTransaction> activeTrx,
                                             List<ClearingTransaction> failedTrx,
                                             Long clearingId) {
        saveSentClearingTransactionsInfo(activeTrx, clearingId);
        saveRefusedClearingTransactionsInfo(failedTrx, clearingId);
    }

    /**
     * Сохранить список переданных в адаптер транзакций в рамках события
     *
     * @param transactions список транзакций
     * @param clearingId ID клирингового события
     */
    private void saveSentClearingTransactionsInfo(List<ClearingTransaction> transactions, Long clearingId) {
        for (ClearingTransaction transaction : transactions) {
            saveSentClearingTranInfo(clearingId, transaction.getTransactionId());
        }
    }

    /**
     * Сохранить список сбойных транзакций в рамках события
     *
     * @param transactions список транзакций
     * @param clearingId ID клирингового события
     */
    private void saveRefusedClearingTransactionsInfo(List<ClearingTransaction> transactions, Long clearingId) {
        for (ClearingTransaction transaction : transactions) {
            saveRefusedClearingTranInfo(clearingId, transaction.getTransactionId());
        }
    }

    /**
     * Сохранить переданную в адаптер в рамках события транзакцию
     *
     * @param clearingId ID клирингового события
     * @param transactionId ID транзакции
     */
    public void saveSentClearingTranInfo(Long clearingId, String transactionId) {
        saveClearingTransactionInfo(clearingId, transactionId, ClearingTrxEventState.PROCESSED);
    }

    /**
     * Сохранить сбойную в рамках события транзакцию
     *
     * @param clearingId ID клирингового события
     * @param transactionId ID транзакции
     */
    public void saveRefusedClearingTranInfo(Long clearingId, String transactionId) {
        saveClearingTransactionInfo(clearingId, transactionId, ClearingTrxEventState.REFUSED);
    }

    /**
     * Сохранить транзакцию в рамках события
     *
     * @param clearingId ID клирингового события
     * @param transactionId ID транзакции
     * @param state состояние
     */
    private void saveClearingTransactionInfo(Long clearingId,
                                             String transactionId,
                                             ClearingTrxEventState state) {
        ClearingTransactionEventInfo transactionInfo = new ClearingTransactionEventInfo();
        transactionInfo.setClearingId(clearingId);
        transactionInfo.setTransactionId(transactionId);
        transactionInfo.setState(state);
        transactionsDao.saveClearingTransactionInfo(transactionInfo);
    }

    /**
     * Обновление статуса транзакций в рамках клирингового события
     *
     * @param activeTrx список успешных трназакций
     * @param failedTrx список неуспешных трназакций
     * @param clearingId ID клирингового события
     */
    public void updateClearingTransactionsState(List<ClearingTransaction> activeTrx,
                                                List<ClearingTransaction> failedTrx,
                                                Long clearingId) {
        updateClearingTransactionsToActiveState(activeTrx, clearingId);
        updateClearingTransactionsToFailedState(failedTrx, clearingId);
    }

    /**
     * Обновление статуса транзакций на "активная" в рамках клирингового события
     *
     * @param transactions список успешных трназакций
     * @param clearingId ID клирингового события
     */
    private void updateClearingTransactionsToActiveState(List<ClearingTransaction> transactions, Long clearingId) {
        for (ClearingTransaction transaction : transactions) {
            transactionsDao.setClearingTransactionMetaInfo(transaction.getTransactionId(), clearingId, ACTIVE);
        }
    }

    /**
     * Обновление статуса транзакций на "сбойная" в рамках клирингового события
     *
     * @param transactions список неуспешных трназакций
     * @param clearingId ID клирингового события
     */
    private void updateClearingTransactionsToFailedState(List<ClearingTransaction> transactions, Long clearingId) {
        for (ClearingTransaction transaction : transactions) {
            transactionsDao.setClearingTransactionMetaInfo(transaction.getTransactionId(), clearingId, FAILED);
        }
    }

    /**
     * Получение пакета транзакций готовых к передаче в клиринговый адаптер банка
     *
     * @param clearingId ID клирингового события
     * @param packageNumber номер передаваемого пакета
     * @return возвращает пакет транзакций готовых к передаче в клиринговый адаптер банка
     */
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
            transactions.add(MidgardUtils.transformTransaction(clearingTransaction));
            ClearingMerchant clearingMerchant = merchantDao.get(info.getMerchantId());
            merchants.add(MidgardUtils.transaformMerchant(clearingMerchant));
        }

        dataPackage.setMerchants(merchants);
        dataPackage.setTransactions(transactions);
        return dataPackage;
    }

    /**
     * Получение списка транзакций, которые должны быть включены в пакет
     *
     * @param clearingId ID клирингового события
     * @param packageNumber номер передаваемого пакета
     * @return возвращает список клиринговых транзакций
     */
    private List<ClearingTransactionEventInfo> getActualClearingTransactionsInfo(Long clearingId,
                                                                                 int packageNumber) {
        int rowFrom = packageNumber * packageSize;
        int rowTo = rowFrom + packageSize;
        return transactionsDao.getClearingTransactionsByClearingId(clearingId,
                ClearingTrxEventState.PROCESSED, rowFrom, rowTo);
    }

    /**
     * Получение количества пакетов, которое необходимо будет отправить в адаптер в рамках клирингового события
     *
     * @param clearingId ID клирингового события
     * @return количество пакетов, которое необходимо будет отправить в адаптер в рамках клирингового события
     */
    public int getClearingTransactionPackagesCount(long clearingId) {
        int packagesCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);
        return (int) Math.floor(packagesCount / packageSize);
    }

}
