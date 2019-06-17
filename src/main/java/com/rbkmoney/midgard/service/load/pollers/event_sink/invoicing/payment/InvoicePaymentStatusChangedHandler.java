package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.payment;

import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.PaymentDao;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import com.rbkmoney.midgard.service.load.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.enums.PaymentStatus;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentStatusChangedHandler extends AbstractInvoicingHandler {

    private final PaymentDao paymentDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("invoice_payment_change.payload.invoice_payment_status_changed",
                    new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) throws DaoException {
        InvoicePaymentStatus invoicePaymentStatus =
                invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus();
        long sequenceId = event.getSequenceId();
        String invoiceId = event.getSourceId();
        String paymentId = invoiceChange.getInvoicePaymentChange().getId();

        log.info("Start payment status changed handling (invoiceId={}, paymentId={}, sequenceId={}, status={})",
                invoiceId, paymentId, sequenceId, invoicePaymentStatus.getSetField().getFieldName());

        Payment paymentSource = paymentDao.get(invoiceId, paymentId);
        if (paymentSource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Invoice payment not found (invoiceId='{}', paymentId='{}', sequenceId='{}')",
                    invoiceId, paymentId, sequenceId);
            return;
            //throw new NotFoundException(String.format("Payment not found, invoiceId='%s', paymentId='%s'", invoiceId, paymentId));
        }
        Long paymentSourceId = paymentSource.getId();
        paymentSource.setId(null);
        paymentSource.setWtime(null);
        paymentSource.setChangeId(changeId);
        paymentSource.setSequenceId(sequenceId);
        paymentSource.setEventId(event.getEventId());
        paymentSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        paymentSource.setStatus(TBaseUtil.unionFieldToEnum(invoicePaymentStatus, PaymentStatus.class));
        if (invoicePaymentStatus.isSetCancelled()) {
            paymentSource.setStatusCancelledReason(invoicePaymentStatus.getCancelled().getReason());
            paymentSource.setStatusCapturedReason(null);
            paymentSource.setStatusFailedFailure(null);
        } else if (invoicePaymentStatus.isSetCaptured()) {
            if (invoicePaymentStatus.getCaptured().isSetCost()) {
                Cash cost = invoicePaymentStatus.getCaptured().getCost();
                paymentSource.setAmount(cost.getAmount());
                paymentSource.setCurrencyCode(cost.getCurrency().getSymbolicCode());
            }
            paymentSource.setStatusCancelledReason(null);
            paymentSource.setStatusCapturedReason(invoicePaymentStatus.getCaptured().getReason());
            paymentSource.setStatusFailedFailure(null);
        } else if (invoicePaymentStatus.isSetFailed()) {
            paymentSource.setStatusCancelledReason(null);
            paymentSource.setStatusCapturedReason(null);
            paymentSource.setStatusFailedFailure(JsonUtil.tBaseToJsonString(invoicePaymentStatus.getFailed()));
        }

        paymentDao.updateNotCurrent(invoiceId, paymentId);
        Long pmntId = paymentDao.save(paymentSource);
        if (pmntId == null) {
            log.info("Payment with invoiceId='{}', changeId='{}' and sequenceId='{}' already processed. " +
                    "A new payment status change record will not be added", invoiceId, changeId, sequenceId);
        } else {
            List<CashFlow> cashFlows = cashFlowDao.getByObjId(paymentSourceId, PaymentChangeType.payment);
            cashFlows.forEach(pcf -> {
                pcf.setId(null);
                pcf.setObjId(pmntId);
            });
            cashFlowDao.save(cashFlows);

            log.info("Payment status has been saved (invoiceId={}, paymentId={}, sequenceId={}, status={})",
                    invoiceId, paymentId, sequenceId, invoicePaymentStatus.getSetField().getFieldName());
        }
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
