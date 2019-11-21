package com.rbkmoney.midgard.service.load.utils;

import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.domain.Payer;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;

import java.time.LocalDateTime;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.DEFAULT_TRX_VERSION;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapperUtil {

    private static final String KAFKA_SOURCE_NAME = "kafka";
    private static final String BUSTERMASE_SOURCE_NAME = "bustermase";

    public static SimpleEvent transformMachineEvent(MachineEvent event) {
        return SimpleEvent.builder()
                .sequenceId(event.getEventId())
                .sourceId(event.getSourceId())
                .createdAt(event.getCreatedAt())
                .eventSourceName(KAFKA_SOURCE_NAME)
                .build();
    }

    public static SimpleEvent transformSinkEvent(Event event) {
        return SimpleEvent.builder()
                .eventId(event.getId())
                .sequenceId(event.getSequence())
                .sourceId(event.getSource().getInvoiceId())
                .createdAt(event.getCreatedAt())
                .eventSourceName(BUSTERMASE_SOURCE_NAME)
                .build();
    }

    public static ClearingTransaction transformTransaction(com.rbkmoney.damsel.domain.InvoicePayment payment,
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

        Payer payer = payment.getPayer();
        trx.setPayerType(payer.getSetField().getFieldName());
        trx.setIsRecurrent(payer.isSetRecurrent());

        BankCard bankCard = getBankCard(payer);
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

    private static BankCard getBankCard(Payer payer) {
        if (payer.isSetCustomer()) {
            return payer.getCustomer().getPaymentTool().getBankCard();
        } else if (payer.isSetRecurrent()) {
            return payer.getRecurrent().getPaymentTool().getBankCard();
        } else if (payer.isSetPaymentResource()) {
            return payer.getPaymentResource().getResource().getPaymentTool().getBankCard();
        } else {
            throw new RuntimeException("Payer type not found!");
        }
    }

    public static ClearingRefund transformRefund(InvoicePaymentRefund refund,
                                                  SimpleEvent event,
                                                  com.rbkmoney.damsel.domain.InvoicePayment payment,
                                                  Integer changeId) {
        ClearingRefund clearingRefund = new ClearingRefund();
        clearingRefund.setInvoiceId(event.getSourceId());
        clearingRefund.setPaymentId(payment.getId());
        clearingRefund.setRefundId(refund.getId());
        clearingRefund.setTransactionId(refund.getExternalId()); //TODO: тот ли это transactionID
        clearingRefund.setPartyId(payment.getOwnerId());
        clearingRefund.setShopId(payment.getShopId());
        clearingRefund.setCreatedAt(LocalDateTime.parse(refund.getCreatedAt()));
        Cash cash = refund.getCash();
        clearingRefund.setAmount(cash.getAmount());
        clearingRefund.setCurrencyCode(cash.getCurrency().getSymbolicCode());
        clearingRefund.setReason(refund.getReason());
        clearingRefund.setDomainRevision(refund.getDomainRevision());
        clearingRefund.setExtra(null); //TODO: where is extra?
        clearingRefund.setClearingState(TransactionClearingState.READY);
        clearingRefund.setTrxVersion(DEFAULT_TRX_VERSION);

        clearingRefund.setSequenceId(event.getSequenceId());
        clearingRefund.setChangeId(changeId);
        return clearingRefund;
    }

    public static void checkRouteInfo(com.rbkmoney.damsel.domain.InvoicePayment payment,
                                         String paymentId,
                                         String invoiceId) {
        if (payment.getRoute() == null || payment.getRoute().getProvider() == null) {
            throw new NotFoundException("Provider ID for invoice " + invoiceId + " with payment id " +
                    paymentId + " not found!");
        }
    }

}
