package com.rbkmoney.midgard.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="clearing-service.adapters.mts")
public class MtsAdapterProperties extends AdapterProperties { }
