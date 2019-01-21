package com.rbkmoney.midgard.service.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="clearing-service.adapters.test")
public class TestAdapterProps extends AdapterProps { }
