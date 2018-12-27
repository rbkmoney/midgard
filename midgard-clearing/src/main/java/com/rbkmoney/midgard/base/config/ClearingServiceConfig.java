package com.rbkmoney.midgard.base.config;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class ClearingServiceConfig {

    @Bean
    public ClearingAdapterSrv.Iface сlearingAdapterThriftClient(
            @Value("${clearing-service.adapters.mts.url}") Resource resource,
            @Value("${clearing-service.adapters.mts.networkTimeout}") int networkTimeout
    ) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .withNetworkTimeout(networkTimeout)
                .build(ClearingAdapterSrv.Iface.class);
    }

}
