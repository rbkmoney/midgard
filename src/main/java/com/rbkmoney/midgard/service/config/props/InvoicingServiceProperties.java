package com.rbkmoney.midgard.service.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="clearing-service.invoicing")
public class InvoicingServiceProperties extends BaseProperties {
}
