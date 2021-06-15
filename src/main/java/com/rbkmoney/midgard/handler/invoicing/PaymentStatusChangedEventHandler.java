package com.rbkmoney.midgard.handler.invoicing;

import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingTransaction;
import com.rbkmoney.midgard.exception.NotFoundException;
import com.rbkmoney.midgard.service.check.OperationCheckingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.midgard.utils.MappingUtils.isExistProviderId;
import static com.rbkmoney.midgard.utils.MappingUtils.transformTransaction;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusChangedEventHandler extends AbstractInvoicingEventHandler {

    private final TransactionsDao transactionsDao;

    private final InvoicingSrv.Iface invoicingService;

    private final List<ClearingAdapter> adapters;

    private final OperationCheckingService operationCheckingService;

    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("invoice_payment_change.payload.invoice_payment_status_changed.status.captured",
                    new IsNullCondition().not()));

    @Override
    public void handle(InvoiceChange invoiceChange, MachineEvent event, Integer changeId) throws Exception {
        String invoiceId = event.getSourceId();
        long sequenceId = event.getEventId();
        log.info("Processing payment with status 'capture' (invoiceId = '{}', sequenceId = '{}', " +
                "changeId = '{}')", invoiceId, sequenceId, changeId);
        String paymentId = invoiceChange.getInvoicePaymentChange().getId();
        Invoice invoice = invoicingService.get(USER_INFO, invoiceId, getEventRange((int) sequenceId));
        if (invoice == null || !invoice.isSetPayments()) {
            throw new NotFoundException(String.format("Invoice or payments not found! (invoice id '%s', " +
                    "sequenceId = '%d' and changeId = '%d')", invoiceId, sequenceId, changeId));
        }

        var payment = getPaymentById(invoice, paymentId);
        if (payment == null) {
            throw new NotFoundException(String.format("Payment with invoice id '%s' sequenceId = '%d' " +
                    "and changeId = '%d' not found!", invoiceId, sequenceId, changeId));
        }
        if (!payment.isSetRoute() || !payment.getRoute().isSetProvider()) {
            throw new NotFoundException(String.format("Route info for payment with invoice id '%s' sequenceId = '%d' " +
                    "and changeId = '%d' not found!", invoiceId, sequenceId, changeId));
        }
        if (!payment.isSetSessions()) {
            throw new NotFoundException(String.format("Sessions for payment with invoice id '%s' sequenceId = '%d' " +
                    "and changeId = '%d' not found!", invoiceId, sequenceId, changeId));
        }

        if (!isExistProviderId(adapters, payment.getRoute().getProvider().getId())) {
            return;
        }

        try {
            if (operationCheckingService.isOperationForSkip(payment)) {
                return;
            }

            ClearingTransaction clearingTransaction = transformTransaction(payment, event, invoiceId, changeId);
            Long trxSeqId = transactionsDao.save(clearingTransaction);
            if (trxSeqId == null) {
                log.info("Payment with status 'capture' (invoiceId = '{}', sequenceId = '{}', " +
                        "changeId = '{}') was was skipped (it already exist)", invoiceId, sequenceId, changeId);
            } else {
                log.info("Payment with status 'capture' (invoiceId = '{}', sequenceId = '{}', " +
                        "changeId = '{}') was processed", invoiceId, sequenceId, changeId);
            }
        } catch (NotFoundException ex) {
            throw new NotFoundException(String.format("%s invoice id '%s', sequence id '%d' and change id '%d'",
                    ex.getMessage(), invoiceId, sequenceId, changeId));
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
