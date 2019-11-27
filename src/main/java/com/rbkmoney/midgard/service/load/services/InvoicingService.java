package com.rbkmoney.midgard.service.load.services;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.handler.invoicing.AbstractInvoicingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicingService implements EventService<SimpleEvent, EventPayload> {

    private final List<AbstractInvoicingHandler> invoicingHandlers;

    @Value("${import.init-last-event-id}")
    private long initLastEventId;

    @Override
    public void handleEvents(SimpleEvent simpleEvent, EventPayload payload) {
        try {
            log.info("Handling event with machineId='{}' and eventId='{}' received from {}",
                    simpleEvent.getSourceId(), simpleEvent.getEventId(), simpleEvent.getEventSourceName());
            List<InvoiceChange> invoiceChanges = payload.getInvoiceChanges();
            for (int i = 0; i < invoiceChanges.size(); i++) {
                InvoiceChange change = invoiceChanges.get(i);
                for (AbstractInvoicingHandler invoicingHandler : invoicingHandlers) {
                    if (invoicingHandler.accept(change)) {
                        invoicingHandler.handle(change, simpleEvent, i);
                    }
                }
            }
        } catch (Throwable e) {
            log.error("Unexpected error while handling event with machineId='{}' and eventId='{}' received from {}",
                    simpleEvent.getSourceId(), simpleEvent.getEventId(), simpleEvent.getEventSourceName(), e);
            throw new RuntimeException("Unexpected error while handling event with machineId='" +
                    simpleEvent.getSourceId() + "' and eventId='" + simpleEvent.getEventId() + "'");
        }
    }

}
