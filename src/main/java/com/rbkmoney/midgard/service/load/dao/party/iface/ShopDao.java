package com.rbkmoney.midgard.service.load.dao.party.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.Shop;

import java.util.List;

public interface ShopDao extends Dao {
    Long save(Shop shop) throws DaoException;
    Shop get(String partyId, String shopId) throws DaoException;
    void updateNotCurrent(String partyId, String shopId) throws DaoException;
    List<Shop> getByPartyId(String partyId);
}
