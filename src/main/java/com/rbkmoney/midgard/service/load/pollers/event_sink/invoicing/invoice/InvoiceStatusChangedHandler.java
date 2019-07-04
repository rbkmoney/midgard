package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.invoice;

import com.rbkmoney.damsel.domain.InvoiceStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.InvoiceCartDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.InvoiceDao;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Invoice;
import org.jooq.generated.feed.tables.pojos.InvoiceCart;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceStatusChangedHandler extends AbstractInvoicingHandler {

    private final InvoiceDao invoiceDao;

    private final InvoiceCartDao invoiceCartDao;

    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("invoice_status_changed", new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) throws DaoException {
        InvoiceStatus invoiceStatus = invoiceChange.getInvoiceStatusChanged().getStatus();
        long sequenceId = event.getSequenceId();
        String invoiceId = event.getSourceId();

        Invoice invoiceSource = invoiceDao.get(event.getSourceId());
        if (invoiceSource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Invoice not found, sequenceId={}, invoiceId='{}'", sequenceId, invoiceId);
            return;
            //throw new NotFoundException(String.format("Invoice not found, invoiceId='%s'", event.getSource().getInvoiceId()));
        }
        log.info("Start invoice status changed handling (invoiceId={}, partyId={}, shopId={}, sequenceId={}, status={})",
                invoiceId, invoiceSource.getPartyId(), invoiceSource.getShopId(), sequenceId,
                invoiceStatus.getSetField().getFieldName());

        Long invoiceSourceId = invoiceSource.getId();
        invoiceSource.setId(null);
        invoiceSource.setWtime(null);
        invoiceSource.setChangeId(changeId);
        invoiceSource.setSequenceId(sequenceId);
        invoiceSource.setEventId(event.getEventId());
        invoiceSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        invoiceSource.setStatus(TBaseUtil.unionFieldToEnum(invoiceStatus, org.jooq.generated.feed.enums.InvoiceStatus.class));
        if (invoiceStatus.isSetCancelled()) {
            invoiceSource.setStatusCancelledDetails(invoiceStatus.getCancelled().getDetails());
            invoiceSource.setStatusFulfilledDetails(null);
        } else if (invoiceStatus.isSetFulfilled()) {
            invoiceSource.setStatusCancelledDetails(null);
            invoiceSource.setStatusFulfilledDetails(invoiceStatus.getFulfilled().getDetails());
        }


        Long invId = invoiceDao.save(invoiceSource);
        if (invId == null) {
            log.info("Received duplicate key value when change invoice status with " +
                    "invoiceId='{}', changeId='{}', sequenceId='{}'", invoiceId, changeId, sequenceId);
        } else {
            invoiceDao.updateNotCurrent(invoiceSourceId);
            List<InvoiceCart> invoiceCartList = invoiceCartDao.getByInvId(invoiceSourceId);
            invoiceCartList.forEach(ic -> {
                ic.setId(null);
                ic.setInvId(invId);
            });
            invoiceCartDao.save(invoiceCartList);

            log.info("Invoice has been saved (invoiceId={}, partyId={}, shopId={}, sequenceId={}, status={})",
                    invoiceId, invoiceSource.getPartyId(), invoiceSource.getShopId(), sequenceId,
                    invoiceStatus.getSetField().getFieldName());
        }

    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
