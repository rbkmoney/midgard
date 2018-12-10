package com.rbkmoney.midgard.load.services;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.load.DAO.invoicing.iface.InvoiceDao;
import com.rbkmoney.midgard.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InvoicingService implements EventService<Event, EventPayload> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final InvoiceDao invoiceDao;

    private final List<AbstractInvoicingHandler> invoicingHandlers;

    public InvoicingService(InvoiceDao invoiceDao, List<AbstractInvoicingHandler> invoicingHandlers) {
        this.invoiceDao = invoiceDao;
        this.invoicingHandlers = invoicingHandlers;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(Event processingEvent, EventPayload payload) {
        if (payload.isSetInvoiceChanges()) {
            payload.getInvoiceChanges().forEach(cc -> invoicingHandlers.forEach(ph -> {
                if (ph.accept(cc)) {
                    ph.handle(cc, processingEvent);
                }
            }));
        }
    }

    @Override
    public Optional<Long> getLastEventId() throws DaoException {
        Optional<Long> lastEventId = Optional.ofNullable(invoiceDao.getLastEventId());
        log.info("Last invoicing eventId={}", lastEventId);
        return lastEventId;
    }
}
