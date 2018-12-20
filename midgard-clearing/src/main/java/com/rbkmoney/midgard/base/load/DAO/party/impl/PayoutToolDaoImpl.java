package com.rbkmoney.midgard.base.load.DAO.party.impl;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.DAO.party.iface.PayoutToolDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.RecordRowMapper;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.PayoutTool;
import org.jooq.generated.feed.tables.records.PayoutToolRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static org.jooq.generated.feed.tables.PayoutTool.PAYOUT_TOOL;


@Component
public class PayoutToolDaoImpl extends AbstractGenericDao implements PayoutToolDao {

    private final RowMapper<PayoutTool> payoutToolRowMapper;

    public PayoutToolDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.payoutToolRowMapper = new RecordRowMapper<>(PAYOUT_TOOL, PayoutTool.class);
    }

    @Override
    public void save(List<PayoutTool> payoutToolList) throws DaoException {
        //todo: Batch insert
        for (PayoutTool payoutTool : payoutToolList) {
            PayoutToolRecord record = getDslContext().newRecord(PAYOUT_TOOL, payoutTool);
            Query query = getDslContext().insertInto(PAYOUT_TOOL).set(record);
            execute(query);
        }
    }

    @Override
    public List<PayoutTool> getByCntrctId(Long cntrctId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYOUT_TOOL)
                .where(PAYOUT_TOOL.CNTRCT_ID.eq(cntrctId));
        return fetch(query, payoutToolRowMapper);
    }
}
