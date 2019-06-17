package com.rbkmoney.midgard.service.config;

import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.midgard.service.config.props.InvoicingProperties;
import com.rbkmoney.midgard.service.config.props.PartyManagementProperties;
import com.rbkmoney.midgard.service.load.pollers.event_sink.InvoicingEventStockHandler;
import com.rbkmoney.midgard.service.load.pollers.event_sink.PartyManagementEventStockHandler;
import com.rbkmoney.midgard.service.load.services.InvoicingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataLoadConfig {

    @Bean
    public EventPublisher partyManagementEventPublisher(
            PartyManagementEventStockHandler partyMngmntEventStockHandler,
            PartyManagementProperties partyManagementProperties
    ) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(partyManagementProperties.getUrl().getURI())
                .withEventHandler(partyMngmntEventStockHandler)
                .withMaxPoolSize(partyManagementProperties.getPolling().getMaxPoolSize())
                .withEventRetryDelay(partyManagementProperties.getPolling().getRetryDelay())
                .withPollDelay(partyManagementProperties.getPolling().getDelay())
                .withMaxQuerySize(partyManagementProperties.getPolling().getMaxQuerySize())
                .build();
    }

    @Bean
    public List<InvoicingEventStockHandler> invoicingEventStockHandlers(
            InvoicingService invoicingService,
            @Value("${bm.invoicing.workersCount}") int workersCount){
        List<InvoicingEventStockHandler> invoicingEventStockHandlers = new ArrayList<>();
        for (int i = 0; i < workersCount; ++i) {
            invoicingEventStockHandlers.add(new InvoicingEventStockHandler(invoicingService, workersCount, i));
        }
        return invoicingEventStockHandlers;
    }

    @Bean(name = "invoicingEventPublishers")
    public List<EventPublisher> invoicingEventPublishers(
            List<InvoicingEventStockHandler> invoicingEventStockHandlers,
            InvoicingProperties invoicingProperties,
            @Value("${bm.invoicing.workersCount}") int workersCount
    ) throws IOException {
        List<EventPublisher> eventPublishers = new ArrayList<>();
        for (int i = 0; i < workersCount; ++i) {
            eventPublishers.add(new PollingEventPublisherBuilder()
                    .withURI(invoicingProperties.getUrl().getURI())
                    .withEventHandler(invoicingEventStockHandlers.get(i))
                    .withMaxPoolSize(invoicingProperties.getPolling().getMaxPoolSize())
                    .withEventRetryDelay(invoicingProperties.getPolling().getRetryDelay())
                    .withPollDelay(invoicingProperties.getPolling().getDelay())
                    .withMaxQuerySize(invoicingProperties.getPolling().getMaxQuerySize())
                    .build());
        }
        return eventPublishers;
    }

}
