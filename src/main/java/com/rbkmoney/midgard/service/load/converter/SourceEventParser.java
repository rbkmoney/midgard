package com.rbkmoney.midgard.service.load.converter;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.kafka.common.converter.BinaryConverter;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.service.load.exceptions.ParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceEventParser {

    private final BinaryConverter<EventPayload> converter;

    public EventPayload parseEvent(MachineEvent message) {
        try {
            byte[] bin = message.getData().getBin();
            return converter.convert(bin, EventPayload.class);
        } catch (Exception e) {
            log.error("Exception when parse message e: ", e);
            throw new ParseException();
        }
    }
}
