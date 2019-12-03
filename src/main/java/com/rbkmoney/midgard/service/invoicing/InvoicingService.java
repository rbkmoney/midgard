package com.rbkmoney.midgard.service.invoicing;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.handler.invoicing.AbstractInvoicingEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicingService implements EventService<MachineEvent, EventPayload> {

    private final List<AbstractInvoicingEventHandler> invoicingHandlers;

    @Value("${clearing-service.error-timeout}")
    private long errorTimeout = 1000L;

    @Override
    public void handleEvents(MachineEvent simpleEvent, EventPayload payload) {
        try {
            log.debug("Handling event with machineId='{}' and eventId='{}'", simpleEvent.getSourceId(),
                    simpleEvent.getEventId());
            List<InvoiceChange> invoiceChanges = payload.getInvoiceChanges();
            for (int i = 0; i < invoiceChanges.size(); i++) {
                InvoiceChange change = invoiceChanges.get(i);
                for (AbstractInvoicingEventHandler invoicingHandler : invoicingHandlers) {
                    if (invoicingHandler.accept(change)) {
                        invoicingHandler.handle(change, simpleEvent, i);
                    }
                }
            }
        } catch (Throwable e) {
            log.error("Unexpected error while handling event with machineId='{}' and eventId='{}'",
                    simpleEvent.getSourceId(), simpleEvent.getEventId(), e);
            errorWait(errorTimeout);
            throw new RuntimeException("Unexpected error while handling event with machineId='" +
                    simpleEvent.getSourceId() + "' and eventId='" + simpleEvent.getEventId() + "'");
        }
    }

    private void errorWait(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
            log.error("InvoicingService InterruptedException when wait retry", ex);
            Thread.currentThread().interrupt();
        }
    }

}
