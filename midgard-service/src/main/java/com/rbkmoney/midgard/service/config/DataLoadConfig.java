package com.rbkmoney.midgard.service.config;

import com.rbkmoney.damsel.domain_config.RepositorySrv;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.midgard.service.load.pollers.event_sink.InvoicingEventStockHandler;
import com.rbkmoney.midgard.service.load.pollers.event_sink.PartyManagementEventStockHandler;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

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
    public EventPublisher invoicingEventPublisher(
            InvoicingEventStockHandler invoicingEventStockHandler,
            InvoicingProperties invoicingProperties
    ) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(invoicingProperties.getUrl().getURI())
                .withEventHandler(invoicingEventStockHandler)
                .withMaxPoolSize(invoicingProperties.getPolling().getMaxPoolSize())
                .withEventRetryDelay(invoicingProperties.getPolling().getRetryDelay())
                .withPollDelay(invoicingProperties.getPolling().getDelay())
                .withMaxQuerySize(invoicingProperties.getPolling().getMaxQuerySize())
                .build();
    }

    @Bean
    public RepositorySrv.Iface dominantClient(@Value("${dmt.url}") Resource resource,
                                              @Value("${dmt.networkTimeout}") int networkTimeout
    ) throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(RepositorySrv.Iface.class);
    }

}
