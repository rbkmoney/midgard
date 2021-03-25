package com.rbkmoney.midgard.test.unit;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.midgard.exception.ParseException;
import com.rbkmoney.midgard.listener.InvoicingKafkaListener;
import com.rbkmoney.midgard.service.invoicing.InvoicingService;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

public class InvoicingListenerTest {

    @Mock
    private InvoicingService invoicingService;
    @Mock
    private MachineEventParser eventParser;
    @Mock
    private Acknowledgment ack;

    private InvoicingKafkaListener listener;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        listener = new InvoicingKafkaListener(invoicingService, eventParser);
    }

    @Test
    public void listenNonInvoiceChanges() {

        MachineEvent message = new MachineEvent();
        Event event = new Event();
        EventPayload payload = new EventPayload();
        payload.setCustomerChanges(List.of());
        event.setPayload(payload);
        Mockito.when(eventParser.parse(message)).thenReturn(payload);

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);

        listener.handle(sinkEvent, ack);

        Mockito.verify(invoicingService, Mockito.times(0)).handleEvents(any(), any());
        Mockito.verify(ack, Mockito.times(1)).acknowledge();
    }

    @Test(expected = ParseException.class)
    public void listenEmptyException() {
        MachineEvent message = new MachineEvent();

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);

        Mockito.when(eventParser.parse(message)).thenThrow(new ParseException());

        listener.handle(sinkEvent, ack);

        Mockito.verify(ack, Mockito.times(0)).acknowledge();
    }

    @Test
    public void listenChanges() {
        Event event = new Event();
        EventPayload payload = new EventPayload();
        ArrayList<InvoiceChange> invoiceChanges = new ArrayList<>();
        invoiceChanges.add(new InvoiceChange());
        payload.setInvoiceChanges(invoiceChanges);
        event.setPayload(payload);
        MachineEvent message = new MachineEvent();
        Mockito.when(eventParser.parse(message)).thenReturn(payload);

        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);

        listener.handle(sinkEvent, ack);

        Mockito.verify(invoicingService, Mockito.times(1)).handleEvents(any(), any());
        Mockito.verify(ack, Mockito.times(1)).acknowledge();
    }

}
