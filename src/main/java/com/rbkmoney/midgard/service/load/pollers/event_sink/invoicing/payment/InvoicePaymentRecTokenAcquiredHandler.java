package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.payment;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.PaymentDao;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
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
public class InvoicePaymentRecTokenAcquiredHandler extends AbstractInvoicingHandler {

    private final PaymentDao paymentDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_rec_token_acquired",
            new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange change, SimpleEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        String token = invoicePaymentChange.getPayload().getInvoicePaymentRecTokenAcquired().getToken();
        long sequenceId = event.getEventId();

        log.info("Start handling payment recurrent token acquired, sequenceId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, invoiceId, paymentId);
        Payment paymentSource = paymentDao.get(invoiceId, paymentId);
        if (paymentSource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Invoice payment not found, sequenceId='{}', invoiceId='{}', paymentId='{}'",
                    sequenceId, invoiceId, paymentId);
            return;
            //throw new NotFoundException(String.format("Invoice payment not found, invoiceId='%s', paymentId='%s'",
            //        invoiceId, paymentId));
        }
        Long paymentSourceId = paymentSource.getId();
        paymentSource.setId(null);
        paymentSource.setWtime(null);
        paymentSource.setChangeId(changeId);
        paymentSource.setSequenceId(sequenceId);
        paymentSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        paymentSource.setRecurrentIntentionToken(token);
        paymentDao.updateNotCurrent(invoiceId, paymentId);
        long pmntId = paymentDao.save(paymentSource);
        List<CashFlow> cashFlows = cashFlowDao.getByObjId(paymentSourceId, PaymentChangeType.payment);
        cashFlows.forEach(pcf -> {
            pcf.setId(null);
            pcf.setObjId(pmntId);
        });
        cashFlowDao.save(cashFlows);
        log.info("Payment recurrent token have been saved, sequenceId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, invoiceId, paymentId);
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
