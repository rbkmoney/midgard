package com.rbkmoney.midgard.base.clearing.helpers.DAO;

import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.ClearingDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.RecordRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record1;
import org.jooq.generated.midgard.enums.ClearingTrxEventState;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionEventInfo;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.records.ClearingTransactionEventInfoRecord;
import org.jooq.generated.midgard.tables.records.ClearingTransactionRecord;
import org.jooq.generated.midgard.tables.records.FailureTransactionRecord;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.generated.midgard.Tables.CLEARING_TRANSACTION_EVENT_INFO;
import static org.jooq.generated.midgard.tables.ClearingTransaction.CLEARING_TRANSACTION;
import static org.jooq.generated.midgard.tables.FailureTransaction.*;
import static org.jooq.impl.DSL.count;

/**
 * Класс для взаимодействия с транзакциями.
 * clearing_transaction - в данной таблице хранится информация о транзакциях на основании которых необходимо
 * сформировать клиринг
 * failure_transaction - в данной таблице хранится информация о транзакциях, с которыми были проблемы в рамках клиринга
 * clearing_transaction_info - в данной таблице хранится информация о транзакциях, которые попали в клиринговое событие
 */
@Slf4j
public class TransactionsDao extends AbstractGenericDao implements ClearingDao<ClearingTransaction> {

    /** Маппер */
    private final RowMapper<ClearingTransaction> transactionRowMapper;
    /** Маппер */
    private final RowMapper<ClearingTransactionEventInfo> transactionEventInfoRowMapper;

    public TransactionsDao(DataSource dataSource) {
        super(dataSource);
        transactionRowMapper = new RecordRowMapper<>(CLEARING_TRANSACTION, ClearingTransaction.class);
        transactionEventInfoRowMapper =
                new RecordRowMapper<>(CLEARING_TRANSACTION_EVENT_INFO, ClearingTransactionEventInfo.class);
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
     * @param providerId ID провайдера
     * @param dateTo дата, до которой необходимо получить список транзакций
     * @param states список статусов транзакций, которые должны попасть в выборку
     * @return список клиринговых транзакций
     */
    public List<ClearingTransaction> getTransactionsByProviderId(String providerId,
                                                                 LocalDateTime dateTo,
                                                                 List<TransactionClearingState> states) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where((CLEARING_TRANSACTION.TRANSACTION_DATE.lessThan(dateTo))
                        .and(CLEARING_TRANSACTION.PROVIDER_ID.eq(providerId))
                        .and(CLEARING_TRANSACTION.TRANSACTION_CLEARING_STATE.in(states)));
        return fetch(query, transactionRowMapper);
    }

    /**
     * Получение списка клиринговых транзакций для определенного банка
     *
     * @param providerId ID провайдера
     * @param dateFrom дата, c которой необходимо получить список транзакций
     * @param dateTo дата, до которой необходимо получить список транзакций
     * @param states список статусов транзакций, которые должны попасть в выборку
     * @return список клиринговых транзакций
     */
    public List<ClearingTransaction> getTransactionsByProviderId(String providerId,
                                                                 LocalDateTime dateFrom,
                                                                 LocalDateTime dateTo,
                                                                 List<TransactionClearingState> states) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where((CLEARING_TRANSACTION.TRANSACTION_DATE.between(dateFrom, dateTo))
                        .and(CLEARING_TRANSACTION.PROVIDER_ID.eq(providerId))
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
    public void saveClearingTransactionInfo(ClearingTransactionEventInfo transactionInfo) {
        ClearingTransactionEventInfoRecord record = getDslContext().newRecord(CLEARING_TRANSACTION_EVENT_INFO, transactionInfo);
        Query query = getDslContext().insertInto(CLEARING_TRANSACTION_EVENT_INFO).set(record);
        execute(query);
    }

    /**
     * Получение пачки данных о транзакциях в рамках клирингового события
     *
     * @param clearingId ID клирингового события
     * @param state статус транзакции
     * @param rowForm с какого элемента нужно начать
     * @param rowTo до какого элемента нужно сделать
     * @return список транзакции в рамках клирингового события
     */
    public List<ClearingTransactionEventInfo> getClearingTransactionsByClearingId(Long clearingId,
                                                                                  ClearingTrxEventState state,
                                                                                  int rowForm,
                                                                                  int rowTo) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION_EVENT_INFO)
                .where(CLEARING_TRANSACTION_EVENT_INFO.CLEARING_ID.eq(clearingId))
                .and(CLEARING_TRANSACTION_EVENT_INFO.STATE.eq(state))
                .and(CLEARING_TRANSACTION_EVENT_INFO.ROW_NUMBER.greaterThan(rowForm))
                .and(CLEARING_TRANSACTION_EVENT_INFO.ROW_NUMBER.lessOrEqual(rowTo));
        return fetch(query, transactionEventInfoRowMapper);
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

    /**
     * Получение количества успешных транзакций попавших в клиринговое событие
     *
     * @param clearingId идентификатор клирингового события
     * @return возвращает количество успешных транзакций попавших в клиринговое событие
     */
    public int getProcessedClearingTransactionCount(long clearingId) {
        Field<Integer> rowCount = count(CLEARING_TRANSACTION_EVENT_INFO.CLEARING_ID).as("rowCount");
        Record1<Integer> record = getDslContext().select(rowCount)
                .from(CLEARING_TRANSACTION_EVENT_INFO)
                .where(CLEARING_TRANSACTION_EVENT_INFO.CLEARING_ID.eq(clearingId))
                .and(CLEARING_TRANSACTION_EVENT_INFO.STATE.eq(ClearingTrxEventState.PROCESSED)).fetchOne();
        return record.value1();
    }

}
