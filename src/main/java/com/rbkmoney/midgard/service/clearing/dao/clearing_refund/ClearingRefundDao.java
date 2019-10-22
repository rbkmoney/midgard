package com.rbkmoney.midgard.service.clearing.dao.clearing_refund;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;

import java.util.List;

public interface ClearingRefundDao extends ClearingDao<ClearingRefund, String> {

    ClearingRefund getRefund(String invoiceId, String paymentId, String refundId, Integer trxVersion);

    ClearingRefund getLastTransactionEvent();

    ClearingRefund getLastActiveRefund();

    List<ClearingRefund> getReadyClearingRefunds(int providerId, int packageSize);

    void updateClearingRefundState(String invoiceId, String paymentId, String refundId,
                                   int version, long clearingId, Integer providerId, TransactionClearingState state);

}
