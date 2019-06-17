package com.rbkmoney.midgard.service.config.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka.ssl")
public class KafkaSslProperties {

    private String trustStorePassword;
    private String trustStoreLocation;
    private String keyStorePassword;
    private String keyPassword;
    private String keyStoreLocation;
    private boolean enabled;
    private String keyStoreType;
    private String trustStoreType;

}