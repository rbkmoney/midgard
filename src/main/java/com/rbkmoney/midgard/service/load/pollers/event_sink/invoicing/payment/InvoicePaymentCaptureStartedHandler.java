package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentCaptureParams;

import com.rbkmoney.damsel.payment_processing.InvoicePaymentCaptureStarted;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.InvoiceCartDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.PaymentDao;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import com.rbkmoney.midgard.service.load.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.InvoiceCart;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentCaptureStartedHandler extends AbstractInvoicingHandler {

    private final CashFlowDao cashFlowDao;

    private final PaymentDao paymentDao;

    private final InvoiceCartDao invoiceCartDao;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_capture_started",
            new IsNullCondition().not()));


    @Override
    public void handle(InvoiceChange change, SimpleEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        InvoicePaymentCaptureStarted invoicePaymentCaptureStarted = change.getInvoicePaymentChange()
                .getPayload()
                .getInvoicePaymentCaptureStarted();

        long sequenceId = event.getEventId();
        String paymentId = invoicePaymentChange.getId();
        String invoiceId = event.getSourceId();

        InvoicePaymentCaptureParams invoicePaymentCaptureStartedParams = invoicePaymentCaptureStarted.getParams();

        log.info("Start payment capture started handling, sequenceId={}, invoiceId={}, paymentId={}",
                sequenceId, invoiceId, paymentId);
        Payment paymentSource = paymentDao.get(invoiceId, paymentId);
        if (paymentSource == null) {
            log.error("Payment not found (sequenceId='{}', invoiceId='{}', paymentId='{}'",
                    sequenceId, invoiceId, paymentId);
            return;
        }
        Long paymentSourceId = paymentSource.getId();
        paymentSource.setId(null);
        paymentSource.setWtime(null);
        paymentSource.setChangeId(changeId);
        paymentSource.setSequenceId(sequenceId);
        paymentSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));

        if (invoicePaymentCaptureStartedParams.isSetCash()) {
            paymentSource.setAmount(invoicePaymentCaptureStartedParams.getCash().getAmount());
            paymentSource.setCurrencyCode(invoicePaymentCaptureStartedParams.getCash().getCurrency().getSymbolicCode());
        }
        paymentSource.setStatusCapturedStartedReason(invoicePaymentCaptureStartedParams.getReason());

        Long pmntId = paymentDao.save(paymentSource);
        if (pmntId != null) {
            paymentDao.updateNotCurrent(paymentSourceId);
            saveCashFlow(paymentSourceId, pmntId, PaymentChangeType.payment);
        }

        if (invoicePaymentCaptureStartedParams.isSetCart()) {
            List<InvoiceCart> invoiceCarts = invoicePaymentCaptureStartedParams.getCart().getLines().stream()
                    .map(il -> {
                        InvoiceCart ic = new InvoiceCart();
                        ic.setInvId(Long.valueOf(paymentSource.getInvoiceId()));
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

        log.info("Payment has been saved, sequenceId={}, invoiceId={}, paymentId={}", sequenceId, invoiceId, paymentSource.getId());
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }

    public void saveCashFlow(Long objSourceId, Long objId, PaymentChangeType type) {
        if (objId != null) {
            List<CashFlow> cashFlows = cashFlowDao.getByObjId(objSourceId, type);
            cashFlows.forEach(pcf -> {
                pcf.setId(null);
                pcf.setObjId(objId);
            });
            cashFlowDao.save(cashFlows);
        }
    }

}