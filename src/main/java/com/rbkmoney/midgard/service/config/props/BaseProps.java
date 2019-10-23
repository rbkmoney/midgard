package com.rbkmoney.midgard.service.config.props;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public abstract class BaseProps {

    private Resource url;
    private int networkTimeout;

}
