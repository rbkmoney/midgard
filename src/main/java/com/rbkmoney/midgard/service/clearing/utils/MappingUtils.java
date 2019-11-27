package com.rbkmoney.midgard.service.clearing.utils;

import com.rbkmoney.midgard.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.enums.CashFlowAccount;
import org.jooq.generated.midgard.enums.ClearingTrxType;
import org.jooq.generated.midgard.enums.PaymentChangeType;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.*;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MappingUtils {

    public static final int DEFAULT_TRX_VERSION = 1;

    public static ClearingTransaction transformTransaction(Payment payment) {
        ClearingTransaction trx = new ClearingTransaction();
        trx.setSequenceId(payment.getSequenceId());
        trx.setChangeId(payment.getChangeId());
        trx.setInvoiceId(payment.getInvoiceId());
        trx.setPaymentId(payment.getPaymentId());
        trx.setProviderId(payment.getRouteProviderId());

        trx.setTransactionId(payment.getSessionPayloadTransactionBoundTrxId());
        trx.setTransactionDate(payment.getCreatedAt());
        trx.setTransactionAmount(payment.getAmount());
        trx.setTransactionCurrency(payment.getCurrencyCode());
        trx.setTransactionClearingState(TransactionClearingState.READY);

        trx.setPartyId(payment.getPartyId());
        trx.setShopId(payment.getShopId());

        trx.setPayerBankCardToken(payment.getPayerBankCardToken());
        trx.setPayerBankCardPaymentSystem(payment.getPayerBankCardPaymentSystem());
        trx.setPayerBankCardBin(payment.getPayerBankCardBin());
        trx.setPayerBankCardMaskedPan(payment.getPayerBankCardMaskedPan());
        trx.setPayerBankCardTokenProvider(payment.getPayerBankCardTokenProvider());
        trx.setExtra(payment.getSessionPayloadTransactionBoundTrxExtraJson());
        trx.setTrxVersion(DEFAULT_TRX_VERSION);
        trx.setIsRecurrent(payment.getMakeRecurrent());
        trx.setRouteTerminalId(payment.getRouteTerminalId());
        trx.setPayerType(payment.getPayerType() == null ? null : payment.getPayerType().getName());
        trx.setPayerRecurrentParentInvoiceId(payment.getPayerRecurrentParentInvoiceId());
        trx.setPayerRecurrentParentPaymentId(payment.getPayerRecurrentParentPaymentId());
        return trx;
    }

    public static Transaction transformRefundTransaction(ClearingTransaction clrTran,
                                                         List<ClearingTransactionCashFlow> cashFlowList,
                                                         ClearingRefund refund) {
        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo();
        generalTranInfo.setTransactionId(refund.getTransactionId());
        generalTranInfo.setTransactionDate(refund.getCreatedAt().toInstant(ZoneOffset.UTC).toString());
        generalTranInfo.setTransactionAmount(refund.getAmount());
        generalTranInfo.setTransactionCurrency(refund.getCurrencyCode());
        generalTranInfo.setInvoiceId(refund.getInvoiceId());
        generalTranInfo.setPaymentId(refund.getPaymentId());
        generalTranInfo.setTransactionType("REFUND");

        return fillAdditionalInfo(generalTranInfo, clrTran, refund.getExtra(), cashFlowList);
    }

    public static Transaction transformClearingTransaction(ClearingTransaction clrTran,
                                                           List<ClearingTransactionCashFlow> cashFlowList) {
        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo();
        generalTranInfo.setTransactionId(clrTran.getTransactionId());
        generalTranInfo.setTransactionDate(clrTran.getTransactionDate().toInstant(ZoneOffset.UTC).toString());
        generalTranInfo.setTransactionAmount(clrTran.getTransactionAmount());
        generalTranInfo.setTransactionCurrency(clrTran.getTransactionCurrency());
        generalTranInfo.setInvoiceId(clrTran.getInvoiceId());
        generalTranInfo.setPaymentId(clrTran.getPaymentId());
        generalTranInfo.setTransactionType("PAYMENT");

        return fillAdditionalInfo(generalTranInfo, clrTran, clrTran.getExtra(), cashFlowList);
    }

    private static Transaction fillAdditionalInfo(GeneralTransactionInfo generalTranInfo,
                                                  ClearingTransaction clrTran,
                                                  String extra,
                                                  List<ClearingTransactionCashFlow> cashFlowList) {
        Transaction transaction = new Transaction();
        transaction.setGeneralTransactionInfo(generalTranInfo);
        transaction.setTransactionCardInfo(getTransactionCardInfo(clrTran));
        transaction.setAdditionalTransactionData(transformContent(extra));
        transaction.setTransactionCashFlow(transformTranCashFlow(cashFlowList));

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

    private static List<TransactionCashFlow> transformTranCashFlow(List<ClearingTransactionCashFlow> cashFlowList) {
        List<TransactionCashFlow> transactionCashFlowList = new ArrayList<>();
        if (cashFlowList == null) {
            return transactionCashFlowList;
        }

        for (ClearingTransactionCashFlow cashFlow : cashFlowList) {
            transactionCashFlowList.add(transformCashFlow(cashFlow));
        }
        return transactionCashFlowList;
    }

    private static TransactionCashFlow transformCashFlow(ClearingTransactionCashFlow cashFlow) {
        TransactionCashFlow tranCashFlow = new TransactionCashFlow();
        tranCashFlow.setObjType(CashFlowChangeType.valueOf(cashFlow.getObjType().name()));
        tranCashFlow.setAmount(cashFlow.getAmount());
        tranCashFlow.setCurrencyCode(cashFlow.getCurrencyCode());

        tranCashFlow.setSourceAccountId(cashFlow.getSourceAccountId());
        tranCashFlow.setSourceAccountType(CashFlowAccountType.valueOf(cashFlow.getSourceAccountType().name()));
        tranCashFlow.setSourceAccountTypeValue(cashFlow.getSourceAccountTypeValue());

        tranCashFlow.setDestinationAccountId(cashFlow.getDestinationAccountId());
        tranCashFlow.setDestinationAccountType(
                CashFlowAccountType.valueOf(cashFlow.getDestinationAccountType().name()));
        tranCashFlow.setDestinationAccountTypeValue(cashFlow.getDestinationAccountTypeValue());
        return tranCashFlow;
    }

    public static List<ClearingTransactionCashFlow> transformCashFlow(List<CashFlow> cashFlowList,
                                                                      Long sourceEventId) {
        List<ClearingTransactionCashFlow> transactionCashFlowList = new ArrayList<>();
        for (CashFlow flow : cashFlowList) {
            transactionCashFlowList.add(transformCashFlow(flow, sourceEventId));
        }
        return transactionCashFlowList;
    }

    public static ClearingTransactionCashFlow transformCashFlow(CashFlow cashFlow, Long sourceEventId) {
        ClearingTransactionCashFlow tranCashFlow = new ClearingTransactionCashFlow();

        tranCashFlow.setSourceEventId(sourceEventId);
        tranCashFlow.setObjType(PaymentChangeType.valueOf(cashFlow.getObjType().name()));
        tranCashFlow.setAmount(cashFlow.getAmount());
        tranCashFlow.setCurrencyCode(cashFlow.getCurrencyCode());

        tranCashFlow.setSourceAccountId(cashFlow.getSourceAccountId());
        tranCashFlow.setSourceAccountType(CashFlowAccount.valueOf(cashFlow.getSourceAccountType().name()));
        tranCashFlow.setSourceAccountTypeValue(cashFlow.getSourceAccountTypeValue());

        tranCashFlow.setDestinationAccountId(cashFlow.getDestinationAccountId());
        tranCashFlow.setDestinationAccountType(CashFlowAccount.valueOf(cashFlow.getDestinationAccountType().name()));
        tranCashFlow.setDestinationAccountTypeValue(cashFlow.getDestinationAccountTypeValue());
        return tranCashFlow;
    }

    public static ClearingRefund transformRefund(Refund refund) {
        ClearingRefund clearingRefund = new ClearingRefund();
        clearingRefund.setSequenceId(refund.getSequenceId());
        clearingRefund.setChangeId(refund.getChangeId());
        clearingRefund.setInvoiceId(refund.getInvoiceId());
        clearingRefund.setPaymentId(refund.getPaymentId());
        clearingRefund.setRefundId(refund.getRefundId());
        clearingRefund.setTransactionId(refund.getSessionPayloadTransactionBoundTrxId());
        clearingRefund.setPartyId(refund.getPartyId());
        clearingRefund.setShopId(refund.getShopId());
        clearingRefund.setCreatedAt(refund.getCreatedAt());
        clearingRefund.setAmount(refund.getAmount());
        clearingRefund.setCurrencyCode(refund.getCurrencyCode());
        clearingRefund.setReason(refund.getReason());
        clearingRefund.setDomainRevision(refund.getDomainRevision());
        clearingRefund.setExtra(refund.getSessionPayloadTransactionBoundTrxExtraJson());
        clearingRefund.setClearingState(TransactionClearingState.READY);
        clearingRefund.setTrxVersion(DEFAULT_TRX_VERSION);
        return clearingRefund;
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
        eventTrxInfo.setRowNumber(trx.getId());
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
        eventTrxInfo.setRowNumber(refund.getId());
        eventTrxInfo.setProviderId(providerId);
        return eventTrxInfo;
    }

}
