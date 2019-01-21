package com.rbkmoney.midgard.service.load.pollers.event_sink;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.midgard.service.load.services.InvoicingService;
import com.rbkmoney.midgard.service.load.utils.HashUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public class InvoicingEventStockHandler implements EventHandler<StockEvent> {

    private final InvoicingService invoicingService;

    private final int divider;

    private final int mod;

    @Override
    public EventAction handle(StockEvent stockEvent, String subsKey) {
        Event processingEvent = stockEvent.getSourceEvent().getProcessingEvent();
        EventPayload payload = processingEvent.getPayload();
        if (payload.isSetInvoiceChanges()) {
            if (HashUtil.checkHashMod(processingEvent.getSource().getInvoiceId(), divider, mod)) {
                try {
                    invoicingService.handleEvents(processingEvent, payload);
                } catch (RuntimeException e) {
                    log.error("Error when polling invoicing event with id={}", processingEvent.getId(), e);
                    return EventAction.DELAYED_RETRY;
                }
            }
        }
        return EventAction.CONTINUE;
    }

}
