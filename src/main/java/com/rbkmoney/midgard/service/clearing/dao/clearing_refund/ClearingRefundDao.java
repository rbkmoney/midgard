package com.rbkmoney.midgard.service.clearing.dao.clearing_refund;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;

public interface ClearingRefundDao extends ClearingDao<ClearingRefund, String> {

    ClearingRefund getRefund(String invoiceId, String paymentId) throws DaoException;

    ClearingRefund getLastTransactionEvent() throws DaoException;

}
