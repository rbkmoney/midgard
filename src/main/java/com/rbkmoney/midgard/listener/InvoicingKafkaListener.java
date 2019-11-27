package com.rbkmoney.midgard.listener;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.midgard.service.invoicing.InvoicingService;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@RequiredArgsConstructor
public class InvoicingKafkaListener {

    private final InvoicingService invoicingService;

    private final MachineEventParser<EventPayload> parser;

    @KafkaListener(topics = "${kafka.topics.invoice.id}", containerFactory = "kafkaListenerContainerFactory")
    public void handle(SinkEvent sinkEvent, Acknowledgment ack) {
        MachineEvent machineEvent = sinkEvent.getEvent();
        EventPayload payload = parser.parse(machineEvent);
        if (payload.isSetInvoiceChanges()) {
            log.info("Reading sinkEvent from kafka (sourceId='{}', eventId='{}')", machineEvent.getSourceId(),
                    machineEvent.getEventId());
            invoicingService.handleEvents(machineEvent, payload);
        }
        ack.acknowledge();
    }

}
