package com.rbkmoney.midgard.service.config;

import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.midgard.service.config.props.PartyManagementProperties;
import com.rbkmoney.midgard.service.load.pollers.event_sink.PartyManagementEventStockHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
