package com.rbkmoney.midgard.service.clearing.dao.clearing_refund;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;

public interface ClearingRefundDao extends ClearingDao<ClearingRefund> {

    ClearingRefund getRefund(String transactionId) throws DaoException;

    Long getLastTransactionEventId() throws DaoException;

}
