package com.rbkmoney.midgard.service.load.dao.party.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.Contract;

import java.util.List;

public interface ContractDao extends Dao {
    Long save(Contract contract) throws DaoException;
    Contract get(String partyId, String contractId) throws DaoException;
    void updateNotCurrent(String partyId, String contractId) throws DaoException;
    List<Contract> getByPartyId(String partyId);
}
