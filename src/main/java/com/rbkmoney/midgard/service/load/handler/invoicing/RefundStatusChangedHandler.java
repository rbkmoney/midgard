package com.rbkmoney.midgard.service.load.handler.invoicing;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.domain.InvoicePaymentRefundStatus;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.DEFAULT_TRX_VERSION;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundStatusChangedHandler extends AbstractInvoicingHandler {

    private final ClearingRefundDao clearingRefundDao;

    private final InvoicingSrv.Iface invoicingService;

    private final UserInfo userInfo;

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
        if (invoicePaymentRefundStatus.isSetSucceeded()) {
            String invoiceId = event.getSourceId();
            String paymentId = invoiceChange.getInvoicePaymentChange().getId();
            String refundId = invoicePaymentRefundChange.getId();

            EventRange eventRange = new EventRange();
            eventRange.setAfter(0L);
            eventRange.setLimit(Integer.MAX_VALUE);
            Invoice invoice = invoicingService.get(userInfo, invoiceId, eventRange);
            InvoicePayment invoicePayment = invoice.getPayments().stream()
                    .filter(invPayment -> invPayment.getPayment().getId() == paymentId)
                    .findFirst()
                    .orElse(null);
            if (invoicePayment == null || invoicePayment.getPayment() == null) {
                throw new Exception("Payment " + paymentId + " for invoice " + invoiceId + " not found");
            }
            com.rbkmoney.damsel.domain.InvoicePayment payment = invoicePayment.getPayment();
            if (payment.getRoute() == null || payment.getRoute().getProvider() == null) {
                throw new RuntimeException("Provider ID for invoice " + invoiceId + " with payment id " +
                        paymentId + " not found!");
            }
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
        }

    }

    private static ClearingRefund transformRefund(InvoicePaymentRefund refund,
                                                  SimpleEvent event,
                                                  com.rbkmoney.damsel.domain.InvoicePayment payment,
                                                  Integer changeId) {
        ClearingRefund clearingRefund = new ClearingRefund();
        clearingRefund.setInvoiceId(event.getSourceId());
        clearingRefund.setPaymentId(payment.getId());
        clearingRefund.setRefundId(refund.getId());
        clearingRefund.setTransactionId(refund.getExternalId()); //TODO: тот ли это transactionID
        clearingRefund.setPartyId(payment.getOwnerId());
        clearingRefund.setShopId(payment.getShopId());
        clearingRefund.setCreatedAt(LocalDateTime.parse(refund.getCreatedAt()));
        Cash cash = refund.getCash();
        clearingRefund.setAmount(cash.getAmount());
        clearingRefund.setCurrencyCode(cash.getCurrency().getSymbolicCode());
        clearingRefund.setReason(refund.getReason());
        clearingRefund.setDomainRevision(refund.getDomainRevision());
        clearingRefund.setExtra(null); //TODO: where is extra?
        clearingRefund.setClearingState(TransactionClearingState.READY);
        clearingRefund.setTrxVersion(DEFAULT_TRX_VERSION);

        clearingRefund.setSequenceId(event.getSequenceId());
        clearingRefund.setChangeId(changeId);
        return clearingRefund;
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
