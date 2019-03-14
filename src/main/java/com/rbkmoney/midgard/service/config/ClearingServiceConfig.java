package com.rbkmoney.midgard.service.config;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.config.props.MtsAdapterProps;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ClearingServiceConfig {

    @Bean
    public ClearingAdapterSrv.Iface mtsClearingAdapterThriftClient(MtsAdapterProps props) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(props.getUrl().getURI())
                .withNetworkTimeout(props.getNetworkTimeout())
                .build(ClearingAdapterSrv.Iface.class);
    }

    @Bean
    public ClearingAdapter mtsClearingAdapter(MtsAdapterProps props) throws IOException {
        return new ClearingAdapter(mtsClearingAdapterThriftClient(props),
                props.getName(),
                props.getProviderId());
    }

}
