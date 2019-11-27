package com.rbkmoney.midgard.service.load.handler.invoicing;

import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.midgard.service.load.utils.MapperUtil.isExistProviderId;
import static com.rbkmoney.midgard.service.load.utils.MapperUtil.transformTransaction;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusChangedHandler extends AbstractInvoicingHandler {

    private final TransactionsDao transactionsDao;

    private final InvoicingSrv.Iface invoicingService;

    private final List<ClearingAdapter> adapters;

    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("invoice_payment_change.payload.invoice_payment_status_changed.status.captured",
                    new IsNullCondition().not()));

    @Override
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) throws Exception {
        String invoiceId = event.getSourceId();
        log.info("Processing payment with status 'capture' (invoiceId = '{}', sequenceId = '{}', " +
                "changeId = '{}')", invoiceId, event.getSequenceId(), changeId);
        String paymentId = invoiceChange.getInvoicePaymentChange().getId();
        Invoice invoice = invoicingService.get(USER_INFO, invoiceId, getEventRange((int) event.getSequenceId()));
        if (invoice == null || !invoice.isSetPayments()) {
            throw new NotFoundException(String.format("Invoice or payments not found! (invoice id '%s', " +
                    "sequenceId = '%d' and changeId = '%d')", invoiceId, event.getSequenceId(), changeId));
        }

        var payment = getPaymentById(invoice, paymentId);
        if (payment == null) {
            throw new NotFoundException(String.format("Payment with invoice id '%s' sequenceId = '%d' " +
                    "and changeId = '%d' not found!", invoiceId, event.getSequenceId(), changeId));
        }
        if (!payment.isSetRoute() || !payment.getRoute().isSetProvider()) {
            throw new NotFoundException(String.format("Route info for payment with invoice id '%s' sequenceId = '%d' " +
                    "and changeId = '%d' not found!", invoiceId, event.getSequenceId(), changeId));
        }
        if (!payment.isSetSessions()) {
            throw new NotFoundException(String.format("Sessions for payment with invoice id '%s' sequenceId = '%d' " +
                    "and changeId = '%d' not found!", invoiceId, event.getSequenceId(), changeId));
        }

        if (!isExistProviderId(adapters, payment.getRoute().getProvider().getId())) {
            return;
        }
        Long lastSourceRowId = transactionsDao.getLastTransaction().getSourceRowId();
        ClearingTransaction clearingTransaction =
                transformTransaction(payment, event, invoiceId, changeId, lastSourceRowId);
        Long trxSeqId = transactionsDao.save(clearingTransaction);
        if (trxSeqId == null) {
            log.info("Payment with status 'capture' (invoiceId = '{}', sequenceId = '{}', " +
                    "changeId = '{}') was was skipped (it already exist)", invoiceId, event.getSequenceId(), changeId);
        } else {
            log.info("Payment with status 'capture' (invoiceId = '{}', sequenceId = '{}', " +
                    "changeId = '{}') was processed", invoiceId, event.getSequenceId(), changeId);
        }

    }

    private InvoicePayment getPaymentById(Invoice invoice, String paymentId) {
        return invoice.getPayments().stream()
                .filter(pmnt -> paymentId.equals(pmnt.getPayment().getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }

}
