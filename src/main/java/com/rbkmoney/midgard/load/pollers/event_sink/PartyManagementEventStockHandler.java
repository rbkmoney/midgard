package com.rbkmoney.midgard.load.pollers.event_sink;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.midgard.load.services.PartyManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PartyManagementEventStockHandler implements EventHandler<StockEvent> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PartyManagementService partyManagementService;

    public PartyManagementEventStockHandler(PartyManagementService partyManagementService) {
        this.partyManagementService = partyManagementService;
    }

    @Override
    public EventAction handle(StockEvent stockEvent, String subsKey) {
        Event processingEvent = stockEvent.getSourceEvent().getProcessingEvent();
        EventPayload payload = processingEvent.getPayload();

        try {
            partyManagementService.handleEvents(processingEvent, payload);
        } catch (RuntimeException e) {
            log.error("Error when polling party management event with id={}", processingEvent.getId(), e);
            return EventAction.DELAYED_RETRY;
        }
        return EventAction.CONTINUE;
    }
}
