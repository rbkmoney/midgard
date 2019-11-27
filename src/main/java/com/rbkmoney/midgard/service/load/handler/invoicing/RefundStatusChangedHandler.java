package com.rbkmoney.midgard.service.load.handler.invoicing;

import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.midgard.service.load.utils.MapperUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundStatusChangedHandler extends AbstractInvoicingHandler {

    private final ClearingRefundDao clearingRefundDao;

    private final InvoicingSrv.Iface invoicingService;

    private final List<ClearingAdapter> adapters;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_refund_change" +
                    ".payload.invoice_payment_refund_status_changed.status.succeeded",
            new IsNullCondition().not()));

    @Override
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) throws Exception {
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        InvoicePaymentRefundChange invoicePaymentRefundChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentRefundChange();
        String invoiceId = event.getSourceId();

        log.info("Processing refund with status 'succeeded' (invoiceId = '{}', sequenceId = '{}', " +
                "changeId = '{}')", invoiceId, event.getSequenceId(), changeId);
        String paymentId = invoiceChange.getInvoicePaymentChange().getId();
        String refundId = invoicePaymentRefundChange.getId();
        long sequenceId = event.getSequenceId();

        Invoice invoice = invoicingService.get(USER_INFO, invoiceId, getEventRange((int) sequenceId));
        if (invoice == null || !invoice.isSetPayments()) {
            throw new NotFoundException(String.format("Invoice or payments not found! (invoice id '%s', " +
                    "sequenceId = '%d' and changeId = '%d')", invoiceId, sequenceId, changeId));
        }

        InvoicePayment invoicePayment = invoice.getPayments().stream()
                .filter(invPayment -> paymentId.equals(invPayment.getPayment().getId()))
                .findFirst()
                .orElse(null);
        if (invoicePayment == null || !invoicePayment.isSetPayment()) {
            throw new NotFoundException(String.format("Payment for invoice (invoice id '%s', " +
                    "sequenceId = '%d' and changeId = '%d') not found!", invoiceId, sequenceId, changeId));
        }
        if (!invoicePayment.isSetRoute()
                || !invoicePayment.getRoute().isSetProvider()
                || !invoicePayment.isSetSessions()) {
            throw new NotFoundException(String.format("Route or session info for payment with invoice id '%s', " +
                    "sequenceId = '%d' and changeId = '%d' not found!", invoiceId, sequenceId, changeId));
        }
        if (!isExistProviderId(adapters, invoicePayment.getRoute().getProvider().getId())) {
            return;
        }
        if (!invoicePayment.isSetRefunds()) {
            throw new NotFoundException(String.format("Refunds for invoice not found! (invoice id '%s', " +
                    "sequenceId = '%d' and changeId = '%d')", invoiceId, sequenceId, changeId));
        }
        InvoicePaymentRefund refund = invoicePayment.getRefunds().stream()
                .filter(hgRefund -> refundId.equals(hgRefund.getRefund().getId()))
                .findFirst()
                .orElse(null);

        if (refund == null || !refund.isSetSessions()) {
            throw new NotFoundException(String.format("InvoicePaymentRefund or sessions for refund " +
                    "(invoice id '%s', sequence id '%d', change id '%d') not found!", invoiceId, sequenceId, changeId));
        }

        ClearingRefund clearingRefund = transformRefund(refund, event, invoicePayment.getPayment(), changeId);
        Long refundSeqId = clearingRefundDao.save(clearingRefund);
        if (refundSeqId == null) {
            log.info("Refund with status 'succeeded' (invoiceId = '{}', sequenceId = '{}', " +
                    "changeId = '{}') was skipped (it already exist)", invoiceId, sequenceId, changeId);
        } else {
            log.info("Refund with status 'succeeded' (invoiceId = '{}', sequenceId = '{}', " +
                    "changeId = '{}') was processed", invoiceId, sequenceId, changeId);
        }

    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }

}
