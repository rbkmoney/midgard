package com.rbkmoney.midgard.base.tests.unit;

import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.InvoiceDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.impl.InvoiceDaoImpl;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import com.rbkmoney.midgard.service.load.services.InvoicingService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.jdbc2.optional.SimpleDataSource;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InvoicingServiceTest {

    private static List<AbstractInvoicingHandler> wrongHandlers = new ArrayList<>();
    private static List<AbstractInvoicingHandler> rightHandlers = new ArrayList<>();
    private static InvoiceDao invoiceDao = new InvoiceDaoImpl(new SimpleDataSource());

    @BeforeClass
    public static void init() {
        AbstractInvoicingHandler wrong = mock(AbstractInvoicingHandler.class);
        when(wrong.accept(any())).thenReturn(false);
        wrongHandlers.add(wrong);

        AbstractInvoicingHandler right = mock(AbstractInvoicingHandler.class);
        when(right.accept(any())).thenReturn(true);
        rightHandlers.add(right);
    }

    @Test
    public void handleEmptyChanges() {
        InvoicingService invoicingService = new InvoicingService(rightHandlers, invoiceDao);

        SimpleEvent message = SimpleEvent.builder().build();
        EventPayload payload = new EventPayload();
        payload.setInvoiceChanges(List.of());

        invoicingService.handleEvents(message, payload);

        verify(rightHandlers.get(0), times(0)).accept(any());
    }

    @Test
    public void handlerSupportsInvoicing() {
        InvoicingService invoicingService = new InvoicingService(rightHandlers, invoiceDao);

        SimpleEvent message = SimpleEvent.builder().build();
        EventPayload payload = new EventPayload();
        payload.setInvoiceChanges(List.of(mock(InvoiceChange.class)));

        invoicingService.handleEvents(message, payload);

        verify(rightHandlers.get(0), times(1)).accept(any());
        verify(rightHandlers.get(0), times(1)).handle(any(), any(), any());
    }

    @Test
    public void handlerNotSupportInvoicing() {
        InvoicingService invoicingService = new InvoicingService(wrongHandlers, invoiceDao);

        SimpleEvent message = SimpleEvent.builder().build();
        EventPayload payload = new EventPayload();
        payload.setInvoiceChanges(List.of(mock(InvoiceChange.class)));

        invoicingService.handleEvents(message, payload);

        verify(wrongHandlers.get(0), times(1)).accept(any());
        verify(wrongHandlers.get(0), times(0)).handle(any(), any(), any());
    }

}
