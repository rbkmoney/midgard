package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.invoice;

import com.rbkmoney.damsel.domain.InvoiceStatus;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.InvoiceCartDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.InvoiceDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Invoice;
import org.jooq.generated.feed.tables.pojos.InvoiceCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class InvoiceStatusChangedHandler extends AbstractInvoicingHandler {

    private final InvoiceDao invoiceDao;

    private final InvoiceCartDao invoiceCartDao;

    private final Filter filter;

    @Autowired
    public InvoiceStatusChangedHandler(InvoiceDao invoiceDao, InvoiceCartDao invoiceCartDao) {
        this.invoiceDao = invoiceDao;
        this.invoiceCartDao = invoiceCartDao;
        this.filter = new PathConditionFilter(new PathConditionRule("invoice_status_changed", new IsNullCondition().not()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(InvoiceChange invoiceChange, Event event) throws DaoException {
        InvoiceStatus invoiceStatus = invoiceChange.getInvoiceStatusChanged().getStatus();
        long eventId = event.getId();

        Invoice invoiceSource = invoiceDao.get(event.getSource().getInvoiceId());
        if (invoiceSource == null) {
            throw new NotFoundException(String.format("Invoice not found, invoiceId='%s'", event.getSource().getInvoiceId()));
        }
        log.info("Start invoice status changed handling, eventId={}, invoiceId={}, partyId={}, shopId={}, status={}",
                eventId, invoiceSource.getInvoiceId(), invoiceSource.getPartyId(), invoiceSource.getShopId(), invoiceStatus.getSetField().getFieldName());

        Long invoiceSourceId = invoiceSource.getId();
        invoiceSource.setId(null);
        invoiceSource.setWtime(null);
        invoiceSource.setEventId(eventId);
        invoiceSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        invoiceSource.setStatus(TBaseUtil.unionFieldToEnum(invoiceStatus, org.jooq.generated.feed.enums.InvoiceStatus.class));
        if (invoiceStatus.isSetCancelled()) {
            invoiceSource.setStatusCancelledDetails(invoiceStatus.getCancelled().getDetails());
            invoiceSource.setStatusFulfilledDetails(null);
        } else if (invoiceStatus.isSetFulfilled()) {
            invoiceSource.setStatusCancelledDetails(null);
            invoiceSource.setStatusFulfilledDetails(invoiceStatus.getFulfilled().getDetails());
        }

        invoiceDao.updateNotCurrent(invoiceSource.getInvoiceId());
        long invId = invoiceDao.save(invoiceSource);
        List<InvoiceCart> invoiceCartList = invoiceCartDao.getByInvId(invoiceSourceId);
        invoiceCartList.forEach(ic -> {
            ic.setId(null);
            ic.setInvId(invId);
        });
        invoiceCartDao.save(invoiceCartList);

        log.info("Invoice has been saved, eventId={}, invoiceId={}, partyId={}, shopId={}, status={}",
                eventId, invoiceSource.getInvoiceId(), invoiceSource.getPartyId(), invoiceSource.getShopId(), invoiceStatus.getSetField().getFieldName());
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
