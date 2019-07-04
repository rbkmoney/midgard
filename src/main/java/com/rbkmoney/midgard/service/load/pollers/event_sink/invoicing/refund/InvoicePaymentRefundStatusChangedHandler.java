package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.refund;

import com.rbkmoney.damsel.domain.InvoicePaymentRefundStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.RefundDao;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import com.rbkmoney.midgard.service.load.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.enums.RefundStatus;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentRefundStatusChangedHandler extends AbstractInvoicingHandler {

    private final RefundDao refundDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_refund_change" +
                    ".payload.invoice_payment_refund_status_changed",
            new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) {
        long sequenceId = event.getSequenceId();
        String invoiceId = event.getSourceId();
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String paymentId = invoiceChange.getInvoicePaymentChange().getId();
        InvoicePaymentRefundChange invoicePaymentRefundChange = invoicePaymentChange.getPayload()
                .getInvoicePaymentRefundChange();
        InvoicePaymentRefundStatus invoicePaymentRefundStatus =
                invoicePaymentRefundChange.getPayload().getInvoicePaymentRefundStatusChanged().getStatus();
        String refundId = invoicePaymentRefundChange.getId();

        log.info("Start refund status changed handling (invoiceId={}, paymentId={}, refundId={}, sequenceId={}, status={})",
                invoiceId, paymentId, refundId, sequenceId, invoicePaymentRefundStatus.getSetField().getFieldName());
        Refund refundSource = refundDao.get(invoiceId, paymentId, refundId);
        if (refundSource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Refund not found, sequenceId={}, invoiceId='{}', paymentId='{}', refundId='{}'",
                    sequenceId, invoiceId, paymentId, refundId);
            return;
            //throw new NotFoundException(String.format("Refund not found, invoiceId='%s', paymentId='%s', refundId='%s'",
            //        invoiceId, paymentId, refundId));
        }
        Long refundSourceId = refundSource.getId();
        refundSource.setId(null);
        refundSource.setWtime(null);
        refundSource.setChangeId(changeId);
        refundSource.setSequenceId(sequenceId);
        refundSource.setEventId(event.getEventId());
        refundSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        refundSource.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentRefundStatus, RefundStatus.class));
        if (invoicePaymentRefundStatus.isSetFailed()) {
            refundSource.setStatusFailedFailure(JsonUtil.tBaseToJsonString(invoicePaymentRefundStatus.getFailed()));
        } else {
            refundSource.setStatusFailedFailure(null);
        }
        Long rfndId = refundDao.save(refundSource);
        if (rfndId == null) {
            log.info("Refund event with invoiceId='{}', changeId='{}' and sequenceId='{}' already processed. " +
                    "A new status change record will not be added", invoiceId, changeId, sequenceId);
        } else {
            refundDao.updateNotCurrent(refundSourceId);
            List<CashFlow> cashFlows = cashFlowDao.getByObjId(refundSourceId, PaymentChangeType.refund);
            cashFlows.forEach(pcf -> {
                pcf.setId(null);
                pcf.setObjId(rfndId);
            });
            cashFlowDao.save(cashFlows);

            log.info("Refund have been succeeded (invoiceId={}, paymentId={}, refundId={}, sequenceId={}, status={})",
                    invoiceId, paymentId, refundId, sequenceId, invoicePaymentRefundStatus.getSetField().getFieldName());
        }
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
