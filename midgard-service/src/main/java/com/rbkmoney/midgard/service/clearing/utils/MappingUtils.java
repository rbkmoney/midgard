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
        //TODO: что то придумать с tran_id
        trx.setTransactionId(payment.getInvoiceId() + "_" + payment.getPaymentId());
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
        Transaction transaction = transformClearingTransaction(clrTran, cashFlowList);

        GeneralTransactionInfo generalTranInfo = transaction.getGeneralTransactionInfo();
        generalTranInfo.setTransactionDate(refund.getCreatedAt().toString());
        generalTranInfo.setTransactionAmount(refund.getAmount());
        generalTranInfo.setTransactionCurrency(refund.getCurrencyCode());

        return transaction;
    }

    public static Transaction transformClearingTransaction(ClearingTransaction clrTran,
                                                           List<ClearingTransactionCashFlow> cashFlowList) {
        Transaction tran = new Transaction();
        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo();
        generalTranInfo.setTransactionId(clrTran.getTransactionId());
        //TODO: перешнать в строку по отпределенному формату
        generalTranInfo.setTransactionDate(clrTran.getTransactionDate().toInstant(ZoneOffset.UTC).toString());
        generalTranInfo.setTransactionAmount(clrTran.getTransactionAmount());
        generalTranInfo.setTransactionCurrency(clrTran.getTransactionCurrency());
        generalTranInfo.setMcc(clrTran.getMcc() == null ? 0 : clrTran.getMcc());
        tran.setGeneralTransactionInfo(generalTranInfo);

        TransactionCardInfo tranCardInfo = new TransactionCardInfo();
        tranCardInfo.setPayerBankCardToken(clrTran.getPayerBankCardToken());
        tranCardInfo.setPayerBankCardBin(clrTran.getPayerBankCardBin());
        tranCardInfo.setPayerBankCardMaskedPan(clrTran.getPayerBankCardMaskedPan());
        tranCardInfo.setPayerBankCardPaymentSystem(clrTran.getPayerBankCardPaymentSystem());
        tranCardInfo.setPayerBankCardTokenProvider(clrTran.getPayerBankCardTokenProvider());
        tran.setTransactionCardInfo(tranCardInfo);

        Content additionalTranData = new Content();
        additionalTranData.setType("String");
        //TODO: Возможно так же стоит передавать строку, но не факт
        additionalTranData.setData(clrTran.getExtra().getBytes());

        tran.setAdditionalTransactionData(additionalTranData);

        List<TransactionCashFlow> transactionCashFlowList = new ArrayList<>();
        for (ClearingTransactionCashFlow cashFlow : cashFlowList) {
            transactionCashFlowList.add(transformCashFlow(cashFlow));
        }
        tran.setTransactionCashFlow(transactionCashFlowList);

        return tran;
    }

    private static TransactionCashFlow transformCashFlow(ClearingTransactionCashFlow cashFlow) {
        TransactionCashFlow tranCashFlow = new TransactionCashFlow();
        tranCashFlow.setAmount(cashFlow.getAmount());
        tranCashFlow.setCurrencyCode(cashFlow.getCurrencyCode());
        tranCashFlow.setSourceAccountId(cashFlow.getSourceAccountId());
        tranCashFlow.setSourceAccountType(CashFlowAccountType.valueOf(cashFlow.getSourceAccountType().name()));
        tranCashFlow.setSourceAccountTypeValue(cashFlow.getSourceAccountTypeValue());
        tranCashFlow.setDestinationAccountId(cashFlow.getDestinationAccountId());
        tranCashFlow.setDestinationAccountType(
                CashFlowAccountType.valueOf(cashFlow.getDestinationAccountType().name()));
        tranCashFlow.setDestinationAccountTypeValue(cashFlow.getDestinationAccountTypeValue());
        tranCashFlow.setObjType(CashFlowChangeType.valueOf(cashFlow.getObjType().name()));
        return tranCashFlow;
    }

    public static ClearingTransactionCashFlow transformCashFlow(CashFlow cashFlow) {
        ClearingTransactionCashFlow tranCashFlow = new ClearingTransactionCashFlow();
        tranCashFlow.setAmount(cashFlow.getAmount());
        tranCashFlow.setCurrencyCode(cashFlow.getCurrencyCode());
        tranCashFlow.setSourceAccountId(cashFlow.getSourceAccountId());
        tranCashFlow.setSourceAccountType(CashFlowAccount.valueOf(cashFlow.getSourceAccountType().name()));
        tranCashFlow.setSourceAccountTypeValue(cashFlow.getSourceAccountTypeValue());
        tranCashFlow.setDestinationAccountId(cashFlow.getDestinationAccountId());
        tranCashFlow.setDestinationAccountType(CashFlowAccount.valueOf(cashFlow.getDestinationAccountType().name()));
        tranCashFlow.setDestinationAccountTypeValue(cashFlow.getDestinationAccountTypeValue());
        tranCashFlow.setObjType(PaymentChangeType.valueOf(cashFlow.getObjType().name()));
        return tranCashFlow;
    }

    public static ClearingRefund transformRefund(Refund refund) {
        ClearingRefund clearingRefund = new ClearingRefund();
        clearingRefund.setEventId(refund.getEventId());
        clearingRefund.setInvoiceId(refund.getInvoiceId());
        clearingRefund.setPaymentId(refund.getPaymentId());
        clearingRefund.setPartyId(refund.getPartyId());
        clearingRefund.setShopId(refund.getShopId());
        clearingRefund.setCreatedAt(refund.getCreatedAt());
        clearingRefund.setAmount(refund.getAmount());
        clearingRefund.setCurrencyCode(refund.getCurrencyCode());
        clearingRefund.setReason(refund.getReason());
        clearingRefund.setDomainRevision(refund.getDomainRevision());
        clearingRefund.setClearingState(TransactionClearingState.READY);
        return clearingRefund;
    }

    private MappingUtils() { }

}
