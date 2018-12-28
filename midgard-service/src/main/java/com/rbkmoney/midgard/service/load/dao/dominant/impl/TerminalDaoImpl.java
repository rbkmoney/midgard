package com.rbkmoney.midgard.service.load.dao.dominant.impl;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.load.dao.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Terminal;
import org.jooq.generated.feed.tables.records.TerminalRecord;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Terminal.TERMINAL;

@Component
public class TerminalDaoImpl extends AbstractGenericDao implements DomainObjectDao<Terminal, Integer> {

    public TerminalDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Terminal terminal) throws DaoException {
        TerminalRecord terminalRecord = getDslContext().newRecord(TERMINAL, terminal);
        Query query = getDslContext().insertInto(TERMINAL).set(terminalRecord).returning(TERMINAL.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer terminalId) throws DaoException {
        Query query = getDslContext().update(TERMINAL).set(TERMINAL.CURRENT, false)
                .where(TERMINAL.TERMINAL_REF_ID.eq(terminalId).and(TERMINAL.CURRENT));
        execute(query);
    }
}
