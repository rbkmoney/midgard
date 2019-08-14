package com.rbkmoney.midgard.service.clearing.dao.clearing_refund;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;

public interface ClearingRefundDao extends ClearingDao<ClearingRefund, String> {

    ClearingRefund getRefund(String invoiceId, String paymentId, String refundId, Integer trxVersion);

    ClearingRefund getLastTransactionEvent();

}
