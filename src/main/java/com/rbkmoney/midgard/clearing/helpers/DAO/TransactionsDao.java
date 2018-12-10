package com.rbkmoney.midgard.clearing.helpers.DAO;

import com.rbkmoney.midgard.clearing.data.enums.Bank;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.ClearingDao;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.RecordRowMapper;
import org.jooq.Query;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.records.ClearingTransactionInfoRecord;
import org.jooq.generated.midgard.tables.records.ClearingTransactionRecord;
import org.jooq.generated.midgard.tables.records.FailureTransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.generated.midgard.tables.ClearingTransaction.CLEARING_TRANSACTION;
import static org.jooq.generated.midgard.tables.ClearingTransactionInfo.CLEARING_TRANSACTION_INFO;
import static org.jooq.generated.midgard.tables.FailureTransaction.*;

/**
 * Класс для взаимодействия с транзакциями.
 * clearing_transaction - в данной таблице хранится информация о транзакциях на основании которых необходимо
 * сформировать клиринг
 * failure_transaction - в данной таблице хранится информация о транзакциях, с которыми были проблемы в рамках клиринга
 * clearing_transaction_info - в данной таблице хранится информация о транзакциях, которые попали в клиринговое событие
 */
public class TransactionsDao extends AbstractGenericDao implements ClearingDao<ClearingTransaction> {

    /** Логгер */
    private static final Logger log = LoggerFactory.getLogger(TransactionsDao.class);
    /** Маппер */
    private final RowMapper<ClearingTransaction> transactionRowMapper;

    public TransactionsDao(DataSource dataSource) {
        super(dataSource);
        transactionRowMapper = new RecordRowMapper<>(CLEARING_TRANSACTION, ClearingTransaction.class);
    }

    @Override
    public Long save(ClearingTransaction transaction) {
        log.debug("Adding new merchant: {}", transaction);
        ClearingTransactionRecord record = getDslContext().newRecord(CLEARING_TRANSACTION, transaction);
        Query query = getDslContext().insertInto(CLEARING_TRANSACTION).set(record);
        int addedRows = execute(query);
        log.debug("New transaction with id {} was added", transaction.getMerchantId());
        return Long.valueOf(addedRows);
    }

    @Override
    public ClearingTransaction get(String transactionId) {
        log.debug("Getting a transaction with id {}", transactionId);
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where(CLEARING_TRANSACTION.TRANSACTION_ID.eq(transactionId));
        ClearingTransaction clearingTransaction = fetchOne(query, transactionRowMapper);
        log.debug("Transaction with id {} {}", transactionId, clearingTransaction == null ? "not found" : "found");
        return clearingTransaction;
    }

    /**
     * Получение списка клиринговых транзакций для определенного банка
     *
     * @param bank целевой банк
     * @param dateTo дата, до которой необходимо получить список транзакций
     * @param states список статусов транзакций, которые должны попасть в выборку
     * @return список клиринговых транзакций
     */
    public List<ClearingTransaction> getClearingTransactionsByBank(Bank bank,
                                                                   LocalDateTime dateTo,
                                                                   List<TransactionClearingState> states) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where((CLEARING_TRANSACTION.TRANSACTION_DATE.lessThan(dateTo))
                        .and(CLEARING_TRANSACTION.BANK_NAME.eq(bank.name()))
                        .and(CLEARING_TRANSACTION.TRANSACTION_CLEARING_STATE.in(states)));
        return fetch(query, transactionRowMapper);
    }

    /**
     * Получить список клиринговых транзакции
     *
     * @param dateTo дата, до которой необходимо получить список транзакций
     * @param states список статусов транзакций, которые должны попасть в выборку
     * @return список клиринговых транзакций
     */
    public List<ClearingTransaction> getClearingTransactions(LocalDateTime dateTo,
                                                             List<TransactionClearingState> states) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where((CLEARING_TRANSACTION.TRANSACTION_DATE.lessThan(dateTo))
                        .and(CLEARING_TRANSACTION.TRANSACTION_CLEARING_STATE.in(states)));
        return fetch(query, transactionRowMapper);
    }

    /**
     * Сохранить сбойную транзакцию
     *
     * @param failureTransaction сбойная транзакция
     */
    public void saveFailureTransaction(FailureTransaction failureTransaction) {
        FailureTransactionRecord record = getDslContext().newRecord(FAILURE_TRANSACTION, failureTransaction);
        Query query = getDslContext().insertInto(FAILURE_TRANSACTION).set(record);
        execute(query);
    }

    /**
     * Сохранить информацию о транзакции попавшей в клиринговое событие
     *
     * @param transactionInfo информация о транзакции
     */
    public void saveClearingTransactionInfo(ClearingTransactionInfo transactionInfo) {
        ClearingTransactionInfoRecord record = getDslContext().newRecord(CLEARING_TRANSACTION_INFO, transactionInfo);
        Query query = getDslContext().insertInto(CLEARING_TRANSACTION_INFO).set(record);
        execute(query);
    }

    /**
     * Установить новое состояние для клиринговой транзакции
     *
     * @param transactionId идентификатор транзакции
     * @param state состояние
     */
    public void setClearingTransactionState(String transactionId, TransactionClearingState state) {
        log.trace("Set transaction {} to state {}", transactionId, state);
        Query query = getDslContext().update(CLEARING_TRANSACTION)
                .set(CLEARING_TRANSACTION.TRANSACTION_CLEARING_STATE, state)
                .where(CLEARING_TRANSACTION.TRANSACTION_ID.eq(transactionId));
        execute(query);
        log.trace("The state of transacion {} successful chenged to state {}", transactionId, state);
    }

    /**
     * Обновить метаинформацию у коиринговой транзакции
     *
     * @param transactionId идентификатор транзакции
     * @param clearingId идентификатор клирингового события
     * @param state состояние
     */
    public void setClearingTransactionMetaInfo(String transactionId,
                                               Long clearingId,
                                               TransactionClearingState state) {
        log.trace("Set transaction {} to state {}", transactionId, state);
        Query query = getDslContext().update(CLEARING_TRANSACTION)
                .set(CLEARING_TRANSACTION.TRANSACTION_CLEARING_STATE, state)
                .set(CLEARING_TRANSACTION.CLEARING_ID, clearingId)
                .set(CLEARING_TRANSACTION.LAST_ACT_TIME, LocalDateTime.now())
                .where(CLEARING_TRANSACTION.TRANSACTION_ID.eq(transactionId));
        execute(query);
        log.trace("The state of transacion {} successful chenged to state {}", transactionId, state);
    }

}
