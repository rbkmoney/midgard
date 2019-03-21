package com.rbkmoney.midgard.service.load.services;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.InvoiceDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class InvoicingService implements EventService<Event, EventPayload> {

    private final InvoiceDao invoiceDao;

    private final List<AbstractInvoicingHandler> invoicingHandlers;

    public InvoicingService(InvoiceDao invoiceDao,
                            List<AbstractInvoicingHandler> invoicingHandlers) {
        this.invoiceDao = invoiceDao;
        this.invoicingHandlers = invoicingHandlers;
    }

    @Value("${import.init-last-event-id}")
    private long initLastEventId;

    @Override
    @Transactional
    public void handleEvents(Event processingEvent, EventPayload payload) {
        if (payload.isSetInvoiceChanges()) {
            payload.getInvoiceChanges().forEach(cc -> invoicingHandlers.forEach(ph -> {
                if (ph.accept(cc)) {
                    ph.handle(cc, processingEvent);
                }
            }));
        }
    }

    public Optional<Long> getLastEventId(int div, int mod) throws DaoException {
        Optional<Long> lastEventId = Optional.ofNullable(invoiceDao.getLastEventId(div, mod));
        log.info("Last invoicing eventId={}", lastEventId);
        if (lastEventId.isPresent()) {
            return lastEventId;
        } else {
            log.debug("Last invoicing eventId will set to {}", initLastEventId);
            return Optional.of(initLastEventId);
        }
    }

    @Override
    public Optional<Long> getLastEventId() {
        throw new RuntimeException("No longer supported");
    }

}
