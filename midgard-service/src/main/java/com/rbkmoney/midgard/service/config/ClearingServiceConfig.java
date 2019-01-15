package com.rbkmoney.midgard.service.config;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.service.config.props.MtsAdapterProps;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ClearingServiceConfig {

    @Bean
    public ClearingAdapterSrv.Iface mtsClearingAdapterThriftClient(MtsAdapterProps props) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(props.getUrl().getURI())
                .withNetworkTimeout(props.getNetworkTimeout())
                .build(ClearingAdapterSrv.Iface.class);
    }

}
