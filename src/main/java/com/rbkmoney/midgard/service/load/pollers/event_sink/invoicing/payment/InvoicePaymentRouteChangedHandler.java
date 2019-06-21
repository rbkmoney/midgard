package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.payment;

import com.rbkmoney.damsel.domain.PaymentRoute;
import com.rbkmoney.damsel.payment_processing.Event;
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
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentRouteChangedHandler extends AbstractInvoicingHandler {

    private final PaymentDao paymentDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_route_changed",
                new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        PaymentRoute paymentRoute = invoicePaymentChange.getPayload().getInvoicePaymentRouteChanged().getRoute();
        long sequenceId = event.getSequenceId();

        log.info("Start handling payment route change, route='{}', invoiceId='{}', paymentId='{}', sequenceId='{}'",
                paymentRoute, invoiceId, paymentId, sequenceId);
        Payment paymentSource = paymentDao.get(invoiceId, paymentId);
        if (paymentSource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Invoice payment not found (invoiceId='{}', paymentId='{}', sequenceId='{}')",
                    invoiceId, paymentId, sequenceId);
            return;
            //throw new NotFoundException(String.format("Invoice payment not found, invoiceId='%s', paymentId='%s'",
            //        invoiceId, paymentId));
        }
        Long paymentSourceId = paymentSource.getId();
        paymentSource.setId(null);
        paymentSource.setWtime(null);
        paymentSource.setChangeId(changeId);
        paymentSource.setSequenceId(sequenceId);
        paymentSource.setEventId(event.getEventId());
        paymentSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        paymentSource.setRouteProviderId(paymentRoute.getProvider().getId());
        paymentSource.setRouteTerminalId(paymentRoute.getTerminal().getId());
        Long pmntId = paymentDao.save(paymentSource);
        if (pmntId == null) {
            log.info("Payment with invoiceId='{}', changeId='{}' and sequenceId='{}' already processed. " +
                    "A new payment route change record will not be added", invoiceId, changeId, sequenceId);
        } else {
            paymentDao.updateNotCurrent(paymentSourceId);
            List<CashFlow> cashFlows = cashFlowDao.getByObjId(paymentSourceId, PaymentChangeType.payment);
            cashFlows.forEach(pcf -> {
                pcf.setId(null);
                pcf.setObjId(pmntId);
            });
            cashFlowDao.save(cashFlows);
            log.info("Payment route have been saved (route='{}', invoiceId='{}', paymentId='{}', sequenceId='{}')",
                    paymentRoute, invoiceId, paymentId, sequenceId);
        }
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
