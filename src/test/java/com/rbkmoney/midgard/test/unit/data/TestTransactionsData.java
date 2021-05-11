package com.rbkmoney.midgard.test.unit.data;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.damsel.payment_processing.InvoiceRefundSession;
import com.rbkmoney.midgard.BankCardExpDate;
import com.rbkmoney.midgard.Content;
import com.rbkmoney.midgard.GeneralTransactionInfo;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.TransactionCardInfo;
import com.rbkmoney.midgard.domain.enums.TransactionClearingState;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingRefund;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingTransaction;
import com.rbkmoney.midgard.utils.JsonUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.BANK_NAME;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CARDHOLDER_NAME;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CARD_BIN;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CARD_EXP_DATE_MONTH;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CARD_EXP_DATE_YEAR;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CARD_LAST_DIGIT;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CARD_MASKED_PAN;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CARD_PAYMENT_SYSTEM;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CARD_TOKEN;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CHANGE_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CONTENT_TYPE;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.DOMAIN_REVISION;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.INSTANT_DATE_TIME;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.INVOICE_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.INVOICE_PAYMENT_1_AMOUNT;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.INVOICE_PAYMENT_1_CURRENCY;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.INVOICE_REFUND_1_AMOUNT;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.INVOICE_REFUND_1_CURRENCY;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.LOCAL_DATE_TIME;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.PARTY_ID;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.PAYMENT_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.PROVIDER_ID;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.REFUND_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.REFUND_REASON_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.REFUND_TRANSACTION_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.SEQUENCE_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.SHOP_ID;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.TERMINAL_ID;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.TRANSACTION_ID_1;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestTransactionsData {

    public static Invoice getInvoice() {
        Invoice invoice = new Invoice();
        invoice.setInvoice(getTestDomainInvoice());
        invoice.setPayments(getTestInvoicePaymentList());
        return invoice;
    }

    private static com.rbkmoney.damsel.domain.Invoice getTestDomainInvoice() {
        return new com.rbkmoney.damsel.domain.Invoice()
                .setId(INVOICE_ID_1)
                .setCreatedAt(INSTANT_DATE_TIME.toString())
                .setCost(getCash(INVOICE_PAYMENT_1_AMOUNT, INVOICE_PAYMENT_1_CURRENCY))
                .setContext(getContent());
    }

    private static List<InvoicePayment> getTestInvoicePaymentList() {
        List<InvoicePayment> invoicePayments = new ArrayList<>();
        invoicePayments.add(getInvoicePayment());
        return invoicePayments;
    }

    public static InvoicePayment getInvoicePayment() {
        var domainInvoicePayment = new com.rbkmoney.damsel.domain.InvoicePayment()
                .setId(PAYMENT_ID_1)
                .setCreatedAt(INSTANT_DATE_TIME.toString())
                .setStatus(InvoicePaymentStatus.captured(new InvoicePaymentCaptured()))
                .setDomainRevision(1)
                .setMakeRecurrent(false)
                .setCost(getCash(INVOICE_PAYMENT_1_AMOUNT, INVOICE_PAYMENT_1_CURRENCY))
                .setContext(getContent())
                .setPayer(getTestPayer())
                .setOwnerId(PARTY_ID)
                .setShopId(SHOP_ID);

        return new InvoicePayment()
                .setPayment(domainInvoicePayment)
                .setRoute(getPaymentRoute())
                .setSessions(getInvoicePaymentSessions())
                .setRefunds(getRefunds());
    }

    private static Payer getTestPayer() {
        PaymentTool paymentTool = new PaymentTool();
        paymentTool.setBankCard(getTestBankCard());
        Payer payer = new Payer();
        payer.setCustomer(new CustomerPayer().setPaymentTool(paymentTool));
        return payer;
    }

    private static BankCard getTestBankCard() {
        return new BankCard()
                .setToken(CARD_TOKEN)
                .setBin(CARD_BIN)
                .setBankName(BANK_NAME)
                .setCardholderName(CARDHOLDER_NAME)
                .setPaymentSystemDeprecated(CARD_PAYMENT_SYSTEM)
                .setPaymentSystem(new PaymentSystemRef().setId("1"))
                .setBin(CARD_BIN)
                .setLastDigits(CARD_LAST_DIGIT)
                .setExpDate(new com.rbkmoney.damsel.domain.BankCardExpDate(
                        Byte.valueOf(CARD_EXP_DATE_MONTH),
                        Short.valueOf(CARD_EXP_DATE_YEAR))
                );
    }

    private static List<InvoicePaymentSession> getInvoicePaymentSessions() {
        List<InvoicePaymentSession> sessionList = new ArrayList<>();
        sessionList.add(getTestInvoicePaymentSession());
        return sessionList;
    }

    private static InvoicePaymentSession getTestInvoicePaymentSession() {
        return new InvoicePaymentSession()
                .setTargetStatus(
                        com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus.captured(
                                new InvoicePaymentCaptured()
                        )
                )
                .setTransactionInfo(getTransactionInfo(TRANSACTION_ID_1));
    }

    private static Map<String, String> getTestExtraMap() {
        Map<String, String> extra = new HashMap<>();
        extra.put("key", "value");
        return extra;
    }

    private static List<com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund> getRefunds() {
        List<com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund> refundList = new ArrayList<>();
        refundList.add(getInvoicePaymentRefund());
        return refundList;
    }

    public static com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund getInvoicePaymentRefund() {
        var refund = new com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund();
        refund.setRefund(new InvoicePaymentRefund()
                .setCreatedAt(INSTANT_DATE_TIME.toString())
                .setReason(REFUND_REASON_1)
                .setId(REFUND_ID_1)
                .setCash(getCash(INVOICE_REFUND_1_AMOUNT, INVOICE_REFUND_1_CURRENCY))
                .setDomainRevision(DOMAIN_REVISION)
                .setStatus(InvoicePaymentRefundStatus.succeeded(new InvoicePaymentRefundSucceeded())));
        refund.setSessions(getInvoiceRefundSessions());
        return refund;
    }

    private static List<InvoiceRefundSession> getInvoiceRefundSessions() {
        List<InvoiceRefundSession> invoiceRefundSessions = new ArrayList<>();
        InvoiceRefundSession invoiceRefundSession = new InvoiceRefundSession();
        invoiceRefundSession.setTransactionInfo(getTransactionInfo(REFUND_TRANSACTION_ID_1));
        invoiceRefundSessions.add(invoiceRefundSession);
        return invoiceRefundSessions;
    }

    private static TransactionInfo getTransactionInfo(String transactionId) {
        return new TransactionInfo()
                .setId(transactionId)
                .setTimestamp(LOCAL_DATE_TIME.toString())
                .setExtra(getTestExtraMap());
    }

    private static PaymentRoute getPaymentRoute() {
        return new PaymentRoute()
                .setProvider(new ProviderRef().setId(PROVIDER_ID))
                .setTerminal(new TerminalRef().setId(TERMINAL_ID));
    }

    private static com.rbkmoney.damsel.base.Content getContent() {
        return new com.rbkmoney.damsel.base.Content()
                .setType(CONTENT_TYPE)
                .setData("test".getBytes());
    }

    private static Cash getCash(long amount, String currency) {
        return new Cash()
                .setAmount(amount)
                .setCurrency(new CurrencyRef()
                        .setSymbolicCode(currency));
    }

    public static ClearingTransaction getTestClearingTransaction() {
        ClearingTransaction trx = new ClearingTransaction();

        trx.setSequenceId(SEQUENCE_ID_1);
        trx.setChangeId(CHANGE_ID_1);
        trx.setInvoiceId(INVOICE_ID_1);
        trx.setPaymentId(PAYMENT_ID_1);
        trx.setProviderId(PROVIDER_ID);
        trx.setTransactionId(TRANSACTION_ID_1);
        trx.setTransactionDate(LOCAL_DATE_TIME);
        trx.setTransactionAmount(INVOICE_PAYMENT_1_AMOUNT);
        trx.setTransactionCurrency(INVOICE_PAYMENT_1_CURRENCY);
        trx.setTransactionClearingState(TransactionClearingState.READY);
        trx.setPartyId(PARTY_ID);
        trx.setShopId(SHOP_ID);
        trx.setIsRecurrent(false);
        trx.setRouteTerminalId(TERMINAL_ID);
        trx.setPayerType("customer");

        trx.setPayerBankCardToken(CARD_TOKEN);
        trx.setPayerBankCardPaymentSystem("1");
        trx.setPayerBankCardBin(CARD_BIN);
        trx.setPayerBankCardMaskedPan(CARD_MASKED_PAN);
        trx.setPayerBankCardExpiredDateMonth(CARD_EXP_DATE_MONTH);
        trx.setPayerBankCardExpiredDateYear(CARD_EXP_DATE_YEAR);
        trx.setPayerBankCardCardholderName(CARDHOLDER_NAME);
        //trx.setPayerBankCardTokenProvider(CARD_TOKEN_PROVIDER);

        trx.setExtra(JsonUtil.objectToJsonString(getTestExtraMap()));
        trx.setTrxVersion(1);

        return trx;
    }

    public static Transaction getTestProtoTransaction() {
        Transaction trx = new Transaction();

        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo();
        generalTranInfo.setTransactionId(TRANSACTION_ID_1);
        generalTranInfo.setTransactionDate(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC).toString());
        generalTranInfo.setTransactionAmount(INVOICE_PAYMENT_1_AMOUNT);
        generalTranInfo.setTransactionCurrency(INVOICE_PAYMENT_1_CURRENCY);
        generalTranInfo.setTransactionType("PAYMENT");
        generalTranInfo.setInvoiceId(INVOICE_ID_1);
        generalTranInfo.setPaymentId(PAYMENT_ID_1);
        trx.setGeneralTransactionInfo(generalTranInfo);

        TransactionCardInfo tranCardInfo = new TransactionCardInfo();
        tranCardInfo.setPayerBankCardToken(CARD_TOKEN);
        tranCardInfo.setPayerBankCardBin(CARD_BIN);
        tranCardInfo.setPayerBankCardMaskedPan(CARD_MASKED_PAN);
        tranCardInfo.setPayerBankCardPaymentSystem("1");
        tranCardInfo.setPayerBankCardCardholderName(CARDHOLDER_NAME);
        tranCardInfo.setPayerBankCardExpDate(createPayerBankCardExpDate());
        trx.setTransactionCardInfo(tranCardInfo);

        Content additionalTranData = new Content();
        additionalTranData.setType(CONTENT_TYPE);
        additionalTranData.setData(JsonUtil.objectToJsonString(getTestExtraMap()).getBytes());

        trx.setAdditionalTransactionData(additionalTranData);
        trx.setTransactionCashFlow(new ArrayList<>());

        return trx;
    }

    public static Transaction getTestProtoRefundTransaction() {
        Transaction trx = new Transaction();

        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo();
        generalTranInfo.setTransactionId(REFUND_TRANSACTION_ID_1);
        generalTranInfo.setTransactionDate(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC).toString());
        generalTranInfo.setTransactionAmount(INVOICE_REFUND_1_AMOUNT);
        generalTranInfo.setTransactionCurrency(INVOICE_REFUND_1_CURRENCY);
        generalTranInfo.setTransactionType("REFUND");
        generalTranInfo.setInvoiceId(INVOICE_ID_1);
        generalTranInfo.setPaymentId(PAYMENT_ID_1);
        trx.setGeneralTransactionInfo(generalTranInfo);

        TransactionCardInfo tranCardInfo = new TransactionCardInfo();
        tranCardInfo.setPayerBankCardToken(CARD_TOKEN);
        tranCardInfo.setPayerBankCardBin(CARD_BIN);
        tranCardInfo.setPayerBankCardMaskedPan(CARD_MASKED_PAN);
        tranCardInfo.setPayerBankCardPaymentSystem("1");
        tranCardInfo.setPayerBankCardCardholderName(CARDHOLDER_NAME);
        tranCardInfo.setPayerBankCardExpDate(createPayerBankCardExpDate());
        trx.setTransactionCardInfo(tranCardInfo);

        Content additionalTranData = new Content();
        additionalTranData.setType(CONTENT_TYPE);
        additionalTranData.setData(JsonUtil.objectToJsonString(getTestExtraMap()).getBytes());

        trx.setAdditionalTransactionData(additionalTranData);
        trx.setTransactionCashFlow(new ArrayList<>());

        return trx;
    }

    public static ClearingRefund getTestClearingRefund() {
        ClearingRefund clearingRefund = new ClearingRefund();

        clearingRefund.setSequenceId(SEQUENCE_ID_1);
        clearingRefund.setChangeId(CHANGE_ID_1);
        clearingRefund.setInvoiceId(INVOICE_ID_1);
        clearingRefund.setPaymentId(PAYMENT_ID_1);
        clearingRefund.setRefundId(REFUND_ID_1);
        clearingRefund.setTransactionId(REFUND_TRANSACTION_ID_1);
        clearingRefund.setPartyId(PARTY_ID);
        clearingRefund.setShopId(SHOP_ID);
        clearingRefund.setCreatedAt(LOCAL_DATE_TIME);
        clearingRefund.setAmount(INVOICE_REFUND_1_AMOUNT);
        clearingRefund.setCurrencyCode(INVOICE_REFUND_1_CURRENCY);
        clearingRefund.setReason(REFUND_REASON_1);
        clearingRefund.setDomainRevision(DOMAIN_REVISION);
        clearingRefund.setClearingState(TransactionClearingState.READY);
        clearingRefund.setExtra(JsonUtil.objectToJsonString(getTestExtraMap()));
        clearingRefund.setTrxVersion(1);

        return clearingRefund;
    }

    private static BankCardExpDate createPayerBankCardExpDate() {
        return new BankCardExpDate()
                .setMonth(Byte.valueOf(CARD_EXP_DATE_MONTH))
                .setYear(Short.valueOf(CARD_EXP_DATE_YEAR));
    }

}
