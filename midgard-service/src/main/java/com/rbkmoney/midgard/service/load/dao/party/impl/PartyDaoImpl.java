package com.rbkmoney.midgard.service.load.dao.party.impl;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.RecordRowMapper;
import com.rbkmoney.midgard.service.load.dao.party.iface.PartyDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Party;
import org.jooq.generated.feed.tables.records.PartyRecord;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Contract.CONTRACT;
import static org.jooq.generated.feed.tables.Contractor.CONTRACTOR;
import static org.jooq.generated.feed.tables.Party.PARTY;
import static org.jooq.generated.feed.tables.Shop.SHOP;

@Component
public class PartyDaoImpl extends AbstractGenericDao implements PartyDao {

    private final RowMapper<Party> partyRowMapper;

    public PartyDaoImpl(DataSource dataSource) {
        super(dataSource);
        partyRowMapper = new RecordRowMapper<>(PARTY, Party.class);
    }

    @Override
    public Long getLastEventId() throws DaoException {
        Query query = getDslContext().select(DSL.max(DSL.field("event_id"))).from(
                getDslContext().select(PARTY.EVENT_ID.max().as("event_id")).from(PARTY)
                        .unionAll(getDslContext().select(CONTRACT.EVENT_ID.max().as("event_id")).from(CONTRACT))
                        .unionAll(getDslContext().select(CONTRACTOR.EVENT_ID.max().as("event_id")).from(CONTRACTOR))
                        .unionAll(getDslContext().select(SHOP.EVENT_ID.max().as("event_id")).from(SHOP))
        );
        return fetchOne(query, Long.class);
    }

    @Override
    public Long save(Party party) throws DaoException {
        PartyRecord record = getDslContext().newRecord(PARTY, party);
        Query query = getDslContext().insertInto(PARTY).set(record).returning(PARTY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Party get(String partyId) throws DaoException {
        Query query = getDslContext().selectFrom(PARTY)
                .where(PARTY.PARTY_ID.eq(partyId).and(PARTY.CURRENT));

        return fetchOne(query, partyRowMapper);
    }

    @Override
    public void updateNotCurrent(String partyId) throws DaoException {
        Query query = getDslContext().update(PARTY).set(PARTY.CURRENT, false)
                .where(PARTY.PARTY_ID.eq(partyId).and(PARTY.CURRENT));
        execute(query);
    }
}
