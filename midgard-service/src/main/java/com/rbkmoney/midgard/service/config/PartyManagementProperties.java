package com.rbkmoney.midgard.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="bm.partymanagement")
public class PartyManagementProperties extends PoolingEventProperties {

}
