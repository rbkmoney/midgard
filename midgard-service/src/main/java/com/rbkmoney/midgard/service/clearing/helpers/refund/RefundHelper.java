package com.rbkmoney.midgard.service.clearing.helpers.refund;

import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;

import java.util.List;

public interface RefundHelper {

    List<Refund> getRefunds(long eventId, List<Integer> providerIds, int poolSize);

    void saveClearingRefund(ClearingRefund clearingRefund);

}
