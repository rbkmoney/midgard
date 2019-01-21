package com.rbkmoney.midgard.service.config.props;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public abstract class AdapterProps {

    private String name;
    private Resource url;
    private int networkTimeout;
    private int providerId;

}
