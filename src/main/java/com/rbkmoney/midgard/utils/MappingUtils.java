package com.rbkmoney.midgard.utils;

import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.damsel.payment_processing.InvoiceRefundSession;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.Content;
import com.rbkmoney.midgard.GeneralTransactionInfo;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.TransactionCardInfo;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.exception.NotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.ClearingTrxType;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingEventTransactionInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.FailureTransaction;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MappingUtils {

    public static final int DEFAULT_TRX_VERSION = 1;

    public static Transaction transformRefundTransaction(ClearingTransaction clrTran,
                                                         ClearingRefund refund) {
        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo();
        generalTranInfo.setTransactionId(refund.getTransactionId());
        generalTranInfo.setTransactionDate(refund.getCreatedAt().toInstant(ZoneOffset.UTC).toString());
        generalTranInfo.setTransactionAmount(refund.getAmount());
        generalTranInfo.setTransactionCurrency(refund.getCurrencyCode());
        generalTranInfo.setInvoiceId(refund.getInvoiceId());
        generalTranInfo.setPaymentId(refund.getPaymentId());
        generalTranInfo.setTransactionType("REFUND");

        return fillAdditionalInfo(generalTranInfo, clrTran, refund.getExtra());
    }

    public static Transaction transformClearingTransaction(ClearingTransaction clrTran) {
        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo();
        generalTranInfo.setTransactionId(clrTran.getTransactionId());
        generalTranInfo.setTransactionDate(clrTran.getTransactionDate().toInstant(ZoneOffset.UTC).toString());
        generalTranInfo.setTransactionAmount(clrTran.getTransactionAmount());
        generalTranInfo.setTransactionCurrency(clrTran.getTransactionCurrency());
        generalTranInfo.setInvoiceId(clrTran.getInvoiceId());
        generalTranInfo.setPaymentId(clrTran.getPaymentId());
        generalTranInfo.setTransactionType("PAYMENT");

        return fillAdditionalInfo(generalTranInfo, clrTran, clrTran.getExtra());
    }

    private static Transaction fillAdditionalInfo(GeneralTransactionInfo generalTranInfo,
                                                  ClearingTransaction clrTran,
                                                  String extra) {
        Transaction transaction = new Transaction();
        transaction.setGeneralTransactionInfo(generalTranInfo);
        transaction.setTransactionCardInfo(getTransactionCardInfo(clrTran));
        transaction.setAdditionalTransactionData(transformContent(extra));
        transaction.setTransactionCashFlow(new ArrayList<>());

        return transaction;
    }

    private static Content transformContent(String extra) {
        Content additionalTranData = new Content();
        additionalTranData.setType("application/json");
        additionalTranData.setData(extra.getBytes());
        return additionalTranData;
    }

    private static TransactionCardInfo getTransactionCardInfo(ClearingTransaction clrTran) {
        TransactionCardInfo tranCardInfo = new TransactionCardInfo();
        tranCardInfo.setPayerBankCardToken(clrTran.getPayerBankCardToken());
        tranCardInfo.setPayerBankCardBin(clrTran.getPayerBankCardBin());
        tranCardInfo.setPayerBankCardMaskedPan(clrTran.getPayerBankCardMaskedPan());
        tranCardInfo.setPayerBankCardPaymentSystem(clrTran.getPayerBankCardPaymentSystem());
        tranCardInfo.setPayerBankCardTokenProvider(clrTran.getPayerBankCardTokenProvider());
        return tranCardInfo;
    }

    public static FailureTransaction getFailureTransaction(Transaction transaction, Long clearingId) {
        FailureTransaction failureTransaction = new FailureTransaction();
        GeneralTransactionInfo transactionInfo = transaction.getGeneralTransactionInfo();
        failureTransaction.setClearingId(clearingId);
        failureTransaction.setTransactionId(transactionInfo.getTransactionId());
        failureTransaction.setInvoiceId(transactionInfo.getInvoiceId());
        failureTransaction.setPaymentId(transactionInfo.getPaymentId());
        failureTransaction.setRefundId(transactionInfo.getRefundId());
        failureTransaction.setErrorReason(transaction.getComment());
        failureTransaction.setTransactionType(ClearingTrxType.valueOf(transactionInfo.getTransactionType()));
        return failureTransaction;
    }

    public static FailureTransaction getFailureTransaction(ClearingEventTransactionInfo info,
                                                           String errorMessage,
                                                           ClearingTrxType type) {
        FailureTransaction failureTransaction = new FailureTransaction();
        failureTransaction.setClearingId(info.getClearingId());
        failureTransaction.setTransactionId(info.getTransactionId());
        failureTransaction.setInvoiceId(info.getInvoiceId());
        failureTransaction.setPaymentId(info.getPaymentId());
        failureTransaction.setRefundId(info.getRefundId());
        failureTransaction.setErrorReason(errorMessage);
        failureTransaction.setTransactionType(type);
        return failureTransaction;
    }

    public static ClearingEventTransactionInfo transformClearingTrx(long clearingId,
                                                                    int providerId,
                                                                    ClearingTransaction trx) {
        ClearingEventTransactionInfo eventTrxInfo = new ClearingEventTransactionInfo();
        eventTrxInfo.setClearingId(clearingId);
        eventTrxInfo.setTransactionType(ClearingTrxType.PAYMENT);
        eventTrxInfo.setInvoiceId(trx.getInvoiceId());
        eventTrxInfo.setPaymentId(trx.getPaymentId());
        eventTrxInfo.setTransactionId(trx.getTransactionId());
        eventTrxInfo.setTrxVersion(trx.getTrxVersion());
        eventTrxInfo.setRowNumber(trx.getSourceRowId());
        eventTrxInfo.setProviderId(providerId);
        return eventTrxInfo;
    }

    public static ClearingEventTransactionInfo transformClearingRefund(long clearingId,
                                                                       int providerId,
                                                                       ClearingRefund refund) {
        ClearingEventTransactionInfo eventTrxInfo = new ClearingEventTransactionInfo();
        eventTrxInfo.setClearingId(clearingId);
        eventTrxInfo.setTransactionType(ClearingTrxType.REFUND);
        eventTrxInfo.setInvoiceId(refund.getInvoiceId());
        eventTrxInfo.setPaymentId(refund.getPaymentId());
        eventTrxInfo.setRefundId(refund.getRefundId());
        eventTrxInfo.setTransactionId(refund.getTransactionId());
        eventTrxInfo.setTrxVersion(refund.getTrxVersion());
        eventTrxInfo.setRowNumber(refund.getSourceRowId());
        eventTrxInfo.setProviderId(providerId);
        return eventTrxInfo;
    }

    public static ClearingTransaction transformTransaction(InvoicePayment invoicePayment,
                                                           MachineEvent event,
                                                           String invoiceId,
                                                           Integer changeId) {
        ClearingTransaction trx = new ClearingTransaction();

        var paymentRoute = invoicePayment.getRoute();
        trx.setProviderId(paymentRoute.getProvider().getId());
        trx.setRouteTerminalId(paymentRoute.getTerminal().getId());

        trx.setInvoiceId(invoiceId);
        long sequenceId = event.getEventId();
        trx.setSequenceId(sequenceId);
        trx.setChangeId(changeId);
        trx.setSourceRowId(0L);
        trx.setTransactionClearingState(TransactionClearingState.READY);
        trx.setTrxVersion(MappingUtils.DEFAULT_TRX_VERSION);

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
                                                 MachineEvent event,
                                                 com.rbkmoney.damsel.domain.InvoicePayment payment,
                                                 Integer changeId) {
        var refund = invoicePaymentRefund.getRefund();

        ClearingRefund clearingRefund = new ClearingRefund();
        clearingRefund.setInvoiceId(event.getSourceId());
        clearingRefund.setSequenceId(event.getEventId());
        clearingRefund.setChangeId(changeId);
        clearingRefund.setPaymentId(payment.getId());
        clearingRefund.setRefundId(refund.getId());
        clearingRefund.setPartyId(payment.getOwnerId());
        clearingRefund.setShopId(payment.getShopId());
        clearingRefund.setCreatedAt(TypeUtil.stringToLocalDateTime(refund.getCreatedAt()));
        clearingRefund.setReason(refund.getReason());
        clearingRefund.setDomainRevision(refund.getDomainRevision());
        clearingRefund.setClearingState(TransactionClearingState.READY);
        clearingRefund.setTrxVersion(MappingUtils.DEFAULT_TRX_VERSION);
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
                                                      MachineEvent event) {
        InvoiceRefundSession refundSession = invoicePaymentRefund.getSessions().stream()
                .findFirst()
                .orElse(null);

        if (refundSession == null) {
            throw new NotFoundException(String.format("Refund session for refund (invoice id '%s'," +
                    "sequenceId id '%d') not found!", event.getSourceId(), event.getEventId()));
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
