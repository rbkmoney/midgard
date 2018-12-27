package com.rbkmoney.midgard.base.load.dao.invoicing.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.RecordRowMapper;
import com.rbkmoney.midgard.base.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.enums.AdjustmentCashFlowType;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.records.CashFlowRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static org.jooq.generated.feed.tables.CashFlow.CASH_FLOW;

@Component
public class CashFlowDaoImpl extends AbstractGenericDao implements CashFlowDao {

    private final RowMapper<CashFlow> cashFlowRowMapper;

    public CashFlowDaoImpl(DataSource dataSource) {
        super(dataSource);
        cashFlowRowMapper = new RecordRowMapper<>(CASH_FLOW, CashFlow.class);
    }

    @Override
    public void save(List<CashFlow> cashFlowList) throws DaoException {
        //todo: Batch insert
        for (CashFlow paymentCashFlow : cashFlowList) {
            CashFlowRecord record = getDslContext().newRecord(CASH_FLOW, paymentCashFlow);
            Query query = getDslContext().insertInto(CASH_FLOW).set(record);
            execute(query);
        }
    }

    @Override
    public List<CashFlow> getByObjId(Long objId, PaymentChangeType paymentChangeType) throws DaoException {
        Query query = getDslContext().selectFrom(CASH_FLOW)
                .where(CASH_FLOW.OBJ_ID.eq(objId).and(CASH_FLOW.OBJ_TYPE.eq(paymentChangeType)));
        return fetch(query, cashFlowRowMapper);
    }

    @Override
    public List<CashFlow> getForAdjustments(Long adjId, AdjustmentCashFlowType adjustmentCashFlowType) throws DaoException {
        Query query = getDslContext().selectFrom(CASH_FLOW)
                .where(CASH_FLOW.OBJ_ID.eq(adjId).and(CASH_FLOW.OBJ_TYPE.eq(PaymentChangeType.adjustment)).and(CASH_FLOW.ADJ_FLOW_TYPE.eq(adjustmentCashFlowType)));
        return fetch(query, cashFlowRowMapper);
    }
}
