package com.rbkmoney.midgard.test.integration.dao;

import com.rbkmoney.midgard.dao.AbstractGenericDao;
import com.rbkmoney.midgard.dao.RecordRowMapper;
import com.rbkmoney.midgard.exception.DaoException;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record1;
import org.jooq.generated.feed.enums.PaymentStatus;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;

import static org.jooq.generated.feed.Tables.PAYMENT;
import static org.jooq.generated.midgard.tables.ClearingRefund.CLEARING_REFUND;
import static org.jooq.generated.midgard.tables.ClearingTransaction.CLEARING_TRANSACTION;
import static org.jooq.impl.DSL.count;

public class TestTransactionsDao extends AbstractGenericDao {

    private final RowMapper<ClearingTransaction> transactionRowMapper;

    public TestTransactionsDao(DataSource dataSource) {
        super(dataSource);
        transactionRowMapper = new RecordRowMapper<>(CLEARING_TRANSACTION, ClearingTransaction.class);
    }

    public List<ClearingTransaction> getAllTransactionsByState(long clearingId,
                                                               TransactionClearingState state) {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION)
                .where(CLEARING_TRANSACTION.CLEARING_ID.eq(clearingId))
                .and(CLEARING_TRANSACTION.TRANSACTION_CLEARING_STATE.eq(state));
        return fetch(query, transactionRowMapper);
    }

    public Integer getReadyClearingTransactionsCount(int providerId) throws DaoException {
        Field<Integer> rowCount = count(CLEARING_TRANSACTION.TRANSACTION_ID).as("rowCount");
        Record1<Integer> record = getDslContext().select(rowCount)
                .from(CLEARING_TRANSACTION)
                .where(CLEARING_TRANSACTION.PROVIDER_ID.eq(providerId))
                .fetchOne();
        return record.value1();
    }

    public Integer getClearingRefundCount(String shopId) throws DaoException {
        Field<Integer> rowCount = count(CLEARING_REFUND.TRANSACTION_ID).as("rowCount");
        Record1<Integer> record = getDslContext().select(rowCount)
                .from(CLEARING_REFUND)
                .where(CLEARING_REFUND.SHOP_ID.equal(shopId))
                .fetchOne();
        return record.value1();
    }

    public Integer getPaymentsCount(List<Integer> providerIds) throws DaoException {
        Field<Integer> rowCount = count(PAYMENT.INVOICE_ID).as("rowCount");
        Record1<Integer> record = getDslContext().select(rowCount)
                .from(PAYMENT)
                .where(PAYMENT.STATUS.eq(PaymentStatus.captured))
                .and(PAYMENT.ROUTE_PROVIDER_ID.in(providerIds))
                .fetchOne();
        return record.value1();
    }

}
