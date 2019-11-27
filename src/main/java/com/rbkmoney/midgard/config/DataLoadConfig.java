package com.rbkmoney.midgard.config;

import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.midgard.config.props.InvoicingServiceProperties;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class DataLoadConfig {

    @Bean
    public InvoicingSrv.Iface invoicingThriftClient(InvoicingServiceProperties props) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(props.getUrl().getURI())
                .withNetworkTimeout(props.getNetworkTimeout())
                .build(InvoicingSrv.Iface.class);
    }

}
