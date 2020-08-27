package com.rbkmoney.midgard.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "service.schedulator")
public class SchedulatorServiceProperties {

    private Resource url;
    private int networkTimeout;
    private Integer retryInitialInterval;
    private Integer retryMaxAttempts;
    private Integer retryMaxInterval;

}
