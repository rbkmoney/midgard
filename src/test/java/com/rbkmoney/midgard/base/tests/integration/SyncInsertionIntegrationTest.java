package com.rbkmoney.midgard.base.tests.integration;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoiceCreated;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SyncInsertionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AbstractInvoicingHandler invoiceCreatedHandler;

    @Test
    public void syncInsertionIntegrationTest() throws ExecutionException, InterruptedException {
        InvoiceChange invoiceChange = getTestInvoiceChange();
        SimpleEvent event = getTestSimpleEvent();
        Integer changeId = 1;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> futureOne = executor.submit(() -> invoiceCreatedHandler.handle(invoiceChange, event, changeId));
        Future<?> futureTwo = executor.submit(() -> invoiceCreatedHandler.handle(invoiceChange, event, changeId));
        futureTwo.get();

    }

    private static InvoiceChange getTestInvoiceChange() {
        InvoiceChange invoiceChange = new InvoiceChange();
        InvoiceCreated invoiceCreated = new InvoiceCreated();
        Invoice invoice = new Invoice();
        invoice.setOwnerId("ownerId_1");
        invoice.setShopId("shopId_1");
        invoice.setPartyRevision(123L);
        invoice.setCreatedAt(Instant.now().toString());
        invoice.setStatus(InvoiceStatus.paid(new InvoicePaid()));

        InvoiceDetails details = new InvoiceDetails();
        details.setProduct("Product_1");
        details.setDescription("Desc_1");
        invoice.setDetails(details);
        invoice.setDue(Instant.now().toString());

        Cash cost = new Cash();
        cost.setAmount(1000L);
        CurrencyRef currency = new CurrencyRef();
        currency.setSymbolicCode("RUB");
        cost.setCurrency(currency);
        invoice.setCost(cost);
        invoice.setContext(new Content());

        invoiceCreated.setInvoice(invoice);
        invoiceChange.setInvoiceCreated(invoiceCreated);

        return invoiceChange;
    }

    private static SimpleEvent getTestSimpleEvent() {
        return SimpleEvent.builder()
                .eventId(123)
                .sequenceId(123)
                .sourceId("123")
                .createdAt(Instant.now().toString())
                .build();
    }

}
