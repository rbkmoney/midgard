package com.rbkmoney.midgard.service.load.services;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.load.dao.party.iface.PartyDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.AbstractPartyManagementHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PartyManagementService implements EventService<Event, EventPayload> {

    private final PartyDao partyDao;

    private final List<AbstractPartyManagementHandler> partyManagementHandlers;

    @Override
    @Transactional
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

    @Override
    public Optional<Long> getLastEventId(int div, int mod) throws Exception {
        throw new Exception("The method is not implemented");
    }

}
