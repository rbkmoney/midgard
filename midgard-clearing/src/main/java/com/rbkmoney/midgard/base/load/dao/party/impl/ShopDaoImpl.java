package com.rbkmoney.midgard.base.load.dao.party.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.dao.party.iface.ShopDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.RecordRowMapper;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Shop;
import org.jooq.generated.feed.tables.records.ShopRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static org.jooq.generated.feed.tables.Shop.SHOP;

@Component
public class ShopDaoImpl extends AbstractGenericDao implements ShopDao {

    private final RowMapper<Shop> shopRowMapper;

    public ShopDaoImpl(DataSource dataSource) {
        super(dataSource);
        shopRowMapper = new RecordRowMapper<>(SHOP, Shop.class);
    }

    @Override
    public Long save(Shop shop) throws DaoException {
        ShopRecord record = getDslContext().newRecord(SHOP, shop);
        Query query = getDslContext().insertInto(SHOP).set(record).returning(SHOP.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Shop get(String partyId, String shopId) throws DaoException {
        Query query = getDslContext().selectFrom(SHOP)
                .where(SHOP.PARTY_ID.eq(partyId).and(SHOP.SHOP_ID.eq(shopId)).and(SHOP.CURRENT));

        return fetchOne(query, shopRowMapper);
    }

    @Override
    public void updateNotCurrent(String partyId, String shopId) throws DaoException {
        Query query = getDslContext().update(SHOP).set(SHOP.CURRENT, false)
                .where(SHOP.PARTY_ID.eq(partyId).and(SHOP.SHOP_ID.eq(shopId)).and(SHOP.CURRENT));
        execute(query);
    }


    @Override
    public List<Shop> getByPartyId(String partyId) {
        Query query = getDslContext().selectFrom(SHOP)
                .where(SHOP.PARTY_ID.eq(partyId).and(SHOP.CURRENT));
        return fetch(query, shopRowMapper);
    }
}
