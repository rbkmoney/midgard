package com.rbkmoney.midgard.load.DAO.party.impl;

import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.RecordRowMapper;
import com.rbkmoney.midgard.load.DAO.party.iface.ContractorDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Contractor;
import org.jooq.generated.feed.tables.records.ContractorRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static org.jooq.generated.feed.tables.Contractor.CONTRACTOR;


@Component
public class ContractorDaoImpl extends AbstractGenericDao implements ContractorDao {

    private final RowMapper<Contractor> contractorRowMapper;

    public ContractorDaoImpl(DataSource dataSource) {
        super(dataSource);
        contractorRowMapper = new RecordRowMapper<>(CONTRACTOR, Contractor.class);
    }

    @Override
    public Long save(Contractor contractor) throws DaoException {
        ContractorRecord record = getDslContext().newRecord(CONTRACTOR, contractor);
        Query query = getDslContext().insertInto(CONTRACTOR).set(record).returning(CONTRACTOR.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Contractor get(String partyId, String contractorId) throws DaoException {
        Query query = getDslContext().selectFrom(CONTRACTOR)
                .where(CONTRACTOR.PARTY_ID.eq(partyId).and(CONTRACTOR.CONTRACTOR_ID.eq(contractorId)).and(CONTRACTOR.CURRENT));

        return fetchOne(query, contractorRowMapper);
    }

    @Override
    public void updateNotCurrent(String partyId, String contractId) throws DaoException {
        Query query = getDslContext().update(CONTRACTOR).set(CONTRACTOR.CURRENT, false)
                .where(CONTRACTOR.PARTY_ID.eq(partyId).and(CONTRACTOR.CONTRACTOR_ID.eq(contractId)).and(CONTRACTOR.CURRENT));
        execute(query);
    }

    @Override
    public List<Contractor> getByPartyId(String partyId) {
        Query query = getDslContext().selectFrom(CONTRACTOR)
                .where(CONTRACTOR.PARTY_ID.eq(partyId).and(CONTRACTOR.CURRENT));
        return fetch(query, contractorRowMapper);
    }
}
