package com.rbkmoney.midgard.dao.refund;

import com.rbkmoney.midgard.dao.ClearingDao;
import org.jooq.generated.enums.TransactionClearingState;
import org.jooq.generated.tables.pojos.ClearingRefund;

import java.util.List;

public interface ClearingRefundDao extends ClearingDao<ClearingRefund, String> {

    ClearingRefund getRefund(String invoiceId, String paymentId, String refundId, Integer trxVersion);

    List<ClearingRefund> getReadyClearingRefunds(int providerId, int packageSize);

    void updateClearingRefundState(String invoiceId, String paymentId, String refundId,
                                   int version, long clearingId, Integer providerId, TransactionClearingState state);

}
