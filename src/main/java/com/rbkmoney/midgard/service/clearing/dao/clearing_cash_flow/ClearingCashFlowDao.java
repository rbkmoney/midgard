package com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;

import java.util.List;

public interface ClearingCashFlowDao extends ClearingDao<List<ClearingTransactionCashFlow>, Long> {
}
