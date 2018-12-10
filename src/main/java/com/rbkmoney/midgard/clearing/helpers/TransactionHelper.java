package com.rbkmoney.midgard.clearing.helpers;

import com.rbkmoney.midgard.clearing.data.enums.Bank;
import com.rbkmoney.midgard.clearing.helpers.DAO.TransactionsDao;
import com.rbkmoney.midgard.clearing.utils.MidgardUtils;
import org.jooq.generated.midgard.enums.CtState;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.jooq.generated.midgard.enums.TransactionClearingState.ACTIVE;
import static org.jooq.generated.midgard.enums.TransactionClearingState.FAILED;

/** Вспомогательный класс для работы с транзакциями для клиринга */
@Component
public class TransactionHelper {

    /** Логгер */
    private static final Logger log = LoggerFactory.getLogger(TransactionHelper.class);
    /** Объект для работы с данными в БД */
    private final TransactionsDao dao;

    public TransactionHelper(DataSource dataSource) {
        dao = new TransactionsDao(dataSource);
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
        ClearingTransaction tmpTransaction = dao.get(transaction.getTransactionId());
        if (tmpTransaction != null) {
            dao.save(transaction);
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
        return dao.get(transactionId);
    }

    /**
     * Актуальный список клиринговых транзакций для банка
     *
     * @param bank целевой банк
     * @return список клиринговых транзакций
     */
    public List<ClearingTransaction> getActualClearingTransactions(Bank bank) {
        return dao.getClearingTransactionsByBank(bank, LocalDateTime.now(), Arrays.asList(ACTIVE, FAILED));
    }

    /**
     * Актуальный список клиринговых транзакций
     *
     * @return список клиринговых транзакций
     */
    public List<ClearingTransaction> getAllActualClearingTransactions() {
        return dao.getClearingTransactions(LocalDateTime.now(), Arrays.asList(ACTIVE, FAILED));
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
        dao.saveFailureTransaction(failureTransaction);
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
        saveClearingTransactionInfo(clearingId, transactionId, CtState.SENT);
    }

    /**
     * Сохранить сбойную в рамках события транзакцию
     *
     * @param clearingId ID клирингового события
     * @param transactionId ID транзакции
     */
    public void saveRefusedClearingTranInfo(Long clearingId, String transactionId) {
        saveClearingTransactionInfo(clearingId, transactionId, CtState.REFUSED);
    }

    /**
     * Сохранить транзакцию в рамках события
     *
     * @param clearingId ID клирингового события
     * @param transactionId ID транзакции
     * @param ctState состояние
     */
    private void saveClearingTransactionInfo(Long clearingId, String transactionId, CtState ctState) {
        ClearingTransactionInfo transactionInfo = new ClearingTransactionInfo();
        transactionInfo.setClearingId(clearingId);
        transactionInfo.setTransactionId(transactionId);
        transactionInfo.setCtState(ctState);
        dao.saveClearingTransactionInfo(transactionInfo);
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
            dao.setClearingTransactionMetaInfo(transaction.getTransactionId(), clearingId, ACTIVE);
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
            dao.setClearingTransactionMetaInfo(transaction.getTransactionId(), clearingId, FAILED);
        }
    }

}
