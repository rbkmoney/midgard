package com.rbkmoney.midgard.scheduler.model;

import lombok.Data;

@Data
public class AdapterJobContext {

    private String name;

    private Integer providerId;

    private String url;

    private Integer networkTimeout;

}
