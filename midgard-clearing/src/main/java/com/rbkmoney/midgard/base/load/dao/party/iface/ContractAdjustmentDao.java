package com.rbkmoney.midgard.base.load.dao.party.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.ContractAdjustment;

import java.util.List;

public interface ContractAdjustmentDao extends Dao {
    void save(List<ContractAdjustment> contractAdjustmentList) throws DaoException;
    List<ContractAdjustment> getByCntrctId(Long cntrctId) throws DaoException;
}
