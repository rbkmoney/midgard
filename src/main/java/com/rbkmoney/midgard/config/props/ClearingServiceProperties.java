package com.rbkmoney.midgard.config.props;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "clearing-service")
public class ClearingServiceProperties {

    private List<AdapterProperties> adapters = new ArrayList<>();

    @NotEmpty
    private String serviceCallbackPath;

    private BaseProperties invoicingService;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AdapterProperties extends BaseProperties {

        private String name;

        private int providerId;

        private int packageSize;

        @NestedConfigurationProperty
        private SchedulerProperties scheduler;

    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "scheduler")
    public static class SchedulerProperties {

        @NotEmpty
        private boolean enabled;

        @NotEmpty
        private String jobId;

        @NotNull
        private Integer revisionId;

        @NotNull
        private Integer schedulerId;

        @NotNull
        private Integer calendarId;
    }

}
