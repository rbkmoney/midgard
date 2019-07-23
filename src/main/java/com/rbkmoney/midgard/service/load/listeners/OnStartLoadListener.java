package com.rbkmoney.midgard.service.load.listeners;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.eventstock.client.DefaultSubscriberConfig;
import com.rbkmoney.eventstock.client.EventConstraint;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.SubscriberConfig;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.InvoicingEventStockHandler;
import com.rbkmoney.midgard.service.load.services.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(value = "bm.pollingEnabled", havingValue = "true")
public class OnStartLoadListener implements ApplicationListener<ApplicationReadyEvent> {

    private final EventPublisher partyManagementEventPublisher;

    private final EventService<Event, EventPayload> partyManagementService;

    private final List<EventPublisher> invoicingEventPublishers;

    private final List<InvoicingEventStockHandler> invoicingEventStockHandlers;

    private final EventService<SimpleEvent, EventPayload> invoicingService;

    public OnStartLoadListener(EventPublisher partyManagementEventPublisher,
                               @Qualifier("invoicingEventPublishers")
                                       List<EventPublisher> invoicingEventPublishers,
                               List<InvoicingEventStockHandler> invoicingEventStockHandlers,

                               EventService<Event, EventPayload> partyManagementService,
                               EventService<SimpleEvent, EventPayload> invoicingService) {
        this.partyManagementEventPublisher = partyManagementEventPublisher;
        this.invoicingEventPublishers = invoicingEventPublishers;
        this.invoicingEventStockHandlers = invoicingEventStockHandlers;

        this.partyManagementService = partyManagementService;
        this.invoicingService = invoicingService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            partyManagementEventPublisher.subscribe(buildSubscriberConfig(partyManagementService.getLastEventId()));
            for (int i = 0; i < invoicingEventPublishers.size(); ++i) {
                InvoicingEventStockHandler invoicingEventStockHandler = invoicingEventStockHandlers.get(i);
                Optional<Long> lastEventId = invoicingService.getLastEventId(invoicingEventStockHandler.getDivider(),
                        invoicingEventStockHandler.getMod());
                invoicingEventPublishers.get(i).subscribe(buildSubscriberConfig(lastEventId));
            }
        } catch (Exception e) {
            log.error("Error occurred while subscribing", e);
        }
    }

    private SubscriberConfig buildSubscriberConfig(Optional<Long> lastEventIdOptional) {
        EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();
        lastEventIdOptional.ifPresent(eventIDRange::setFromExclusive);
        EventFlowFilter eventFlowFilter = new EventFlowFilter(new EventConstraint(eventIDRange));
        return new DefaultSubscriberConfig(eventFlowFilter);
    }

}
