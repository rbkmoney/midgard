package com.rbkmoney.midgard.test.unit.data;

import com.rbkmoney.midgard.Content;
import com.rbkmoney.midgard.GeneralTransactionInfo;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.TransactionCardInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestTransactionsData {

    private static final LocalDateTime dateTIme =
            LocalDateTime.of(2019, 01, 12, 12, 12, 44);

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
        additionalTranData.setData("extra json".getBytes());

        trx.setAdditionalTransactionData(additionalTranData);
        trx.setTransactionCashFlow(new ArrayList<>());

        return trx;
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

}
