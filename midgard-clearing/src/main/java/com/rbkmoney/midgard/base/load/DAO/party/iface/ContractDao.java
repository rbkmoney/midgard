package com.rbkmoney.midgard.base.load.DAO.party.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.Contract;

import java.util.List;

public interface ContractDao extends Dao {
    Long save(Contract contract) throws DaoException;
    Contract get(String partyId, String contractId) throws DaoException;
    void updateNotCurrent(String partyId, String contractId) throws DaoException;
    List<Contract> getByPartyId(String partyId);
}
