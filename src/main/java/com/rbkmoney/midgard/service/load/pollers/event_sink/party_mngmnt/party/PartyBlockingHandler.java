package com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.party;

import com.rbkmoney.damsel.domain.Blocking;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.load.dao.party.iface.PartyDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.AbstractPartyManagementHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Party;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PartyBlockingHandler extends AbstractPartyManagementHandler {

    private final PartyDao partyDao;

    private final Filter filter;

    public PartyBlockingHandler(PartyDao partyDao) {
        this.partyDao = partyDao;
        this.filter = new PathConditionFilter(new PathConditionRule(
                "party_blocking",
                new IsNullCondition().not()));
    }

    @Override
    @Transactional
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        Blocking partyBlocking = change.getPartyBlocking();
        String partyId = event.getSource().getPartyId();
        log.info("Start party blocking handling, eventId={}, partyId={}", eventId, partyId);
        Party partySource = partyDao.get(partyId);
        if (partySource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Party not found, partyId='{}'", partyId);
            return;
            //throw new NotFoundException(String.format("Party not found, partyId='%s'", partyId));
        }
        partySource.setId(null);
        partySource.setWtime(null);
        partySource.setEventId(eventId);
        partySource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        partySource.setBlocking(TBaseUtil.unionFieldToEnum(partyBlocking, org.jooq.generated.feed.enums.Blocking.class));
        if (partyBlocking.isSetUnblocked()) {
            partySource.setBlockingUnblockedReason(partyBlocking.getUnblocked().getReason());
            partySource.setBlockingUnblockedSince(TypeUtil.stringToLocalDateTime(partyBlocking.getUnblocked().getSince()));
            partySource.setBlockingBlockedReason(null);
            partySource.setBlockingBlockedSince(null);
        } else if (partyBlocking.isSetBlocked()) {
            partySource.setBlockingUnblockedReason(null);
            partySource.setBlockingUnblockedSince(null);
            partySource.setBlockingBlockedReason(partyBlocking.getBlocked().getReason());
            partySource.setBlockingBlockedSince(TypeUtil.stringToLocalDateTime(partyBlocking.getBlocked().getSince()));
        }
        partyDao.updateNotCurrent(partyId);
        partyDao.save(partySource);
        log.info("Party blocking has been saved, eventId={}, partyId={}", eventId, partyId);
    }

    @Override
    public Filter<PartyChange> getFilter() {
        return filter;
    }
}
