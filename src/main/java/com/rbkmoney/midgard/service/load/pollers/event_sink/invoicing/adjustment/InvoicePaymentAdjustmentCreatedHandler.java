package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.adjustment;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustment;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.utils.CashFlowUtil;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.AdjustmentDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.PaymentDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.AdjustmentCashFlowType;
import org.jooq.generated.feed.enums.AdjustmentStatus;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.Adjustment;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class InvoicePaymentAdjustmentCreatedHandler extends AbstractInvoicingHandler {

    private final AdjustmentDao adjustmentDao;

    private final PaymentDao paymentDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter;

    @Autowired
    public InvoicePaymentAdjustmentCreatedHandler(AdjustmentDao adjustmentDao, PaymentDao paymentDao, CashFlowDao cashFlowDao) {
        this.adjustmentDao = adjustmentDao;
        this.paymentDao = paymentDao;
        this.cashFlowDao = cashFlowDao;
        this.filter = new PathConditionFilter(new PathConditionRule(
                "invoice_payment_change.payload.invoice_payment_adjustment_change.payload.invoice_payment_adjustment_created",
                new IsNullCondition().not()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(InvoiceChange invoiceChange, Event event) {
        long eventId = event.getId();
        String invoiceId = event.getSource().getInvoiceId();
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String paymentId = invoicePaymentChange.getId();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentAdjustmentChange();
        InvoicePaymentAdjustment invoicePaymentAdjustment = invoicePaymentAdjustmentChange
                .getPayload().getInvoicePaymentAdjustmentCreated().getAdjustment();
        String adjustmentId = invoicePaymentAdjustment.getId();

        log.info("Start adjustment created handling, eventId={}, invoiceId={}, paymentId={}, adjustmentId={}",
                eventId, invoiceId, paymentId, adjustmentId);

        Adjustment adjustment = new Adjustment();
        adjustment.setEventId(eventId);
        adjustment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        adjustment.setDomainRevision(invoicePaymentAdjustment.getDomainRevision());
        adjustment.setAdjustmentId(adjustmentId);
        adjustment.setPaymentId(paymentId);
        adjustment.setInvoiceId(invoiceId);
        Payment payment = paymentDao.get(invoiceId, paymentId);
        if (payment == null) {
            throw new NotFoundException(String.format("Payment on adjustment not found, invoiceId='%s', paymentId='%s', adjustmentId='%s'",
                    invoiceId, paymentId, adjustmentId));
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

        log.info("Adjustment has been saved, eventId={}, invoiceId={}, paymentId={}, adjustmentId={}",
                eventId, invoiceId, paymentId, adjustmentId);
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
