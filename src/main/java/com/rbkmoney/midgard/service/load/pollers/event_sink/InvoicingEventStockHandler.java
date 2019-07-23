package com.rbkmoney.midgard.service.load.pollers.event_sink;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.services.InvoicingService;
import com.rbkmoney.midgard.service.load.utils.HashUtil;
import com.rbkmoney.midgard.service.load.utils.MapperUtil;
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
        EventPayload payload = stockEvent.getSourceEvent().getProcessingEvent().getPayload();

        if (payload.isSetInvoiceChanges()) {
            SimpleEvent event = MapperUtil.transformSinkEvent(stockEvent.getSourceEvent().getProcessingEvent());
            if (HashUtil.checkHashMod(event.getSourceId(), divider, mod)) {
                try {
                    invoicingService.handleEvents(event, payload);
                } catch (RuntimeException e) {
                    log.error("Error when polling invoicing event with id={}", event.getSourceId(), e);
                    return EventAction.DELAYED_RETRY;
                }
            }
        }
        return EventAction.CONTINUE;
    }

}
