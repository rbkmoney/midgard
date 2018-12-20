package com.rbkmoney.midgard.base.load.DAO.party.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.DAO.party.iface.ContractAdjustmentDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.RecordRowMapper;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.ContractAdjustment;
import org.jooq.generated.feed.tables.records.ContractAdjustmentRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static org.jooq.generated.feed.tables.ContractAdjustment.CONTRACT_ADJUSTMENT;

@Component
public class ContractAdjustmentDaoImpl extends AbstractGenericDao implements ContractAdjustmentDao {

    private final RowMapper<ContractAdjustment> contractAdjustmentRowMapper;

    public ContractAdjustmentDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.contractAdjustmentRowMapper = new RecordRowMapper<>(CONTRACT_ADJUSTMENT, ContractAdjustment.class);;
    }

    @Override
    public void save(List<ContractAdjustment> contractAdjustmentList) throws DaoException {
        //todo: Batch insert
        for (ContractAdjustment contractAdjustment : contractAdjustmentList) {
            ContractAdjustmentRecord record = getDslContext().newRecord(CONTRACT_ADJUSTMENT, contractAdjustment);
            Query query = getDslContext().insertInto(CONTRACT_ADJUSTMENT).set(record);
            execute(query);
        }
    }

    @Override
    public List<ContractAdjustment> getByCntrctId(Long cntrctId) throws DaoException {
        Query query = getDslContext().selectFrom(CONTRACT_ADJUSTMENT)
                .where(CONTRACT_ADJUSTMENT.CNTRCT_ID.eq(cntrctId));
        return fetch(query, contractAdjustmentRowMapper);
    }
}
