package com.rbkmoney.midgard.service.load.pollers.listeners;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.services.InvoicingService;
import com.rbkmoney.midgard.service.load.utils.MapperUtil;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvoicingKafkaListener {

    private final InvoicingService invoicingService;

    private final MachineEventParser<EventPayload> parser;

    @KafkaListener(topics = "${kafka.topics.invoice.id}", containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "${kafka.consumer.listener-startup}")
    public void handle(SinkEvent sinkEvent, Acknowledgment ack) {
        SimpleEvent event = MapperUtil.transformMachineEvent(sinkEvent.getEvent());
        log.debug("Reading sinkEvent, sourceId:{}, eventId:{}", event.getSourceId(), event.getEventId());
        EventPayload payload = parser.parse(sinkEvent.getEvent());
        if (payload.isSetInvoiceChanges()) {
            invoicingService.handleEvents(event, payload);
        }
        ack.acknowledge();
    }

}
