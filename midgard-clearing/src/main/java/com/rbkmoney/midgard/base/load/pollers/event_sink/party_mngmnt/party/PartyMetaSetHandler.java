package com.rbkmoney.midgard.base.load.pollers.event_sink.party_mngmnt.party;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.damsel.payment_processing.PartyMetaSet;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.base.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.base.load.dao.party.iface.PartyDao;
import com.rbkmoney.midgard.base.load.utils.JsonUtil;
import com.rbkmoney.midgard.base.load.pollers.event_sink.party_mngmnt.AbstractPartyManagementHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Party;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PartyMetaSetHandler extends AbstractPartyManagementHandler {

    private final PartyDao partyDao;

    private final Filter filter;

    public PartyMetaSetHandler(PartyDao partyDao) {
        this.partyDao = partyDao;
        this.filter = new PathConditionFilter(new PathConditionRule(
                "party_meta_set",
                new IsNullCondition().not()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        PartyMetaSet partyMetaSet = change.getPartyMetaSet();
        String partyId = event.getSource().getPartyId();
        log.info("Start party metaset handling, eventId={}, partyId={}", eventId, partyId);
        Party partySource = partyDao.get(partyId);
        if (partySource == null) {
            throw new NotFoundException(String.format("Party not found, partyId='%s'", partyId));
        }
        partySource.setId(null);
        partySource.setWtime(null);
        partySource.setEventId(eventId);
        partySource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        partySource.setPartyMetaSetNs(partyMetaSet.getNs());
        partySource.setPartyMetaSetDataJson(JsonUtil.tBaseToJsonString(partyMetaSet.getData()));
        partyDao.updateNotCurrent(partyId);
        partyDao.save(partySource);
        log.info("Party metaset has been saved, eventId={}, partyId={}", eventId, partyId);
    }

    @Override
    public Filter<PartyChange> getFilter() {
        return filter;
    }
}
