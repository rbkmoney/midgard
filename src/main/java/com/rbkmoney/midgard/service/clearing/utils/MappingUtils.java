package com.rbkmoney.midgard.service.clearing.utils;

import com.rbkmoney.midgard.*;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.enums.CashFlowAccount;
import org.jooq.generated.midgard.enums.PaymentChangeType;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

//TODO: переделать на Spring Converter / MapStruct
public final class MappingUtils {

    public static ClearingTransaction transformTransaction(Payment payment) {
        ClearingTransaction trx = new ClearingTransaction();
        trx.setEventId(payment.getEventId());
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
        clearingRefund.setEventId(refund.getEventId());
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
        return clearingRefund;
    }

    private MappingUtils() { }

}
