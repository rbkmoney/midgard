package com.rbkmoney.midgard.base.load.dao.party.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.Shop;

import java.util.List;

public interface ShopDao extends Dao {
    Long save(Shop shop) throws DaoException;
    Shop get(String partyId, String shopId) throws DaoException;
    void updateNotCurrent(String partyId, String shopId) throws DaoException;
    List<Shop> getByPartyId(String partyId);
}
