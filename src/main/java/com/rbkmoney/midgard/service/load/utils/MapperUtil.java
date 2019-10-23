package com.rbkmoney.midgard.service.load.utils;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapperUtil {

    private static final String KAFKA_SOURCE_NAME = "kafka";
    private static final String BUSTERMASE_SOURCE_NAME = "bustermase";

    public static SimpleEvent transformMachineEvent(MachineEvent event) {
        return SimpleEvent.builder()
                .sequenceId(event.getEventId())
                .sourceId(event.getSourceId())
                .createdAt(event.getCreatedAt())
                .eventSourceName(KAFKA_SOURCE_NAME)
                .build();
    }

    public static SimpleEvent transformSinkEvent(Event event) {
        return SimpleEvent.builder()
                .eventId(event.getId())
                .sequenceId(event.getSequence())
                .sourceId(event.getSource().getInvoiceId())
                .createdAt(event.getCreatedAt())
                .eventSourceName(BUSTERMASE_SOURCE_NAME)
                .build();
    }

}
