package com.rbkmoney.midgard.service.clearing.dao.clearing_refund;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;

import java.util.List;

public interface ClearingRefundDao extends ClearingDao<ClearingRefund, String> {

    Long save(ClearingRefund clearingRefund, List<CashFlow> cashFlow) throws DaoException;

    ClearingRefund getRefund(String transactionId) throws DaoException;

    ClearingRefund getLastTransactionEvent() throws DaoException;

}
