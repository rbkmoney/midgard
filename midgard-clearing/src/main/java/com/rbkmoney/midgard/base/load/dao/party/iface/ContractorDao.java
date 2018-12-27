package com.rbkmoney.midgard.base.load.dao.party.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.Contractor;

import java.util.List;

public interface ContractorDao extends Dao {
    Long save(Contractor contractor) throws DaoException;
    Contractor get(String partyId, String contractorId) throws DaoException;
    void updateNotCurrent(String partyId, String contractorId) throws DaoException;
    List<Contractor> getByPartyId(String partyId);
}
