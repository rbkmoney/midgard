package com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.payment;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.CashFlowDao;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.PaymentDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.invoicing.AbstractInvoicingHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class InvoicePaymentRecTokenAcquiredHandler extends AbstractInvoicingHandler {

    private final PaymentDao paymentDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter;

    @Autowired
    public InvoicePaymentRecTokenAcquiredHandler(PaymentDao paymentDao, CashFlowDao cashFlowDao) {
        this.paymentDao = paymentDao;
        this.cashFlowDao = cashFlowDao;
        this.filter = new PathConditionFilter(new PathConditionRule(
                "invoice_payment_change.payload.invoice_payment_rec_token_acquired",
                new IsNullCondition().not()));
    }

    @Override
    @Transactional
    public void handle(InvoiceChange change, Event event) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSource().getInvoiceId();
        String paymentId = invoicePaymentChange.getId();
        String token = invoicePaymentChange.getPayload().getInvoicePaymentRecTokenAcquired().getToken();
        log.info("Start handling payment recurrent token acquired, eventId='{}', invoiceId='{}', paymentId='{}'",
                event.getId(), invoiceId, paymentId);
        Payment paymentSource = paymentDao.get(invoiceId, paymentId);
        if (paymentSource == null) {
            // TODO: исправить после того как прольется БД
            log.error("Invoice payment not found, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
            return;
            //throw new NotFoundException(String.format("Invoice payment not found, invoiceId='%s', paymentId='%s'",
            //        invoiceId, paymentId));
        }
        Long paymentSourceId = paymentSource.getId();
        paymentSource.setId(null);
        paymentSource.setWtime(null);
        paymentSource.setEventId(event.getId());
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
        log.info("Payment recurrent token have been saved, eventId='{}', invoiceId='{}', paymentId='{}'",
                event.getId(), invoiceId, paymentId);
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
