package com.rbkmoney.midgard.utils;

import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.damsel.payment_processing.InvoiceRefundSession;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.BankCardExpDate;
import com.rbkmoney.midgard.Content;
import com.rbkmoney.midgard.GeneralTransactionInfo;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.TransactionCardInfo;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.domain.enums.ClearingTrxType;
import com.rbkmoney.midgard.domain.enums.TransactionClearingState;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventTransactionInfo;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingRefund;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingTransaction;
import com.rbkmoney.midgard.domain.tables.pojos.FailureTransaction;
import com.rbkmoney.midgard.exception.NotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MappingUtils {

    public static final int DEFAULT_TRX_VERSION = 1;

    public static final int MOCKETBANK_PROVIDER_ID = 1;

    public static final String DEFAULT_CONTENT_TYPE = "application/json";

    public static final String TRANSACTION_TYPE_PAYMENT = "PAYMENT";

    public static final String TRANSACTION_TYPE_REFUND = "REFUND";

    public static Transaction transformRefundTransaction(ClearingTransaction clrTran,
                                                         ClearingRefund refund) {
        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo()
                .setTransactionId(refund.getTransactionId())
                .setTransactionDate(refund.getCreatedAt().toInstant(ZoneOffset.UTC).toString())
                .setTransactionAmount(refund.getAmount())
                .setTransactionCurrency(refund.getCurrencyCode())
                .setInvoiceId(refund.getInvoiceId())
                .setPaymentId(refund.getPaymentId())
                .setTransactionType(TRANSACTION_TYPE_REFUND)
                .setIsReversed(refund.getIsReversed() == null ? false : refund.getIsReversed());

        return fillAdditionalInfo(generalTranInfo, clrTran, refund.getExtra());
    }

    public static Transaction transformClearingTransaction(ClearingTransaction clrTran) {
        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo()
                .setTransactionId(clrTran.getTransactionId())
                .setTransactionDate(clrTran.getTransactionDate().toInstant(ZoneOffset.UTC).toString())
                .setTransactionAmount(clrTran.getTransactionAmount())
                .setTransactionCurrency(clrTran.getTransactionCurrency())
                .setInvoiceId(clrTran.getInvoiceId())
                .setPaymentId(clrTran.getPaymentId())
                .setTransactionType(TRANSACTION_TYPE_PAYMENT)
                .setIsReversed(clrTran.getIsReversed() == null ? false : clrTran.getIsReversed());

        return fillAdditionalInfo(generalTranInfo, clrTran, clrTran.getExtra());
    }

    private static Transaction fillAdditionalInfo(GeneralTransactionInfo generalTranInfo,
                                                  ClearingTransaction clrTran,
                                                  String extra) {
        return new Transaction()
                .setGeneralTransactionInfo(generalTranInfo)
                .setTransactionCardInfo(getTransactionCardInfo(clrTran))
                .setAdditionalTransactionData(transformContent(extra))
                .setTransactionCashFlow(new ArrayList<>());
    }

    private static Content transformContent(String extra) {
        return new Content()
                .setType(DEFAULT_CONTENT_TYPE)
                .setData(extra.getBytes());
    }

    private static TransactionCardInfo getTransactionCardInfo(ClearingTransaction clrTran) {
        BankCardExpDate expDate = null;
        if (clrTran.getPayerBankCardExpiredDateMonth() != null
                && clrTran.getPayerBankCardExpiredDateYear() != null) {
            expDate = new BankCardExpDate(
                    Byte.valueOf(clrTran.getPayerBankCardExpiredDateMonth()),
                    Short.valueOf(clrTran.getPayerBankCardExpiredDateYear())
            );
        }

        return new TransactionCardInfo()
                .setPayerBankCardToken(clrTran.getPayerBankCardToken())
                .setPayerBankCardBin(clrTran.getPayerBankCardBin())
                .setPayerBankCardMaskedPan(clrTran.getPayerBankCardMaskedPan())
                .setPayerBankCardPaymentSystem(clrTran.getPayerBankCardPaymentSystem())
                .setPayerBankCardTokenProvider(clrTran.getPayerBankCardTokenProvider())
                .setPayerBankCardCardholderName(clrTran.getPayerBankCardCardholderName())
                .setPayerBankCardExpDate(expDate);
    }

    public static FailureTransaction getFailureTransaction(Transaction transaction, Long clearingId) {
        GeneralTransactionInfo transactionInfo = transaction.getGeneralTransactionInfo();

        FailureTransaction failureTransaction = new FailureTransaction();
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
        eventTrxInfo.setProviderId(providerId);
        eventTrxInfo.setRowNumber(trx.getId());
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
        eventTrxInfo.setProviderId(providerId);
        eventTrxInfo.setRowNumber(refund.getId());
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
                .orElseThrow(() -> new NotFoundException(String.format("Session for transaction with " +
                                "invoice id '%s', sequence id '%d' and change id '%d' not found!",
                        invoiceId, sequenceId, changeId)));

        fillPaymentTrxInfo(trx, paymentSession);
        fillPaymentCashInfo(trx, payment);
        fillPayerInfoToTrx(trx, payment);

        return trx;
    }

    private static void fillPaymentTrxInfo(ClearingTransaction trx, InvoicePaymentSession paymentSession) {
        var transactionInfo = paymentSession.getTransactionInfo();
        if (transactionInfo != null) {
            trx.setTransactionId(transactionInfo.getId());
            trx.setExtra(JsonUtil.objectToJsonString(transactionInfo.getExtra()));
        }
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

        if (payer.isSetRecurrent() && payer.getRecurrent().isSetRecurrentParent()) {
            var recurrentParent = payer.getRecurrent().getRecurrentParent();
            trx.setPayerRecurrentParentInvoiceId(recurrentParent.getInvoiceId());
            trx.setPayerRecurrentParentPaymentId(recurrentParent.getPaymentId());
        }

        fillBankCardData(trx, extractBankCard(payer));
    }

    private static void fillBankCardData(ClearingTransaction trx, com.rbkmoney.damsel.domain.BankCard bankCard) {
        trx.setPayerBankCardToken(bankCard.getToken());
        trx.setPayerBankCardPaymentSystem(bankCard.getPaymentSystem().name());
        trx.setPayerBankCardBin(bankCard.getBin());
        trx.setPayerBankCardMaskedPan(bankCard.getBin() + "******" + bankCard.getLastDigits());
        trx.setPayerBankCardTokenProvider(bankCard.getTokenProvider() == null
                ? null : bankCard.getTokenProvider().name());
        trx.setPayerBankCardCardholderName(bankCard.getCardholderName());
        if (bankCard.getExpDate() != null) {
            trx.setPayerBankCardExpiredDateMonth(String.valueOf(bankCard.getExpDate().getMonth()));
            trx.setPayerBankCardExpiredDateYear(String.valueOf(bankCard.getExpDate().getYear()));
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
            throw new NotFoundException(
                    String.format("Payer type '%s' not found!", payer.getSetField().getFieldName()));
        }
    }

    public static ClearingRefund transformRefund(InvoicePaymentRefund invoicePaymentRefund,
                                                 MachineEvent event,
                                                 com.rbkmoney.damsel.domain.InvoicePayment payment,
                                                 Integer changeId,
                                                 int providerId) {
        var refund = invoicePaymentRefund.getRefund();

        ClearingRefund clearingRefund = new ClearingRefund();
        clearingRefund.setInvoiceId(event.getSourceId());
        clearingRefund.setSequenceId(event.getEventId());
        clearingRefund.setChangeId(changeId);
        clearingRefund.setPaymentId(payment.getId());
        clearingRefund.setRefundId(refund.getId());
        clearingRefund.setDomainRevision(refund.getDomainRevision());
        clearingRefund.setPartyId(payment.getOwnerId());
        clearingRefund.setShopId(payment.getShopId());
        clearingRefund.setCreatedAt(TypeUtil.stringToLocalDateTime(refund.getCreatedAt()));
        clearingRefund.setReason(refund.getReason());
        clearingRefund.setDomainRevision(refund.getDomainRevision());
        clearingRefund.setClearingState(TransactionClearingState.READY);
        clearingRefund.setTrxVersion(MappingUtils.DEFAULT_TRX_VERSION);
        fillRefundCashInfo(refund, clearingRefund);
        fillTransactionAdditionalInfo(invoicePaymentRefund, clearingRefund, event, providerId);

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
                                                      MachineEvent event,
                                                      int providerId) {
        var transactionInfo = invoicePaymentRefund.getSessions().stream()
                .findFirst()
                .map(InvoiceRefundSession::getTransactionInfo)
                .orElse(null);

        // Very often there are problems with sessions for test returns.
        // In order to avoid problems with receiving data, such returns are skipped.
        if (transactionInfo == null) {
            if (providerId == MOCKETBANK_PROVIDER_ID) {
                String emptyJson = "{}";
                clearingRefund.setExtra(emptyJson);
                clearingRefund.setTransactionId(EMPTY_STRING);
                return;
            }
            throw new NotFoundException(String.format("Refund session for refund (invoice id '%s'," +
                    "sequenceId id '%d') not found!", event.getSourceId(), event.getEventId()));
        }
        clearingRefund.setTransactionId(transactionInfo.getId());
        clearingRefund.setExtra(JsonUtil.objectToJsonString(transactionInfo.getExtra()));
    }

    public static boolean isExistProviderId(List<ClearingAdapter> adapters, int providerId) {
        List<Integer> providersIds = adapters.stream()
                .map(ClearingAdapter::getAdapterId)
                .collect(Collectors.toList());
        return providersIds.contains(providerId);
    }

}
