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
import org.jooq.generated.feed.enums.RiskScore;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentRiskScoreChangedHandler extends AbstractInvoicingHandler {

    private final PaymentDao paymentDao;

    private final CashFlowDao cashFlowDao;

    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_risk_score_changed",
            new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = invoiceChange.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        com.rbkmoney.damsel.domain.RiskScore riskScore =
                invoicePaymentChange.getPayload().getInvoicePaymentRiskScoreChanged().getRiskScore();
        long sequenceId = event.getSequenceId();

        log.info("Start handling payment risk score change, sequenceId='{}', invoiceId='{}', paymentId='{}'",
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
        paymentSource.setEventId(event.getEventId());
        paymentSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        RiskScore score = TypeUtil.toEnumField(riskScore.name(), RiskScore.class);
        if (score == null) {
            throw new IllegalArgumentException("Illegal risk score: " + riskScore);
        }
        paymentSource.setRiskScore(score);
        paymentDao.updateNotCurrent(invoiceId, paymentId);
        Long pmntId = paymentDao.save(paymentSource);
        if (pmntId == null) {
            log.info("Payment with sequenceId='{}', invoiceId='{}' and changeId='{}' already processed. " +
                    "A new payment risk score change record will not be added", sequenceId, invoiceId, changeId);
        } else {
            List<CashFlow> cashFlows = cashFlowDao.getByObjId(paymentSourceId, PaymentChangeType.payment);
            cashFlows.forEach(pcf -> {
                pcf.setId(null);
                pcf.setObjId(pmntId);
            });
            cashFlowDao.save(cashFlows);
            log.info("Payment risk score have been saved, sequenceId='{}', invoiceId='{}', paymentId='{}'",
                    sequenceId, invoiceId, paymentId);
        }
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
