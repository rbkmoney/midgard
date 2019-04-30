package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.Handler;

public abstract class AbstractInvoicingHandler implements Handler<InvoiceChange, SimpleEvent> { }