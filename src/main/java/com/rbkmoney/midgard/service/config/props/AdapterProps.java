package com.rbkmoney.midgard.service.config.props;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public abstract class AdapterProps extends BaseProps {

    private String name;
    private int providerId;

}
