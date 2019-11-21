package com.rbkmoney.midgard.service.load.handler.invoicing;

import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.domain.InvoicePaymentRefundStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.midgard.service.load.utils.MapperUtil.checkRouteInfo;
import static com.rbkmoney.midgard.service.load.utils.MapperUtil.transformRefund;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundStatusChangedHandler extends AbstractInvoicingHandler {

    private final ClearingRefundDao clearingRefundDao;

    private final InvoicingSrv.Iface invoicingService;

    private final List<ClearingAdapter> adapters;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_refund_change" +
                    ".payload.invoice_payment_refund_status_changed",
            new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) throws Exception {
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        InvoicePaymentRefundChange invoicePaymentRefundChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentRefundChange();
        InvoicePaymentRefundStatus invoicePaymentRefundStatus =
                invoicePaymentRefundChange.getPayload().getInvoicePaymentRefundStatusChanged().getStatus();
        String invoiceId = event.getSourceId();

        log.info("Processing refund with status 'succeeded' (invoiceId = '{}', sequenceId = '{}', " +
                "changeId = '{}')", invoiceId, event.getSequenceId(), changeId);
        if (invoicePaymentRefundStatus.isSetSucceeded()) {
            String paymentId = invoiceChange.getInvoicePaymentChange().getId();
            String refundId = invoicePaymentRefundChange.getId();

            Invoice invoice = invoicingService.get(USER_INFO, invoiceId, getEventRange((int) event.getSequenceId()));
            InvoicePayment invoicePayment = invoice.getPayments().stream()
                    .filter(invPayment -> paymentId.equals(invPayment.getPayment().getId()))
                    .findFirst()
                    .orElse(null);
            if (invoicePayment == null || invoicePayment.getPayment() == null) {
                throw new NotFoundException("Payment " + paymentId + " for invoice " + invoiceId + " not found");
            }
            com.rbkmoney.damsel.domain.InvoicePayment payment = invoicePayment.getPayment();
            checkRouteInfo(payment, invoiceId, paymentId);

            int providerId = payment.getRoute().getProvider().getId();
            List<Integer> proveidersIds = adapters.stream()
                    .map(ClearingAdapter::getAdapterId)
                    .collect(Collectors.toList());
            if (!proveidersIds.contains(providerId)) {
                return;
            }

            InvoicePaymentRefund refund = invoicePayment.getRefunds().stream()
                    .filter(hgRefund -> refundId.equals(hgRefund.getId()))
                    .findFirst()
                    .orElse(null);
            if (refund == null) {
                throw new Exception("Refund " + refundId +" with payment id " + paymentId + " and invoice id " +
                        invoiceId + " not found");
            }
            ClearingRefund clearingRefund = transformRefund(refund, event, payment, changeId);
            clearingRefundDao.save(clearingRefund);
            log.info("Refund with status 'succeeded' (invoiceId = '{}', sequenceId = '{}', " +
                    "changeId = '{}') was processed", invoiceId, event.getSequenceId(), changeId);
        }
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }

}
