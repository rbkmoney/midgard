package com.rbkmoney.midgard.service.load.handler.invoicing;

import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.Payer;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.DEFAULT_TRX_VERSION;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusChangedHandler extends AbstractInvoicingHandler {

    private final TransactionsDao transactionsDao;

    private final InvoicingSrv.Iface invoicingService;

    private final UserInfo userInfo;

    private final List<ClearingAdapter> adapters;

    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("invoice_payment_change.payload.invoice_payment_status_changed",
                    new IsNullCondition().not()));

    @Override
    @Transactional
    public void handle(InvoiceChange invoiceChange, SimpleEvent event, Integer changeId) throws Exception {
        InvoicePaymentStatus invoicePaymentStatus =
                invoiceChange.getInvoicePaymentChange().getPayload().getInvoicePaymentStatusChanged().getStatus();
        if (invoicePaymentStatus.isSetCaptured()) {
            String invoiceId = event.getSourceId();
            String paymentId = invoiceChange.getInvoicePaymentChange().getId();
            try {
                EventRange eventRange = new EventRange();
                eventRange.setAfter(0L);
                eventRange.setLimit(Integer.MAX_VALUE);
                Invoice invoice = invoicingService.get(userInfo, invoiceId, eventRange);
                com.rbkmoney.damsel.domain.InvoicePayment payment = invoice.getPayments().stream()
                        .map(invoicePayment -> invoicePayment.getPayment())
                        .filter(invoicePayment -> invoicePayment.getId() == paymentId)
                        .findFirst()
                        .orElse(null);
                if (payment == null) {
                    throw new Exception("Payment " + paymentId + " for invoice " + invoiceId + " not found");
                }
                if (payment.getRoute() == null || payment.getRoute().getProvider() == null) {
                    throw new RuntimeException("Provider ID for invoice " + invoiceId + " with payment id " +
                            paymentId + " not found!");
                }
                int providerId = payment.getRoute().getProvider().getId();
                List<Integer> proveidersIds = adapters.stream()
                        .map(ClearingAdapter::getAdapterId)
                        .collect(Collectors.toList());
                if (!proveidersIds.contains(providerId)) {
                    return;
                }
                ClearingTransaction clearingTransaction = transformTransaction(payment, event, invoiceId, changeId);
                transactionsDao.save(clearingTransaction);
            } catch (TException e) {
                log.error("Thrift error: ", e); //TODO: do it
                throw e;
            }
        }
    }

    private static ClearingTransaction transformTransaction(com.rbkmoney.damsel.domain.InvoicePayment payment,
                                                            SimpleEvent event,
                                                            String invoiceId,
                                                            Integer changeId) {
        ClearingTransaction trx = new ClearingTransaction();

        trx.setProviderId(payment.getRoute().getProvider().getId());
        trx.setRouteTerminalId(payment.getRoute().getTerminal().getId());

        trx.setInvoiceId(invoiceId);
        trx.setPaymentId(payment.getId());
        trx.setTransactionId(payment.getExternalId()); //todo: это ли transactionID
        trx.setTransactionDate(LocalDateTime.parse(payment.getCreatedAt()));
        Cash cost = payment.getCost();
        trx.setTransactionAmount(cost.getAmount());
        trx.setTransactionCurrency(cost.getCurrency().getSymbolicCode());
        trx.setTransactionClearingState(TransactionClearingState.READY);

        trx.setPartyId(payment.getOwnerId());
        trx.setShopId(payment.getShopId());

        BankCard bankCard;
        Payer payer = payment.getPayer();
        trx.setPayerType(payer.getSetField().getFieldName());

        if (payer.isSetCustomer()) {
            bankCard = payer.getCustomer().getPaymentTool().getBankCard();
        } else if (payer.isSetRecurrent()) {
            bankCard = payer.getRecurrent().getPaymentTool().getBankCard();
            trx.setIsRecurrent(payer.isSetRecurrent());
        } else if (payer.isSetPaymentResource()) {
            bankCard = payer.getPaymentResource().getResource().getPaymentTool().getBankCard();
        } else {
            throw new RuntimeException("Payer type not found!");
        }

        trx.setPayerBankCardToken(bankCard.getToken());
        trx.setPayerBankCardPaymentSystem(bankCard.getPaymentSystem().name());
        trx.setPayerBankCardBin(bankCard.getBin());
        trx.setPayerBankCardMaskedPan(bankCard.getMaskedPan());
        trx.setPayerBankCardTokenProvider(bankCard.getTokenProvider().name());

        trx.setExtra(new String(payment.getContext().getData())); //TODO: тут ли extra?

        trx.setPayerRecurrentParentInvoiceId(payer.getRecurrent().getRecurrentParent().getInvoiceId());
        trx.setPayerRecurrentParentPaymentId(payer.getRecurrent().getRecurrentParent().getPaymentId());
        trx.setSequenceId(event.getSequenceId());
        trx.setChangeId(changeId);
        trx.setSourceRowId(0L);
        trx.setTrxVersion(DEFAULT_TRX_VERSION);
        return trx;
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
