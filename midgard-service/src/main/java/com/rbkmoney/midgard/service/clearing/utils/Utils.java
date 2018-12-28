package com.rbkmoney.midgard.service.clearing.utils;

import com.rbkmoney.midgard.*;
import org.jooq.generated.midgard.tables.pojos.ClearingMerchant;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;

public final class Utils {

    public static Transaction transformTransaction(ClearingTransaction clrTran) {
        Transaction tran = new Transaction();
        GeneralTransactionInfo generalTranInfo = new GeneralTransactionInfo();
        generalTranInfo.setTransactionId(clrTran.getTransactionId());
        //TODO: перешнать в строку по отпределенному формату
        generalTranInfo.setTransactionDate(clrTran.getTransactionDate().toString());
        generalTranInfo.setTransactionAmount(clrTran.getTransactionAmount());
        generalTranInfo.setTransactionCurrency(clrTran.getTransactionCurrency());
        generalTranInfo.setMerchantId(clrTran.getMerchantId());
        generalTranInfo.setTerminalId(clrTran.getTerminalId());
        generalTranInfo.setMcc(clrTran.getMcc());
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

        return tran;
    }

    public static Merchant transaformMerchant(ClearingMerchant clearingMerchant) {
        Merchant merchant = new Merchant();
        merchant.setMerchantId(clearingMerchant.getMerchantId());
        merchant.setMerchantName(clearingMerchant.getMerchantName());
        merchant.setMerchantAddress(clearingMerchant.getMerchantAddress());
        merchant.setMerchantCity(clearingMerchant.getMerchantCity());
        merchant.setMerchantCountry(clearingMerchant.getMerchantCountry());
        merchant.setMerchantPostalCode(clearingMerchant.getMerchantPostalCode());
        return merchant;
    }

    public static boolean compareTransactions(ClearingTransaction trx1, ClearingTransaction trx2) {
        if (trx1 == trx2) {
            return true;
        }
        if (trx1 == null || trx2 == null) {
            return false;
        }
        if (!trx1.getTransactionId().equals(trx2.getTransactionId())) {
            return false;
        }
        if (!trx1.getTransactionDate().equals(trx2.getTransactionDate())) {
            return false;
        }
        if (!trx1.getTransactionAmount().equals(trx2.getTransactionAmount())) {
            return false;
        }
        if (!trx1.getTransactionCurrency().equals(trx2.getTransactionCurrency())) {
            return false;
        }
        if (!trx1.getProviderId().equals(trx2.getProviderId())) {
            return false;
        }
        if (!trx1.getMcc().equals(trx2.getMcc())) {
            return false;
        }
        if (!trx1.getMerchantId().equals(trx2.getMerchantId())) {
            return false;
        }
        if (!trx1.getTerminalId().equals(trx2.getTerminalId())) {
            return false;
        }

        return true;
    }

    private Utils() { }

}
