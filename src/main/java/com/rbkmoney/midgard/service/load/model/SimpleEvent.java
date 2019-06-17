package com.rbkmoney.midgard.service.load.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class SimpleEvent {

    private long eventId;
    private long sequenceId;
    private String sourceId;
    private String createdAt;

}
