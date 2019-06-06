package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.adjustment;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustment;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.AdjustmentDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.PaymentDao;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import com.rbkmoney.midgard.service.load.utils.CashFlowUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.AdjustmentCashFlowType;
import org.jooq.generated.feed.enums.AdjustmentStatus;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.Adjustment;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentAdjustmentCreatedHandler extends AbstractInvoicingHandler {

    private final AdjustmentDao adjustmentDao;

    private final PaymentDao paymentDao;

    private final CashFlowDao cashFlowDao;

    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_adjustment_change" +
                    ".payload.invoice_payment_adjustment_created",
            new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentAdjustmentChange();
        InvoicePaymentAdjustment invoicePaymentAdjustment = invoicePaymentAdjustmentChange
                .getPayload().getInvoicePaymentAdjustmentCreated().getAdjustment();
        String adjustmentId = invoicePaymentAdjustment.getId();

        log.info("Start adjustment created handling, sequenceId={}, invoiceId={}, paymentId={}, adjustmentId={}",
                sequenceId, invoiceId, paymentId, adjustmentId);

        Adjustment adjustment = new Adjustment();
        adjustment.setSequenceId(sequenceId);
        adjustment.setChangeId(changeId);
        adjustment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        adjustment.setDomainRevision(invoicePaymentAdjustment.getDomainRevision());
        adjustment.setAdjustmentId(adjustmentId);
        adjustment.setInvoiceId(invoiceId);
        adjustment.setPaymentId(paymentId);
        Payment payment = paymentDao.get(invoiceId, paymentId);
        if (payment == null) {
            // TODO: исправить после того как прольется БД
            log.error("Payment on adjustment not found, invoiceId='{}', paymentId='{}', adjustmentId='{}'",
                    invoiceId, paymentId, adjustmentId);
            return;
            //throw new NotFoundException(String.format("Payment on adjustment not found, invoiceId='%s', paymentId='%s', adjustmentId='%s'",
            //        invoiceId, paymentId, adjustmentId));
        }
        adjustment.setPartyId(payment.getPartyId());
        adjustment.setShopId(payment.getShopId());
        adjustment.setCreatedAt(TypeUtil.stringToLocalDateTime(invoicePaymentAdjustment.getCreatedAt()));
        adjustment.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentAdjustment.getStatus(), AdjustmentStatus.class));
        if (invoicePaymentAdjustment.getStatus().isSetCaptured()) {
            adjustment.setStatusCapturedAt(TypeUtil.stringToLocalDateTime(invoicePaymentAdjustment.getStatus().getCaptured().getAt()));
        } else if (invoicePaymentAdjustment.getStatus().isSetCancelled()) {
            adjustment.setStatusCancelledAt(TypeUtil.stringToLocalDateTime(invoicePaymentAdjustment.getStatus().getCancelled().getAt()));
        }
        adjustment.setReason(invoicePaymentAdjustment.getReason());
        if (invoicePaymentAdjustment.isSetPartyRevision()) {
            adjustment.setPartyRevision(invoicePaymentAdjustment.getPartyRevision());
        }

        long adjId = adjustmentDao.save(adjustment);
        List<CashFlow> newCashFlowList = CashFlowUtil.convertCashFlows(invoicePaymentAdjustment.getNewCashFlow(),
                adjId, PaymentChangeType.adjustment, AdjustmentCashFlowType.new_cash_flow);
        cashFlowDao.save(newCashFlowList);
        List<CashFlow> oldCashFlowList = CashFlowUtil.convertCashFlows(invoicePaymentAdjustment.getOldCashFlowInverse(),
                adjId, PaymentChangeType.adjustment, AdjustmentCashFlowType.old_cash_flow_inverse);
        cashFlowDao.save(oldCashFlowList);
        adjustmentDao.updateCommissions(adjId);

        log.info("Adjustment has been saved, sequenceId={}, invoiceId={}, paymentId={}, adjustmentId={}",
                sequenceId, invoiceId, paymentId, adjustmentId);
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
