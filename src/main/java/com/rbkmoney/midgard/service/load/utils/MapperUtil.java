package com.rbkmoney.midgard.service.load.utils;

import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;

import java.util.List;
import java.util.stream.Collectors;

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

    public static ClearingTransaction transformTransaction(InvoicePayment invoicePayment,
                                                           SimpleEvent event,
                                                           String invoiceId,
                                                           Integer changeId,
                                                           Long lastSourceRowId) {
        ClearingTransaction trx = new ClearingTransaction();

        var paymentRoute = invoicePayment.getRoute();
        trx.setProviderId(paymentRoute.getProvider().getId());
        trx.setRouteTerminalId(paymentRoute.getTerminal().getId());

        trx.setInvoiceId(invoiceId);
        long sequenceId = event.getSequenceId();
        trx.setSequenceId(sequenceId);
        trx.setChangeId(changeId);
        trx.setSourceRowId(lastSourceRowId + 1);
        trx.setTransactionClearingState(TransactionClearingState.READY);
        trx.setTrxVersion(DEFAULT_TRX_VERSION);

        var payment = invoicePayment.getPayment();
        trx.setPaymentId(payment.getId());
        trx.setPartyId(payment.getOwnerId());
        trx.setShopId(payment.getShopId());
        trx.setTransactionDate(TypeUtil.stringToLocalDateTime(payment.getCreatedAt()));

        InvoicePaymentSession paymentSession = invoicePayment.getSessions().stream()
                .filter(session -> session.getTargetStatus().isSetCaptured())
                .findFirst()
                .orElse(null);
        if (paymentSession == null) {
            throw new NotFoundException(String.format("Session for transaction with invoice id '%s', " +
                    "sequence id '%d' and change id '%d' not found!", invoiceId, sequenceId, changeId));
        }

        fillPaymentTrxInfo(trx, paymentSession);
        fillPaymentCashInfo(trx, payment);
        fillPayerInfoToTrx(trx, payment);
        return trx;
    }

    private static void fillPaymentTrxInfo(ClearingTransaction trx, InvoicePaymentSession paymentSession) {
        var transactionInfo = paymentSession.getTransactionInfo();
        trx.setTransactionId(transactionInfo.getId());
        trx.setExtra(JsonUtil.objectToJsonString(transactionInfo.getExtra()));
    }

    private static void fillPaymentCashInfo(ClearingTransaction trx,
                                            com.rbkmoney.damsel.domain.InvoicePayment payment) {
        var cost = payment.getCost();
        trx.setTransactionAmount(cost.getAmount());
        trx.setTransactionCurrency(cost.getCurrency().getSymbolicCode());
    }

    private static void fillPayerInfoToTrx(ClearingTransaction trx,
                                           com.rbkmoney.damsel.domain.InvoicePayment payment) {
        var payer = payment.getPayer();
        trx.setPayerType(payer.getSetField().getFieldName());
        trx.setIsRecurrent(payer.isSetRecurrent());

        var bankCard = extractBankCard(payer);
        trx.setPayerBankCardToken(bankCard.getToken());
        trx.setPayerBankCardPaymentSystem(bankCard.getPaymentSystem().name());
        trx.setPayerBankCardBin(bankCard.getBin());
        trx.setPayerBankCardMaskedPan(bankCard.getMaskedPan());
        trx.setPayerBankCardTokenProvider(bankCard.getTokenProvider() == null ? null : bankCard.getTokenProvider().name());

        if (payer.isSetRecurrent() && payer.getRecurrent().isSetRecurrentParent()) {
            var recurrentParent = payer.getRecurrent().getRecurrentParent();
            trx.setPayerRecurrentParentInvoiceId(recurrentParent.getInvoiceId());
            trx.setPayerRecurrentParentPaymentId(recurrentParent.getPaymentId());
        }

    }

    private static com.rbkmoney.damsel.domain.BankCard extractBankCard(com.rbkmoney.damsel.domain.Payer payer) {
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

    public static ClearingRefund transformRefund(InvoicePaymentRefund invoicePaymentRefund,
                                                 SimpleEvent event,
                                                 com.rbkmoney.damsel.domain.InvoicePayment payment,
                                                 Integer changeId,
                                                 Long lastSourceRowId) {
        var refund = invoicePaymentRefund.getRefund();

        ClearingRefund clearingRefund = new ClearingRefund();
        clearingRefund.setInvoiceId(event.getSourceId());
        clearingRefund.setSequenceId(event.getSequenceId());
        clearingRefund.setChangeId(changeId);
        clearingRefund.setPaymentId(payment.getId());
        clearingRefund.setRefundId(refund.getId());
        clearingRefund.setPartyId(payment.getOwnerId());
        clearingRefund.setShopId(payment.getShopId());
        clearingRefund.setCreatedAt(TypeUtil.stringToLocalDateTime(refund.getCreatedAt()));
        clearingRefund.setReason(refund.getReason());
        clearingRefund.setDomainRevision(refund.getDomainRevision());
        clearingRefund.setClearingState(TransactionClearingState.READY);
        clearingRefund.setTrxVersion(DEFAULT_TRX_VERSION);
        clearingRefund.setSourceRowId(lastSourceRowId + 1);
        fillRefundCashInfo(refund, clearingRefund);
        fillTransactionAdditionalInfo(invoicePaymentRefund, clearingRefund, event);

        return clearingRefund;
    }

    private static void fillRefundCashInfo(com.rbkmoney.damsel.domain.InvoicePaymentRefund refund,
                                           ClearingRefund clearingRefund) {
        var cash = refund.getCash();
        clearingRefund.setAmount(cash.getAmount());
        clearingRefund.setCurrencyCode(cash.getCurrency().getSymbolicCode());
    }

    private static void fillTransactionAdditionalInfo(InvoicePaymentRefund invoicePaymentRefund,
                                                      ClearingRefund clearingRefund,
                                                      SimpleEvent event) {
        InvoiceRefundSession refundSession = invoicePaymentRefund.getSessions().stream()
                .findFirst()
                .orElse(null);

        if (refundSession == null) {
            throw new NotFoundException(String.format("Refund session for refund (invoice id '%s'," +
                    "sequenceId id '%d') not found!", event.getSourceId(), event.getSequenceId()));
        }

        var transactionInfo = refundSession.getTransactionInfo();
        clearingRefund.setTransactionId(transactionInfo.getId());
        clearingRefund.setExtra(JsonUtil.objectToJsonString(transactionInfo.getExtra()));
    }

    public static boolean isExistProviderId(List<ClearingAdapter> adapters, int providerId) {
        List<Integer> proveidersIds = adapters.stream()
                .map(ClearingAdapter::getAdapterId)
                .collect(Collectors.toList());
        return proveidersIds.contains(providerId);
    }

}
