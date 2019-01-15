package com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow;

import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.jooq.generated.midgard.tables.records.ClearingTransactionCashFlowRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static org.jooq.generated.midgard.tables.ClearingTransactionCashFlow.CLEARING_TRANSACTION_CASH_FLOW;

@Slf4j
@Component
public class ClearingCashFlowDaoImpl extends AbstractGenericDao implements ClearingCashFlowDao {

    private final RowMapper<ClearingTransactionCashFlow> cashFlowRowMapper;

    public ClearingCashFlowDaoImpl(DataSource dataSource) {
        super(dataSource);
        cashFlowRowMapper = new RecordRowMapper<>(CLEARING_TRANSACTION_CASH_FLOW, ClearingTransactionCashFlow.class);
    }

    @Override
    public Long save(List<ClearingTransactionCashFlow> cashFlowList) throws DaoException {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        for (ClearingTransactionCashFlow cashFlow : cashFlowList) {
            ClearingTransactionCashFlowRecord record = getDslContext().newRecord(CLEARING_TRANSACTION_CASH_FLOW, cashFlow);
            Query query = getDslContext().insertInto(CLEARING_TRANSACTION_CASH_FLOW).set(record);
            executeWithReturn(query, keyHolder);
        }
        return keyHolder.getKey().longValue();
    }

    @Override
    public List<ClearingTransactionCashFlow> get(String sourceEventId) throws DaoException {
        Query query = getDslContext().selectFrom(CLEARING_TRANSACTION_CASH_FLOW)
                .where(CLEARING_TRANSACTION_CASH_FLOW.SOURCE_EVENT_ID.eq(Long.parseLong(sourceEventId)));
        return fetch(query, cashFlowRowMapper);
    }

}
