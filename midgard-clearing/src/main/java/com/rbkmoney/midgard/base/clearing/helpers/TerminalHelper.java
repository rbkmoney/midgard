package com.rbkmoney.midgard.base.clearing.helpers;

import com.rbkmoney.midgard.base.clearing.helpers.dao.TerminalDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Terminal;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TerminalHelper {

    private final TerminalDao terminalDao;

    public Terminal getTerminal(int terminalRefId) {
        return terminalDao.getTerminal(terminalRefId);
    }

}
