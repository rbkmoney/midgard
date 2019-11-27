package com.rbkmoney.midgard.config.props;

import lombok.Data;

@Data
public abstract class AdapterProperties extends BaseProperties {

    private String name;
    private int providerId;

}
