package com.rbkmoney.midgard.service.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="bm.invoicing")
public class InvoicingProperties extends PoolingEventProperties {

}