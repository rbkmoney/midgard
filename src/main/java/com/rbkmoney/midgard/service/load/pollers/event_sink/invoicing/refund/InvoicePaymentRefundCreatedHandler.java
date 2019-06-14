package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.refund;

import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundCreated;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.PaymentDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.RefundDao;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import com.rbkmoney.midgard.service.load.utils.CashFlowUtil;
import com.rbkmoney.midgard.service.load.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.enums.RefundStatus;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentRefundCreatedHandler extends AbstractInvoicingHandler {

    private final RefundDao refundDao;

    private final PaymentDao paymentDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_created",
            new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();
        long sequenceId = event.getSequenceId();
        String invoiceId = event.getSourceId();

        InvoicePaymentRefundChange invoicePaymentRefundChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentRefundChange();
        InvoicePaymentRefundCreated invoicePaymentRefundCreated = invoicePaymentRefundChange.getPayload()
                .getInvoicePaymentRefundCreated();

        InvoicePaymentRefund invoicePaymentRefund = invoicePaymentRefundCreated.getRefund();

        String refundId = invoicePaymentRefund.getId();
        log.info("Start refund created handling, sequenceId={}, invoiceId={}, paymentId={}, refundId={}",
                sequenceId, invoiceId, paymentId, refundId);

        Refund refund = new Refund();
        refund.setChangeId(changeId);
        refund.setSequenceId(sequenceId);
        refund.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        refund.setDomainRevision(invoicePaymentRefund.getDomainRevision());
        refund.setRefundId(refundId);
        refund.setPaymentId(paymentId);
        refund.setInvoiceId(invoiceId);
        refund.setEventId(event.getEventId());

        Payment payment = paymentDao.get(invoiceId, paymentId);
        if (payment == null) {
            // TODO: исправить после того как прольется БД
            log.error("Payment on refund not found, sequenceId={}, invoiceId='{}', " +
                    "paymentId='{}', refundId='{}'", sequenceId, invoiceId, paymentId, refundId);
            return;
            //throw new NotFoundException(String.format("Payment on refund not found, invoiceId='%s', " +
            //                "paymentId='%s', refundId='%s'", invoiceId, paymentId, refundId));
        }

        refund.setPartyId(payment.getPartyId());
        refund.setShopId(payment.getShopId());
        refund.setCreatedAt(TypeUtil.stringToLocalDateTime(invoicePaymentRefund.getCreatedAt()));
        refund.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentRefund.getStatus(), RefundStatus.class));
        if (invoicePaymentRefund.getStatus().isSetFailed()) {
            refund.setStatusFailedFailure(JsonUtil.tBaseToJsonString(invoicePaymentRefund.getStatus().getFailed()));
        }

        if (invoicePaymentRefund.isSetCash()) {
            refund.setAmount(invoicePaymentRefund.getCash().getAmount());
            refund.setCurrencyCode(invoicePaymentRefund.getCash().getCurrency().getSymbolicCode());
        } else {
            refund.setAmount(payment.getAmount());
            refund.setCurrencyCode(payment.getCurrencyCode());
        }
        refund.setReason(invoicePaymentRefund.getReason());
        if (invoicePaymentRefund.isSetPartyRevision()) {
            refund.setPartyRevision(invoicePaymentRefund.getPartyRevision());
        }

        Long rfndId = refundDao.save(refund);
        if (rfndId == null) {
            log.info("Refund with sequenceId='{}', invoiceId='{}' and changeId='{}' already processed",
                    sequenceId, invoiceId, changeId);
        } else {
            List<CashFlow> cashFlowList = CashFlowUtil.convertCashFlows(invoicePaymentRefundCreated.getCashFlow(),
                    rfndId, PaymentChangeType.refund);
            cashFlowDao.save(cashFlowList);
            refundDao.updateCommissions(rfndId);

            log.info("Refund has been saved, sequenceId={}, invoiceId={}, paymentId={}, refundId={}",
                    sequenceId, invoiceId, paymentId, refundId);
        }
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
