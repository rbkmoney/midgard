package com.rbkmoney.midgard.service.load.dao.party.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.Contractor;

import java.util.List;

public interface ContractorDao extends Dao {
    Long save(Contractor contractor) throws DaoException;
    Contractor get(String partyId, String contractorId) throws DaoException;
    void updateNotCurrent(String partyId, String contractorId) throws DaoException;
    List<Contractor> getByPartyId(String partyId);
}
