package com.rbkmoney.midgard.config;

import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.damsel.schedule.SchedulatorSrv;
import com.rbkmoney.midgard.config.props.BaseProperties;
import com.rbkmoney.midgard.config.props.ClearingServiceProperties;
import com.rbkmoney.midgard.config.props.SchedulatorServiceProperties;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.IOException;

@Configuration
public class ApplicationConfig {

    @Value("${clearing-service.scheduler-pool-size}")
    private int schedulerPoolSize;

    @Bean
    public InvoicingSrv.Iface invoicingThriftClient(ClearingServiceProperties clearingServiceProperties)
            throws IOException {
        BaseProperties props = clearingServiceProperties.getInvoicingService();
        return new THSpawnClientBuilder()
                .withAddress(props.getUrl().getURI())
                .withNetworkTimeout(props.getNetworkTimeout())
                .build(InvoicingSrv.Iface.class);
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(schedulerPoolSize);
        return  taskScheduler;
    }

    @Bean
    public RetryTemplate retryTemplate(SchedulatorServiceProperties props) {
        final ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setMaxInterval(props.getRetryMaxInterval());
        exponentialBackOffPolicy.setInitialInterval(props.getRetryInitialInterval());

        final RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(props.getRetryMaxAttempts()));
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);

        return retryTemplate;
    }

    @Bean
    public SchedulatorSrv.Iface schedulatorClient(SchedulatorServiceProperties props)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(props.getNetworkTimeout())
                .withAddress(props.getUrl().getURI())
                .build(SchedulatorSrv.Iface.class);
    }

}
