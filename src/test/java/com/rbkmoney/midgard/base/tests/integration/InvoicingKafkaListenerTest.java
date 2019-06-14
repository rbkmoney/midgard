package com.rbkmoney.midgard.base.tests.integration;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.kafka.common.serializer.ThriftSerializer;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.midgard.base.tests.integration.AbstractIntegrationTest;
import com.rbkmoney.midgard.service.load.converter.SourceEventParser;
import com.rbkmoney.midgard.service.load.services.InvoicingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;

@Slf4j
public class InvoicingKafkaListenerTest extends AbstractIntegrationTest {

    @org.springframework.beans.factory.annotation.Value("${kafka.topics.invoicing}")
    public String topic;

    @MockBean
    InvoicingService invoicingService;

    @MockBean
    SourceEventParser eventParser;

    @Before
    public void init() throws IOException, SQLException {
        initDb();
    }

    @Test
    public void listenEmptyChanges() throws InterruptedException {
        Mockito.when(eventParser.parseEvent(any())).thenReturn(EventPayload.invoice_changes(emptyList()));

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(createMessage());

        writeToTopic(sinkEvent);

        waitForTopicSync();

        Mockito.verify(eventParser, Mockito.times(1)).parseEvent(any());
        Mockito.verify(invoicingService, Mockito.times(1)).handleEvents(any(), any());
    }

    private void writeToTopic(SinkEvent sinkEvent) {
        Producer<String, SinkEvent> producer = createProducer();
        ProducerRecord<String, SinkEvent> producerRecord = new ProducerRecord<>(topic, null, sinkEvent);
        try {
            producer.send(producerRecord).get();
        } catch (Exception e) {
            log.error("KafkaAbstractTest initialize e: ", e);
        }
        producer.close();
    }

    private void waitForTopicSync() throws InterruptedException {
        Thread.sleep(1000L);
    }


    private MachineEvent createMessage() {
        MachineEvent message = new MachineEvent();
        Value data = new Value();
        data.setBin(new byte[0]);
        message.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        message.setEventId(1L);
        message.setSourceNs(SOURCE_NS);
        message.setSourceId(SOURCE_ID);
        message.setData(data);
        return message;
    }

    public static Producer<String, SinkEvent> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "client_id");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new ThriftSerializer<SinkEvent>().getClass());
        return new KafkaProducer<>(props);
    }

}
