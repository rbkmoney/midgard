package com.rbkmoney.midgard.handler.invoicing;

import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundChange;
import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.dao.refund.ClearingRefundDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingRefund;
import com.rbkmoney.midgard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.midgard.utils.MappingUtils.isExistProviderId;
import static com.rbkmoney.midgard.utils.MappingUtils.transformRefund;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundStatusChangedEventHandler extends AbstractInvoicingEventHandler {

    private final ClearingRefundDao clearingRefundDao;

    private final InvoicingSrv.Iface invoicingService;

    private final List<ClearingAdapter> adapters;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_refund_change" +
                    ".payload.invoice_payment_refund_status_changed.status.succeeded",
            new IsNullCondition().not()));

    @Override
    public void handle(InvoiceChange invoiceChange, MachineEvent event, Integer changeId) throws Exception {
        String invoiceId = event.getSourceId();
        long sequenceId = event.getEventId();

        log.info("Processing refund with status 'succeeded' (invoiceId = '{}', sequenceId = '{}', " +
                "changeId = '{}')", invoiceId, sequenceId, changeId);
        String paymentId = invoiceChange.getInvoicePaymentChange().getId();

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

        int providerId = invoicePayment.getRoute().getProvider().getId();
        if (!isExistProviderId(adapters, providerId)) {
            return;
        }
        if (!invoicePayment.isSetRefunds()) {
            throw new NotFoundException(String.format("Refunds for invoice not found! (invoice id '%s', " +
                    "sequenceId = '%d' and changeId = '%d')", invoiceId, sequenceId, changeId));
        }

        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        InvoicePaymentRefundChange invoicePaymentRefundChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentRefundChange();
        String refundId = invoicePaymentRefundChange.getId();
        InvoicePaymentRefund refund = invoicePayment.getRefunds().stream()
                .filter(hgRefund -> refundId.equals(hgRefund.getRefund().getId()))
                .findFirst()
                .orElse(null);

        if (refund == null || !refund.isSetSessions()) {
            throw new NotFoundException(String.format("InvoicePaymentRefund or sessions for refund " +
                    "(invoice id '%s', sequence id '%d', change id '%d') not found!", invoiceId, sequenceId, changeId));
        }
        ClearingRefund clearingRefund =
                transformRefund(refund, event, invoicePayment.getPayment(), changeId, providerId);
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
