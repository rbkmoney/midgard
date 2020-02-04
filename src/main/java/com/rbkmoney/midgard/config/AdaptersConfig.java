package com.rbkmoney.midgard.config;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.config.props.ClearingServiceProperties;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
public class AdaptersConfig {

    @Bean
    public List<ClearingAdapter> clearingAdapters(ClearingServiceProperties properties) throws IOException {
        List<ClearingAdapter> clearingAdapterList = new CopyOnWriteArrayList<>();
        for (ClearingServiceProperties.AdapterProperties props : properties.getAdapters()) {
            clearingAdapterList.add(
                    new ClearingAdapter(mockClearingAdapterThriftClient(props),
                            props.getName(),
                            props.getProviderId())
            );
        }
        return clearingAdapterList;
    }

    public ClearingAdapterSrv.Iface mockClearingAdapterThriftClient(ClearingServiceProperties.AdapterProperties props)
            throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(props.getUrl().getURI())
                .withNetworkTimeout(props.getNetworkTimeout())
                .build(ClearingAdapterSrv.Iface.class);
    }

}
