package com.rbkmoney.midgard.service.config;

import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.damsel.payment_processing.ServiceUser;
import com.rbkmoney.damsel.payment_processing.UserInfo;
import com.rbkmoney.damsel.payment_processing.UserType;
import com.rbkmoney.midgard.service.config.props.InvoicingServiceProperties;
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

    @Bean
    public UserInfo userInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId("someId");
        userInfo.setType(UserType.service_user(new ServiceUser()));
        return userInfo;
    }

}
