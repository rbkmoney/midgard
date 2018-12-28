package com.rbkmoney.midgard.service.load.dao.party.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.Party;

public interface PartyDao extends Dao {
    Long getLastEventId() throws DaoException;
    Long save(Party party) throws DaoException;
    Party get(String partyId) throws DaoException;
    void updateNotCurrent(String partyId) throws DaoException;
}
