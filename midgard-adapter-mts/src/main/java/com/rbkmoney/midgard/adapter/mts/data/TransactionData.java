package com.rbkmoney.midgard.adapter.mts.data;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionData {

    private String                        providerId;
    private String                        transactionId;
    private LocalDateTime                 transactionDate;
    private Long                          transactionAmount;
    private String                        transactionCurrency;
    private String                        transactionType;
    private String                        payerBankCardToken;
    private String                        payerBankCardPaymentSystem;
    private String                        payerBankCardBin;
    private String                        payerBankCardMaskedPan;
    private String                        payerBankCardTokenProvider;
    private String                        accountTypeFrom;
    private String                        accountTypeTo;
    private String                        approvalCode;
    private String                        cardCaptureCapability;
    private String                        cardDataInputCapability;
    private String                        cardDataInputMode;
    private String                        cardDataOutputCapability;
    private String                        cardPresence;
    private String                        cardholderAuthCapability;
    private String                        cardholderAuthEntity;
    private String                        cardholderAuthMethod;
    private String                        cardholderPresence;
    private String                        eCommerceSecurityLevel;
    private Integer                       mcc;
    private String                        merchantId;
    private Integer                       messageFunctionCode;
    private Integer                       messageReasonCode;
    private Integer                       messageTypeIdentifier;
    private String                        operationalEnvironment;
    private String                        pinCaptureCapability;
    private String                        terminalDataOutputCapability;
    private String                        terminalId;
    private String                        rrn;
    private String                        responseCode;
    private String                        systemTraceAuditNumber;
    private Long                          clearingId;

}
