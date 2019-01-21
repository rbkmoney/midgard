package com.rbkmoney.midgard.service.load.listeners;

import com.rbkmoney.eventstock.client.DefaultSubscriberConfig;
import com.rbkmoney.eventstock.client.EventConstraint;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.SubscriberConfig;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.midgard.service.load.pollers.event_sink.InvoicingEventStockHandler;
import com.rbkmoney.midgard.service.load.services.InvoicingService;
import com.rbkmoney.midgard.service.load.services.PartyManagementService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnStartLoadListener implements ApplicationListener<ApplicationReadyEvent> {

    private final EventPublisher partyManagementEventPublisher;

    private final List<EventPublisher> invoicingEventPublishers;

    private final List<InvoicingEventStockHandler> invoicingEventStockHandlers;

    private final PartyManagementService partyManagementService;

    private final InvoicingService invoicingService;

    @Value("${bm.pollingEnabled}")
    private boolean pollingEnabled;

    public OnStartLoadListener(EventPublisher partyManagementEventPublisher,
                               @Qualifier("invoicingEventPublishers")
                                       List<EventPublisher> invoicingEventPublishers,
                               List<InvoicingEventStockHandler> invoicingEventStockHandlers,

                               PartyManagementService partyManagementService,
                               InvoicingService invoicingService) {
        this.partyManagementEventPublisher = partyManagementEventPublisher;
        this.invoicingEventPublishers = invoicingEventPublishers;
        this.invoicingEventStockHandlers = invoicingEventStockHandlers;

        this.partyManagementService = partyManagementService;
        this.invoicingService = invoicingService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (pollingEnabled) {
            partyManagementEventPublisher.subscribe(buildSubscriberConfig(partyManagementService.getLastEventId()));
            for (int i = 0; i < invoicingEventPublishers.size(); ++i) {
                InvoicingEventStockHandler invoicingEventStockHandler = invoicingEventStockHandlers.get(i);
                Optional<Long> lastEventId = invoicingService.getLastEventId(invoicingEventStockHandler.getDivider(),
                        invoicingEventStockHandler.getMod());
                invoicingEventPublishers.get(i).subscribe(buildSubscriberConfig(lastEventId));
            }
        }
    }

    private SubscriberConfig buildSubscriberConfig(Optional<Long> lastEventIdOptional) {
        EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();
        lastEventIdOptional.ifPresent(eventIDRange::setFromExclusive);
        EventFlowFilter eventFlowFilter = new EventFlowFilter(new EventConstraint(eventIDRange));
        return new DefaultSubscriberConfig(eventFlowFilter);
    }

}
