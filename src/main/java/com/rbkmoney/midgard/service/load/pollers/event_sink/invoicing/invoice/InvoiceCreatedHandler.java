package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.invoice;

import com.fasterxml.jackson.databind.JsonNode;
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
import com.rbkmoney.midgard.service.load.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.InvoiceStatus;
import org.jooq.generated.feed.tables.pojos.Invoice;
import org.jooq.generated.feed.tables.pojos.InvoiceCart;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceCreatedHandler extends AbstractInvoicingHandler {

    private final InvoiceDao invoiceDao;

    private final InvoiceCartDao invoiceCartDao;

    private final Filter filter= new PathConditionFilter(new PathConditionRule("invoice_created",
            new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) throws DaoException {
        com.rbkmoney.damsel.domain.Invoice invoice = invoiceChange.getInvoiceCreated().getInvoice();
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();

        log.info("Start invoice created handling, sequenceId={}, invoiceId={}, partyId={}, shopId={}",
                sequenceId, invoiceId, invoice.getOwnerId(), invoice.getShopId());

        Invoice invoiceRecord = new Invoice();
        invoiceRecord.setSequenceId(sequenceId);
        invoiceRecord.setChangeId(changeId);
        invoiceRecord.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        invoiceRecord.setInvoiceId(invoiceId);
        invoiceRecord.setPartyId(invoice.getOwnerId());
        invoiceRecord.setShopId(invoice.getShopId());
        invoiceRecord.setPartyRevision(invoice.getPartyRevision());
        invoiceRecord.setCreatedAt(TypeUtil.stringToLocalDateTime(invoice.getCreatedAt()));
        InvoiceStatus status = TBaseUtil.unionFieldToEnum(invoice.getStatus(), InvoiceStatus.class);
        invoiceRecord.setStatus(status);
        if (invoice.getStatus().isSetCancelled()) {
            invoiceRecord.setStatusCancelledDetails(invoice.getStatus().getCancelled().getDetails());
        } else if (invoice.getStatus().isSetFulfilled()) {
            invoiceRecord.setStatusFulfilledDetails(invoice.getStatus().getFulfilled().getDetails());
        }
        invoiceRecord.setDetailsProduct(invoice.getDetails().getProduct());
        invoiceRecord.setDetailsDescription(invoice.getDetails().getDescription());
        invoiceRecord.setDue(TypeUtil.stringToLocalDateTime(invoice.getDue()));
        invoiceRecord.setAmount(invoice.getCost().getAmount());
        invoiceRecord.setCurrencyCode(invoice.getCost().getCurrency().getSymbolicCode());
        invoiceRecord.setContext(invoice.getContext().getData());
        invoiceRecord.setTemplateId(invoice.getTemplateId());

        long invId = invoiceDao.save(invoiceRecord);
        if (invoice.getDetails().isSetCart()) {
            List<InvoiceCart> invoiceCarts = invoice.getDetails().getCart().getLines().stream().map(il -> {
                InvoiceCart ic = new InvoiceCart();
                ic.setInvId(invId);
                ic.setProduct(il.getProduct());
                ic.setQuantity(il.getQuantity());
                ic.setAmount(il.getPrice().getAmount());
                ic.setCurrencyCode(il.getPrice().getCurrency().getSymbolicCode());
                Map<String, JsonNode> jsonNodeMap = il.getMetadata().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> JsonUtil.tBaseToJsonNode(e.getValue())));
                ic.setMetadataJson(JsonUtil.objectToJsonString(jsonNodeMap));
                return ic;
            }).collect(Collectors.toList());
            invoiceCartDao.save(invoiceCarts);
        }

        log.info("Invoice has been saved, sequenceId={}, invoiceId={}, partyId={}, shopId={}",
                sequenceId, invoiceId, invoice.getOwnerId(), invoice.getShopId());
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }

}
