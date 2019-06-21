package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.adjustment;

import com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentStatus;
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
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.AdjustmentCashFlowType;
import org.jooq.generated.feed.enums.AdjustmentStatus;
import org.jooq.generated.feed.tables.pojos.Adjustment;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentAdjustmentStatusChangedHandler extends AbstractInvoicingHandler {

    private final AdjustmentDao adjustmentDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_adjustment_change.payload" +
            ".invoice_payment_adjustment_status_changed",
                new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) {
        long sequenceId = event.getSequenceId();
        String invoiceId = event.getSourceId();
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String paymentId = invoiceChange.getInvoicePaymentChange().getId();
        InvoicePaymentAdjustmentChange invoicePaymentAdjustmentChange =
                invoicePaymentChange.getPayload().getInvoicePaymentAdjustmentChange();
        InvoicePaymentAdjustmentStatus invoicePaymentAdjustmentStatus =
                invoicePaymentAdjustmentChange.getPayload().getInvoicePaymentAdjustmentStatusChanged().getStatus();
        String adjustmentId = invoicePaymentAdjustmentChange.getId();

        log.info("Start adjustment status changed handling, sequenceId={}, invoiceId={}, paymentId={}, adjustmentId={}, status={}",
                sequenceId, invoiceId, paymentId, adjustmentId, invoicePaymentAdjustmentStatus.getSetField().getFieldName());
        Adjustment adjustmentSource = adjustmentDao.get(invoiceId, paymentId, adjustmentId);
        if (adjustmentSource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Adjustment not found (invoiceId='{}', paymentId='{}', adjustmentId='{}'б sequenceId='{}'",
                    invoiceId, paymentId, adjustmentId, sequenceId);
            return;
            //throw new NotFoundException(String.format("Adjustment not found, invoiceId='%s', paymentId='%s', adjustmentId='%s'",
            //        invoiceId, paymentId, adjustmentId));
        }
        Long adjustmentSourceId = adjustmentSource.getId();
        adjustmentSource.setId(null);
        adjustmentSource.setWtime(null);
        adjustmentSource.setChangeId(changeId);
        adjustmentSource.setSequenceId(sequenceId);
        adjustmentSource.setEventId(event.getEventId());
        adjustmentSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        adjustmentSource.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentAdjustmentStatus, AdjustmentStatus.class));
        if (invoicePaymentAdjustmentStatus.isSetCaptured()) {
            adjustmentSource.setStatusCapturedAt(TypeUtil.stringToLocalDateTime(invoicePaymentAdjustmentStatus.getCaptured().getAt()));
            adjustmentSource.setStatusCancelledAt(null);
        } else if (invoicePaymentAdjustmentStatus.isSetCancelled()) {
            adjustmentSource.setStatusCapturedAt(null);
            adjustmentSource.setStatusCancelledAt(TypeUtil.stringToLocalDateTime(invoicePaymentAdjustmentStatus.getCancelled().getAt()));
        }

        Long adjId = adjustmentDao.save(adjustmentSource);
        if (adjId == null) {
            log.info("Received duplicate key value when change payment adjustment status with " +
                    "invoiceId='{}', changeId='{}', sequenceId='{}'", invoiceId, changeId, sequenceId);
        } else {
            adjustmentDao.updateNotCurrent(adjustmentSourceId);
            List<CashFlow> newCashFlows = cashFlowDao.getForAdjustments(adjustmentSourceId, AdjustmentCashFlowType.new_cash_flow);
            newCashFlows.forEach(pcf -> {
                pcf.setId(null);
                pcf.setObjId(adjId);
            });
            cashFlowDao.save(newCashFlows);
            List<CashFlow> oldCashFlows = cashFlowDao.getForAdjustments(adjustmentSourceId, AdjustmentCashFlowType.old_cash_flow_inverse);
            oldCashFlows.forEach(pcf -> {
                pcf.setId(null);
                pcf.setObjId(adjId);
            });
            cashFlowDao.save(oldCashFlows);

            log.info("Adjustment status change has been saved (invoiceId={}, paymentId={}, adjustmentId={}, " +
                    "sequenceId={}, status={}", invoiceId, paymentId, adjustmentId, sequenceId,
                    invoicePaymentAdjustmentStatus.getSetField().getFieldName());
        }
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
