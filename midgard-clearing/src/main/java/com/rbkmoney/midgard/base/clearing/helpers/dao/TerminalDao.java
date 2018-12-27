package com.rbkmoney.midgard.base.clearing.helpers.dao;

import com.rbkmoney.midgard.base.clearing.helpers.dao.common.AbstractGenericDao;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.RecordRowMapper;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Terminal;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Terminal.TERMINAL;

@Component
public class TerminalDao extends AbstractGenericDao {

    private final RowMapper<Terminal> terminalRowMapper;

    public TerminalDao(DataSource dataSource) {
        super(dataSource);
        terminalRowMapper = new RecordRowMapper<>(TERMINAL, Terminal.class);
    }

    public Terminal getTerminal(int terminalRefId) {
        Query query = getDslContext().selectFrom(TERMINAL)
                .where(TERMINAL.TERMINAL_REF_ID.eq(terminalRefId))
                .and(TERMINAL.CURRENT.eq(true));
        return fetchOne(query, terminalRowMapper);
    }

}
