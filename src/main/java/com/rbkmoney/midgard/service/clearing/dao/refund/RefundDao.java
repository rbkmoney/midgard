package com.rbkmoney.midgard.service.clearing.dao.refund;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import org.jooq.generated.feed.tables.pojos.Refund;

import java.util.List;

public interface RefundDao {

    List<Refund> getRefunds(long eventId, List<Integer> providerIds, int poolSize) throws DaoException;

}
