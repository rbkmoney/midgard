package com.rbkmoney.midgard.service.clearing.dao.transaction;

import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record1;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;
import org.jooq.generated.midgard.tables.records.ClearingEventTransactionInfoRecord;
import org.jooq.generated.midgard.tables.records.ClearingTransactionRecord;
import org.jooq.generated.midgard.tables.records.FailureTransactionRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.generated.midgard.Tables.CLEARING_EVENT_TRANSACTION_INFO;
import static org.jooq.generated.midgard.enums.TransactionClearingState.FAILED;
import static org.jooq.generated.midgard.enums.TransactionClearingState.READY;
import static org.jooq.generated.midgard.tables.ClearingTransaction.CLEARING_TRANSACTION;
import static org.jooq.generated.midgard.tables.FailureTransaction.FAILURE_TRANSACTION;
import static org.jooq.impl.DSL.count;

/**
 * Класс для взаимодействия с транзакциями.
 * clearing_transaction - в данной таблице хранится информация о транзакциях на основании которых необходимо
 * сформировать клиринг
 * failure_transaction - в данной таблице хранится информация о транзакциях, с которыми были проблемы в рамках клиринга
 * clearing_transaction_info - в данной таблице хранится информация о транзакциях, которые попали в клиринговое событие
 */
@Slf4j
@Component
public class TransactionsDaoImpl extends AbstractGenericDao implements TransactionsDao {

    private final RowMapper<ClearingTransaction> transactionRowMapper;

    private final RowMapper<ClearingEventTransactionInfo> transactionEventInfoRowMapper;

    public TransactionsDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
        transactionRowMapper =
                new RecordRowMapper<>(CLEARING_TRANSACTION, ClearingTransaction.class);
        transactionEventInfoRowMapper =
                new RecordRowMapper<>(CLEARING_EVENT_TRANSACTION_INFO, ClearingEventTransactionInfo.class);
    }

    @Override
    public Long save(ClearingTransaction transaction) {
        ClearingTransactionRecord record = getDslContext().newRecord(CLEARING_TRANSACTION, transaction);
        Query query = getDslContext().insertInto(CLEARING_TRANSACTION).set(record);
        int addedRows = execute(query);
        log.info("New transaction with id {} was added", transaction.getTransactionId());
        return Long.valueOf(addedRows);
    }

    @Override
    public ClearingTransaction get(String transactionId) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where(CLEARING_TRANSACTION.TRANSACTION_ID.eq(transactionId));
        ClearingTransaction clearingTransaction = fetchOne(query, transactionRowMapper);
        log.debug("Transaction with id {} {}", transactionId, clearingTransaction == null ? "not found" : "found");
        return clearingTransaction;
    }

    @Override
    public ClearingTransaction getTransaction(String invoiceId, String paymentId, Integer trxVersion) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where(CLEARING_TRANSACTION.INVOICE_ID.eq(invoiceId)
                        .and(CLEARING_TRANSACTION.PAYMENT_ID.eq(paymentId))
                        .and(CLEARING_TRANSACTION.TRX_VERSION.eq(trxVersion)));
        ClearingTransaction clearingTransaction = fetchOne(query, transactionRowMapper);
        log.debug("Transaction with invoice id id {} and payment id {} {}", invoiceId, paymentId,
                clearingTransaction == null ? "not found" : "found");
        return clearingTransaction;
    }

    @Override
    public void saveFailureTransaction(FailureTransaction failureTransaction) {
        FailureTransactionRecord record = getDslContext().newRecord(FAILURE_TRANSACTION, failureTransaction);
        Query query = getDslContext().insertInto(FAILURE_TRANSACTION).set(record);
        execute(query);
    }

    @Override
    public List<ClearingEventTransactionInfo> getClearingTransactionsByClearingId(Long clearingId,
                                                                                  int providerId,
                                                                                  long lastRowNumber,
                                                                                  int rowLimit) {
        Query query = getDslContext().selectFrom(CLEARING_EVENT_TRANSACTION_INFO)
                .where(CLEARING_EVENT_TRANSACTION_INFO.CLEARING_ID.eq(clearingId))
                .and(CLEARING_EVENT_TRANSACTION_INFO.PROVIDER_ID.eq(providerId))
                .and(CLEARING_EVENT_TRANSACTION_INFO.ROW_NUMBER.greaterThan(lastRowNumber))
                .orderBy(CLEARING_EVENT_TRANSACTION_INFO.ROW_NUMBER)
                .limit(rowLimit);
        return fetch(query, transactionEventInfoRowMapper);
    }

    @Override
    public void saveClearingEventTransactionInfo(ClearingEventTransactionInfo transactionInfo) {
        ClearingEventTransactionInfoRecord record =
                getDslContext().newRecord(CLEARING_EVENT_TRANSACTION_INFO, transactionInfo);
        Query query = getDslContext().insertInto(CLEARING_EVENT_TRANSACTION_INFO).set(record);
        execute(query);
    }

    @Override
    public Integer getProcessedClearingTransactionCount(long clearingId) {
        Field<Integer> rowCount = count(CLEARING_EVENT_TRANSACTION_INFO.CLEARING_ID).as("rowCount");
        Record1<Integer> record = getDslContext().select(rowCount)
                .from(CLEARING_EVENT_TRANSACTION_INFO)
                .where(CLEARING_EVENT_TRANSACTION_INFO.CLEARING_ID.eq(clearingId))
                .fetchOne();
        return record.value1();
    }

    @Override
    public ClearingTransaction getLastTransaction() {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where(CLEARING_TRANSACTION.SOURCE_ROW_ID.isNotNull())
                .orderBy(CLEARING_TRANSACTION.SOURCE_ROW_ID.desc())
                .limit(1);
        return fetchOne(query, transactionRowMapper);
    }

    @Override
    public ClearingTransaction getLastActiveTransaction(int providerId) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where(CLEARING_TRANSACTION.SOURCE_ROW_ID.isNotNull())
                .and(CLEARING_TRANSACTION.PROVIDER_ID.eq(providerId))
                .and(CLEARING_TRANSACTION.TRANSACTION_CLEARING_STATE.notIn(READY, FAILED))
                .orderBy(CLEARING_TRANSACTION.SOURCE_ROW_ID.desc())
                .limit(1);
        return fetchOne(query, transactionRowMapper);
    }

    @Override
    public List<ClearingTransaction> getReadyClearingTransactions(int providerId,
                                                                  int packageSize) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where(CLEARING_TRANSACTION.TRANSACTION_CLEARING_STATE.in(READY, FAILED)
                        .and(CLEARING_TRANSACTION.PROVIDER_ID.eq(providerId)))
                .limit(packageSize);
        return fetch(query, transactionRowMapper);
    }

    @Override
    public void updateClearingTransactionState(String invoiceId,
                                               String paymentId,
                                               int version,
                                               long clearingId,
                                               TransactionClearingState state) {
        Query query = getDslContext().update(CLEARING_TRANSACTION)
                .set(CLEARING_TRANSACTION.TRANSACTION_CLEARING_STATE, state)
                .set(CLEARING_TRANSACTION.CLEARING_ID, clearingId)
                .where(CLEARING_TRANSACTION.INVOICE_ID.eq(invoiceId)
                        .and(CLEARING_TRANSACTION.PAYMENT_ID.eq(paymentId))
                        .and(CLEARING_TRANSACTION.TRX_VERSION.eq(version)));

        execute(query);
    }

}
