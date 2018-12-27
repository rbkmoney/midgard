package com.rbkmoney.midgard.base.load.services;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.load.dao.invoicing.iface.InvoiceDao;
import com.rbkmoney.midgard.base.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class InvoicingService implements EventService<Event, EventPayload> {

    private final InvoiceDao invoiceDao;

    private final List<AbstractInvoicingHandler> invoicingHandlers;

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
