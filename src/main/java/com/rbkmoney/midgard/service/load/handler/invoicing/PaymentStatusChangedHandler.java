package com.rbkmoney.midgard.service.load.handler.invoicing;

import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.midgard.service.load.utils.MapperUtil.checkRouteInfo;
import static com.rbkmoney.midgard.service.load.utils.MapperUtil.transformTransaction;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusChangedHandler extends AbstractInvoicingHandler {

    private final TransactionsDao transactionsDao;

    private final InvoicingSrv.Iface invoicingService;

    private final List<ClearingAdapter> adapters;

    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("invoice_payment_change.payload.invoice_payment_status_changed",
                    new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) throws Exception {
        InvoicePaymentStatus invoicePaymentStatus =
                invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus();
        String invoiceId = event.getSourceId();
        log.info("Processing payment with status 'capture' (invoiceId = '{}', sequenceId = '{}', " +
                "changeId = '{}')", invoiceId, event.getSequenceId(), changeId);
        if (invoicePaymentStatus.isSetCaptured()) {
            String paymentId = invoiceChange.getInvoicePaymentChange().getId();
            Invoice invoice = invoicingService.get(USER_INFO, invoiceId, getEventRange((int) event.getSequenceId()));


            var payment = getPaymentById(invoice, paymentId);
            if (payment == null) {
                throw new NotFoundException(String.format("Payment %s for invoice %s not found", paymentId, invoiceId));
            }
            checkRouteInfo(payment, invoiceId, paymentId);

            int providerId = payment.getRoute().getProvider().getId();
            List<Integer> proveidersIds = adapters.stream()
                    .map(ClearingAdapter::getAdapterId)
                    .collect(Collectors.toList());
            if (!proveidersIds.contains(providerId)) {
                return;
            }
            ClearingTransaction clearingTransaction = transformTransaction(payment, event, invoiceId, changeId);
            transactionsDao.save(clearingTransaction);
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

    @Override
    public boolean accept(InvoiceChange change) {
        return getFilter().match(change) &&
                !change.getInvoicePaymentChange()
                        .getPayload()
                        .getInvoicePaymentStatusChanged()
                        .getStatus()
                        .isSetRefunded();
    }

}
