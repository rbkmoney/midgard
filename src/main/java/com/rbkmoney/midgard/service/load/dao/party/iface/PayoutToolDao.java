package com.rbkmoney.midgard.service.load.dao.party.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.PayoutTool;

import java.util.List;

public interface PayoutToolDao extends Dao {
    void save(List<PayoutTool> payoutToolList) throws DaoException;
    List<PayoutTool> getByCntrctId(Long cntrctId) throws DaoException;
}
