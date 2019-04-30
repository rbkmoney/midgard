package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.payment;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.PaymentDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import com.rbkmoney.midgard.service.load.utils.CashFlowUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentCashFlowChangedHandler extends AbstractInvoicingHandler {

    private final PaymentDao paymentDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_cash_flow_changed",
            new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange change, MachineEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        long sequenceId = event.getEventId();

        log.info("Start handling payment cashflow change, sequenceId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, invoiceId, paymentId);
        Payment paymentSource = paymentDao.get(invoiceId, paymentId);
        if (paymentSource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Payment not found, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
            return;
            //throw new NotFoundException(String.format("Payment not found, invoiceId='%s', paymentId='%s'",
            //        invoiceId, paymentId));
        }
        paymentSource.setId(null);
        paymentSource.setWtime(null);
        paymentSource.setChangeId(changeId);
        paymentSource.setSequenceId(sequenceId);
        paymentSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        paymentDao.updateNotCurrent(invoiceId, paymentId);
        long pmntId = paymentDao.save(paymentSource);
        List<CashFlow> cashFlows = CashFlowUtil.convertCashFlows(
                invoicePaymentChange.getPayload().getInvoicePaymentCashFlowChanged().getCashFlow(),
                pmntId,
                PaymentChangeType.payment
        );
        cashFlowDao.save(cashFlows);
        paymentDao.updateCommissions(pmntId);
        log.info("Payment cashflow has been saved, sequenceId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, invoiceId, paymentId);
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }

}
