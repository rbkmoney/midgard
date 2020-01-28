package com.rbkmoney.midgard.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "service.schedulator")
public class SchedulatorServiceProperties {

    @NotEmpty
    private String url;

    @NotNull
    private Integer networkTimeout;

    @NotNull
    private Integer retryInitialInterval;

    @NotNull
    private Integer retryMaxAttempts;

    @NotNull
    private Integer retryMaxInterval;

}
