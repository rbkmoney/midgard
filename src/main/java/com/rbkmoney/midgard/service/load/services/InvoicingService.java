package com.rbkmoney.midgard.service.load.services;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicingService implements EventService<MachineEvent, EventPayload> {

    private final List<AbstractInvoicingHandler> invoicingHandlers;

    @Value("${import.init-last-event-id}")
    private long initLastEventId;

    @Override
    @Transactional
    public void handleEvents(MachineEvent machineEvent, EventPayload payload) {
        List<InvoiceChange> invoiceChanges = payload.getInvoiceChanges();
        for (int i = 0; i < invoiceChanges.size(); i++) {
            InvoiceChange change = invoiceChanges.get(i);
            for (AbstractInvoicingHandler invoicingHandler : invoicingHandlers) {
                if (invoicingHandler.accept(change)) {
                    invoicingHandler.handle(change, machineEvent, i);
                }
            }
        }
    }

    @Override
    public Optional<Long> getLastEventId() {
        throw new RuntimeException("No longer supported");
    }

}
