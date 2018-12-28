package com.rbkmoney.midgard.service.config;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public abstract class PoolingEventProperties {

    private Resource url;
    private Polling polling = new Polling();

    @Data
    public class Polling {
        private int delay;
        private int retryDelay;
        private int maxPoolSize;
        private int maxQuerySize;
    }

}
