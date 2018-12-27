package com.rbkmoney.midgard.base.config;

import com.rbkmoney.damsel.domain_config.RepositorySrv;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.midgard.base.load.pollers.event_sink.InvoicingEventStockHandler;
import com.rbkmoney.midgard.base.load.pollers.event_sink.PartyManagementEventStockHandler;
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
            @Value("${bm.partyManagement.url}") Resource resource,
            @Value("${bm.partyManagement.polling.delay}") int pollDelay,
            @Value("${bm.partyManagement.polling.retryDelay}") int retryDelay,
            @Value("${bm.partyManagement.polling.maxPoolSize}") int maxPoolSize,
            @Value("${bm.partyManagement.polling.maxQuerySize}") int maxQuerySize
    ) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(resource.getURI())
                .withEventHandler(partyMngmntEventStockHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .withMaxQuerySize(maxQuerySize)
                .build();
    }

    @Bean
    public EventPublisher invoicingEventPublisher(
            InvoicingEventStockHandler invoicingEventStockHandler,
            @Value("${bm.invoicing.url}") Resource resource,
            @Value("${bm.invoicing.polling.delay}") int pollDelay,
            @Value("${bm.invoicing.polling.retryDelay}") int retryDelay,
            @Value("${bm.invoicing.polling.maxPoolSize}") int maxPoolSize,
            @Value("${bm.invoicing.polling.maxQuerySize}") int maxQuerySize
    ) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(resource.getURI())
                .withEventHandler(invoicingEventStockHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .withMaxQuerySize(maxQuerySize)
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
