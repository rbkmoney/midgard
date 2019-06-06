package com.rbkmoney.midgard.service.load.utils;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;

public final class MapperUtil {

    public static SimpleEvent transformMachineEvent(MachineEvent event) {
        return SimpleEvent.builder()
                .eventId(event.getEventId())
                .sourceId(event.getSourceId())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public static SimpleEvent transformSinkEvent(Event event) {
        return SimpleEvent.builder()
                .eventId(event.getId())
                .sourceId(event.getSource().getInvoiceId())
                .createdAt(event.getCreatedAt())
                .build();
    }

    private MapperUtil() { }

}
