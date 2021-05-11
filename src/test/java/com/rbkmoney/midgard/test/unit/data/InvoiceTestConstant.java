package com.rbkmoney.midgard.test.unit.data;

import com.rbkmoney.damsel.domain.LegacyBankCardPaymentSystem;
import com.rbkmoney.geck.common.util.TypeUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InvoiceTestConstant {

    public static final Instant INSTANT_DATE_TIME = Instant.now();
    public static final LocalDateTime LOCAL_DATE_TIME = TypeUtil.stringToLocalDateTime(INSTANT_DATE_TIME.toString());

    public static final String INVOICE_ID_1 = "invoice_1";
    public static final String PAYMENT_ID_1 = "payment_1";
    public static final String REFUND_ID_1 = "refund_1";
    public static final String TRANSACTION_ID_1 = "transaction_1";
    public static final String REFUND_TRANSACTION_ID_1 = "refund_transaction_1";
    public static final long INVOICE_PAYMENT_1_AMOUNT = 1000L;
    public static final long INVOICE_REFUND_1_AMOUNT = 1000L;
    public static final String INVOICE_PAYMENT_1_CURRENCY = "USD";
    public static final String INVOICE_REFUND_1_CURRENCY = "RUB";
    public static final String REFUND_REASON_1 = "Refund reason";
    public static final long DOMAIN_REVISION = 1L;
    public static final int PROVIDER_ID = 115;
    public static final int TERMINAL_ID = 2025;
    public static final long SEQUENCE_ID_1 = 9103L;
    public static final int CHANGE_ID_1 = 0;
    public static final String PARTY_ID = "Party-1";
    public static final String SHOP_ID = "Shop-1";

    public static final String CARD_TOKEN = "Card-Token-001";
    public static final LegacyBankCardPaymentSystem CARD_PAYMENT_SYSTEM = LegacyBankCardPaymentSystem.visa;
    public static final String CARD_PAYMENT_SYSTEM_REF = "1";
    public static final String CARD_BIN = "443322";
    public static final String CARDHOLDER_NAME = "443322";
    public static final String CARD_MASKED_PAN = "443322******1234";
    public static final String CARD_LAST_DIGIT = "1234";
    public static final String CARD_EXP_DATE_MONTH = "12";
    public static final String CARD_EXP_DATE_YEAR = "2020";
    public static final String CARD_TOKEN_PROVIDER = "Card-Token-Provider-001";
    public static final String BANK_NAME = "BankName007";

    public static final String CONTENT_TYPE = "application/json";

}
