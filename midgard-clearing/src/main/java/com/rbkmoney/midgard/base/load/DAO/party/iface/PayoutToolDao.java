package com.rbkmoney.midgard.base.load.DAO.party.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.PayoutTool;

import java.util.List;

public interface PayoutToolDao extends Dao {
    void save(List<PayoutTool> payoutToolList) throws DaoException;
    List<PayoutTool> getByCntrctId(Long cntrctId) throws DaoException;
}
