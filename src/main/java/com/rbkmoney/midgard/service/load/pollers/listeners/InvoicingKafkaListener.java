package com.rbkmoney.midgard.service.load.pollers.listeners;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.midgard.service.load.converter.SourceEventParser;
import com.rbkmoney.midgard.service.load.services.InvoicingService;
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
    private final SourceEventParser sourceEventParser;

    @KafkaListener(topics = "${kafka.topics.invoicing}", containerFactory = "kafkaListenerContainerFactory")
    public void handle(SinkEvent sinkEvent, Acknowledgment ack) {
        log.debug("Reading sinkEvent, sourceId:{}, eventId:{}", sinkEvent.getEvent().getSourceId(), sinkEvent.getEvent().getEventId());
        EventPayload payload = sourceEventParser.parseEvent(sinkEvent.getEvent());
        if (payload.isSetInvoiceChanges()) {
            invoicingService.handleEvents(sinkEvent.getEvent(), payload);
        }
        ack.acknowledge();
    }
}
