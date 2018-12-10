package com.rbkmoney.midgard.load.services;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.load.DAO.party.iface.PartyDao;
import com.rbkmoney.midgard.load.pollers.event_sink.party_mngmnt.AbstractPartyManagementHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PartyManagementService implements EventService<Event, EventPayload> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PartyDao partyDao;

    private final List<AbstractPartyManagementHandler> partyManagementHandlers;

    public PartyManagementService(PartyDao partyDao, List<AbstractPartyManagementHandler> partyManagementHandlers) {
        this.partyDao = partyDao;
        this.partyManagementHandlers = partyManagementHandlers;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(Event processingEvent, EventPayload payload) {
        if (payload.isSetPartyChanges()) {
            payload.getPartyChanges().forEach(cc -> partyManagementHandlers.forEach(ph -> {
                if (ph.accept(cc)) {
                    ph.handle(cc, processingEvent);
                }
            }));
        }
    }

    @Override
    public Optional<Long> getLastEventId() throws DaoException {
        Optional<Long> lastEventId = Optional.ofNullable(partyDao.getLastEventId());
        log.info("Last party management eventId={}", lastEventId);
        return lastEventId;
    }
}
