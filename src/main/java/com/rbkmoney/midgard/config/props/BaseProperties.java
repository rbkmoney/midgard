package com.rbkmoney.midgard.config.props;

import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Validated
public class BaseProperties {

    @NotEmpty
    private Resource url;

    @NotNull
    private int networkTimeout;

}
