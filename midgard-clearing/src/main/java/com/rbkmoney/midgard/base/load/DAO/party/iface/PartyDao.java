package com.rbkmoney.midgard.base.load.DAO.party.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.Party;

public interface PartyDao extends Dao {
    Long getLastEventId() throws DaoException;
    Long save(Party party) throws DaoException;
    Party get(String partyId) throws DaoException;
    void updateNotCurrent(String partyId) throws DaoException;
}
