package com.rbkmoney.midgard.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "clearing-service.adapters")
public class AdaptersProperties  {

    private final List<AdapterProperties> adapterPropertiesList = new ArrayList<>();

    @Data
    public static class AdapterProperties extends BaseProperties {

        private String name;
        private int providerId;

    }
}
