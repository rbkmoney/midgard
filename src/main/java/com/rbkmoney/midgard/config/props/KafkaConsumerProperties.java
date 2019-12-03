package com.rbkmoney.midgard.config.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "kafka.consumer")
public class KafkaConsumerProperties {

    private String autoOffsetReset;
    private boolean enableAutoCommit;
    private String clientId;
    private String groupId;
    private Integer maxPollRecords;
    private Integer connectionsMaxIdleMs;
    private Integer sessionTimeoutMs;
    private Integer concurrency;

}