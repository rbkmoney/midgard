package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.refund;

import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.payment_processing.*;
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
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentRefundSessionChangeTransactionBoundHandler extends AbstractInvoicingHandler {

    private final RefundDao refundDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_refund_change.payload" +
                    ".invoice_payment_session_change.payload.session_transaction_bound",
            new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange change, SimpleEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        InvoicePaymentRefundChange invoicePaymentRefundChange = invoicePaymentChange.getPayload().getInvoicePaymentRefundChange();
        String refundId = invoicePaymentRefundChange.getId();
        InvoicePaymentSessionChange sessionChange = invoicePaymentRefundChange.getPayload().getInvoicePaymentSessionChange();
        long sequenceId = event.getEventId();

        log.info("Start handling refund session change transaction info, sequenceId='{}', invoiceId='{}', " +
                "paymentId='{}', refundId='{}'", sequenceId, invoiceId, paymentId, refundId);
        Refund refundSource = refundDao.get(invoiceId, paymentId, refundId);
        if (refundSource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Refund not found, sequenceId='{}', invoiceId='{}', paymentId='{}', refundId='{}'",
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
        refundSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        SessionChangePayload payload = sessionChange.getPayload();
        TransactionInfo transactionInfo = payload.getSessionTransactionBound().getTrx();
        refundSource.setSessionPayloadTransactionBoundTrxId(transactionInfo.getId());
        refundSource.setSessionPayloadTransactionBoundTrxExtraJson(JsonUtil.objectToJsonString(transactionInfo.getExtra()));
        refundDao.updateNotCurrent(invoiceId, paymentId, refundId);
        long rfndId = refundDao.save(refundSource);
        List<CashFlow> cashFlows = cashFlowDao.getByObjId(refundSourceId, PaymentChangeType.refund);
        cashFlows.forEach(pcf -> {
            pcf.setId(null);
            pcf.setObjId(rfndId);
        });
        cashFlowDao.save(cashFlows);
        log.info("Refund session transaction info has been saved, sequenceId='{}', invoiceId='{}', " +
                "paymentId='{}', refundId='{}'", sequenceId, invoiceId, paymentId, refundId);
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
