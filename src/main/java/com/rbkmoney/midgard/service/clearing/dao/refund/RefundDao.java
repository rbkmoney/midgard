package com.rbkmoney.midgard.service.clearing.dao.refund;

import org.jooq.generated.feed.tables.pojos.Refund;

import java.util.List;

public interface RefundDao {

    List<Refund> getRefunds(long sourceRowId, List<Integer> providerIds, int poolSize);

}
