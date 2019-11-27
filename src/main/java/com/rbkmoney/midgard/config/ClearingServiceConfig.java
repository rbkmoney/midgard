package com.rbkmoney.midgard.config;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.config.props.MtsAdapterProperties;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.IOException;

@Configuration
public class ClearingServiceConfig {

    @Value("${clearing-service.scheduler-pool-size}")
    private int schedulerPoolSize;

    @Bean
    public ClearingAdapterSrv.Iface mtsClearingAdapterThriftClient(MtsAdapterProperties props) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(props.getUrl().getURI())
                .withNetworkTimeout(props.getNetworkTimeout())
                .build(ClearingAdapterSrv.Iface.class);
    }

    @Bean
    public ClearingAdapter mtsClearingAdapter(MtsAdapterProperties props) throws IOException {
        return new ClearingAdapter(mtsClearingAdapterThriftClient(props),
                props.getName(),
                props.getProviderId());
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(schedulerPoolSize);
        return  taskScheduler;
    }

}