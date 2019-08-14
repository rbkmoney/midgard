package com.rbkmoney.midgard.base.tests.unit.data;

import com.rbkmoney.midgard.*;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public final class TestTransactionsData {

    private static final LocalDateTime dateTIme =
            LocalDateTime.of(2019, 01, 12, 12, 12, 44);

    public static Payment getTestPayment() {
        Payment payment = new Payment();

        payment.setSequenceId(1L);
        payment.setEventCreatedAt(dateTIme);
        payment.setInvoiceId("invoice_1");
        payment.setPaymentId("payment_1");
        payment.setRouteProviderId(1);
        payment.setCreatedAt(dateTIme);
        payment.setAmount(1000L);
        payment.setCurrencyCode("RUB");
        payment.setPartyId("pt_1");
        payment.setShopId("sh_1");

        payment.setPayerBankCardToken("token_1");
        payment.setPayerBankCardPaymentSystem("visa");
        payment.setPayerBankCardBin("454545");
        payment.setPayerBankCardMaskedPan("4545 45** **** 9809");
        payment.setPayerBankCardTokenProvider("provider_1");
        payment.setSessionPayloadTransactionBoundTrxExtraJson("extra json");
        payment.setSessionPayloadTransactionBoundTrxId("invoice_1_payment_1");

        return payment;
    }

    public static ClearingTransaction getTestClearingTransaction() {
        ClearingTransaction trx = new ClearingTransaction();

        trx.setSequenceId(1L);
        trx.setInvoiceId("invoice_1");
        trx.setPaymentId("payment_1");
        trx.setProviderId(1);
        trx.setTransactionId(trx.getInvoiceId() + "_" + trx.getPaymentId());
        trx.setTransactionDate(dateTIme);
        trx.setTransactionAmount(1000L);
        trx.setTransactionCurrency("RUB");
        trx.setTransactionClearingState(TransactionClearingState.READY);
        trx.setPartyId("pt_1");
        trx.setShopId("sh_1");

        trx.setPayerBankCardToken("token_1");
        trx.setPayerBankCardPaymentSystem("visa");
        trx.setPayerBankCardBin("454545");
        trx.setPayerBankCardMaskedPan("4545 45** **** 9809");
        trx.setPayerBankCardTokenProvider("provider_1");
        trx.setExtra("extra json");
        trx.setTrxVersion(1);

        return trx;
    }

    public static CashFlow getTestCashFlow() {
        CashFlow cashFlow = new CashFlow();

        cashFlow.setAmount(1000L);
        cashFlow.setCurrencyCode("RUB");
        cashFlow.setSourceAccountId(1L);
        cashFlow.setSourceAccountType(org.jooq.generated.feed.enums.CashFlowAccount.merchant);
        cashFlow.setSourceAccountTypeValue("1000");
        cashFlow.setDestinationAccountId(2L);
        cashFlow.setDestinationAccountType(org.jooq.generated.feed.enums.CashFlowAccount.system);
        cashFlow.setDestinationAccountTypeValue("20");
        cashFlow.setObjType(org.jooq.generated.feed.enums.PaymentChangeType.payment);

        return cashFlow;
    }

    public static ClearingTransactionCashFlow getTestClearingTransactionCashFlow() {
        ClearingTransactionCashFlow tranCashFlow = new ClearingTransactionCashFlow();

        tranCashFlow.setAmount(1000L);
        tranCashFlow.setCurrencyCode("RUB");
        tranCashFlow.setSourceAccountId(1L);
        tranCashFlow.setSourceAccountType(org.jooq.generated.midgard.enums.CashFlowAccount.merchant);
        tranCashFlow.setSourceAccountTypeValue("1000");
        tranCashFlow.setDestinationAccountId(2L);
        tranCashFlow.setDestinationAccountType(org.jooq.generated.midgard.enums.CashFlowAccount.system);
        tranCashFlow.setDestinationAccountTypeValue("20");
        tranCashFlow.setObjType(org.jooq.generated.midgard.enums.PaymentChangeType.payment);

        return tranCashFlow;
    }

    public static TransactionCashFlow getTestTransactionCashFlow() {
        TransactionCashFlow tranCashFlow = new TransactionCashFlow();

        tranCashFlow.setAmount(1000L);
        tranCashFlow.setCurrencyCode("RUB");
        tranCashFlow.setSourceAccountId(1L);
        tranCashFlow.setSourceAccountType(CashFlowAccountType.merchant);
        tranCashFlow.setSourceAccountTypeValue("1000");
        tranCashFlow.setDestinationAccountId(2L);
        tranCashFlow.setDestinationAccountType(CashFlowAccountType.system);
        tranCashFlow.setDestinationAccountTypeValue("20");
        tranCashFlow.setObjType(CashFlowChangeType.payment);

        return tranCashFlow;
    }

    public static Transaction getTestProtoTransaction() {
        Transaction trx = new Transaction();

        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo();
        generalTranInfo.setTransactionId("invoice_1_payment_1");
        generalTranInfo.setTransactionDate(dateTIme.toInstant(ZoneOffset.UTC).toString());
        generalTranInfo.setTransactionAmount(1000L);
        generalTranInfo.setTransactionCurrency("RUB");
        generalTranInfo.setTransactionType("PAYMENT");
        generalTranInfo.setInvoiceId("invoice_1");
        generalTranInfo.setPaymentId("payment_1");
        trx.setGeneralTransactionInfo(generalTranInfo);

        TransactionCardInfo tranCardInfo = new TransactionCardInfo();
        tranCardInfo.setPayerBankCardToken("token_1");
        tranCardInfo.setPayerBankCardBin("454545");
        tranCardInfo.setPayerBankCardMaskedPan("4545 45** **** 9809");
        tranCardInfo.setPayerBankCardPaymentSystem("visa");
        tranCardInfo.setPayerBankCardTokenProvider("provider_1");
        trx.setTransactionCardInfo(tranCardInfo);

        Content additionalTranData = new Content();
        additionalTranData.setType("application/json");
        //TODO: Возможно так же стоит передавать строку, но не факт
        additionalTranData.setData("extra json".getBytes());

        trx.setAdditionalTransactionData(additionalTranData);
        List<TransactionCashFlow> transactionCashFlowList = new ArrayList<>();
        transactionCashFlowList.add(getTestTransactionCashFlow());
        trx.setTransactionCashFlow(transactionCashFlowList);

        return trx;
    }

    public static Refund getTestRefund() {
        Refund refund = new Refund();

        refund.setSequenceId(3L);
        refund.setInvoiceId("invoice_3");
        refund.setPaymentId("payment_3");
        refund.setSessionPayloadTransactionBoundTrxId("tran_id_3");
        refund.setPartyId("pt_3");
        refund.setShopId("sh_3");
        refund.setCreatedAt(dateTIme);
        refund.setAmount(1000L);
        refund.setCurrencyCode("RUB");
        refund.setReason("some reason");
        refund.setDomainRevision(1L);
        refund.setSessionPayloadTransactionBoundTrxExtraJson("extra json");

        return refund;
    }

    public static ClearingRefund getTestClearingRefund() {
        ClearingRefund clearingRefund = new ClearingRefund();

        clearingRefund.setSequenceId(3L);
        clearingRefund.setInvoiceId("invoice_3");
        clearingRefund.setPaymentId("payment_3");
        clearingRefund.setTransactionId("tran_id_3");
        clearingRefund.setPartyId("pt_3");
        clearingRefund.setShopId("sh_3");
        clearingRefund.setCreatedAt(dateTIme);
        clearingRefund.setAmount(1000L);
        clearingRefund.setCurrencyCode("RUB");
        clearingRefund.setReason("some reason");
        clearingRefund.setDomainRevision(1L);
        clearingRefund.setClearingState(TransactionClearingState.READY);
        clearingRefund.setExtra("extra json");
        clearingRefund.setTrxVersion(1);

        return clearingRefund;
    }

    private TestTransactionsData() {}

}
