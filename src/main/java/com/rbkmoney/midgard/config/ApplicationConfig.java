package com.rbkmoney.midgard.config;

import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.midgard.config.props.InvoicingServiceProperties;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.IOException;

@Configuration
public class ApplicationConfig {

    @Value("${clearing-service.scheduler-pool-size}")
    private int schedulerPoolSize;

    @Bean
    public InvoicingSrv.Iface invoicingThriftClient(InvoicingServiceProperties props) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(props.getUrl().getURI())
                .withNetworkTimeout(props.getNetworkTimeout())
                .build(InvoicingSrv.Iface.class);
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(schedulerPoolSize);
        return  taskScheduler;
    }

}
