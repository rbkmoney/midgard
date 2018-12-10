package com.rbkmoney.midgard.clearing.utils;

import org.apache.commons.io.FileUtils;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;

import java.io.File;
import java.io.IOException;

/** Utility class for working with clearing objects */
public final class MidgardUtils {

    /** Cardholder “from” account type code. Value "00" means "Default – unspecified" */
    public static final String ACCOUNT_TYPE_FROM = "00";

    /**
     * A method to compare two transactions. It is necessary, because generated by
     * the jooq utility pojos compare all transaction fields, but it is not always necessary
     *
     * @param trx1 the first transaction
     * @param trx2 the second transaction
     * @return {@code true} if the first transaction equals the second transaction else {@code false}
     */
    public static boolean compareTransactions(ClearingTransaction trx1, ClearingTransaction trx2) {
        if (trx1 == trx2) {
            return true;
        }
        if ((trx1 == null && trx2 != null) || (trx1 != null && trx2 == null)) {
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
        if (!trx1.getBankName().equals(trx2.getBankName())) {
            return false;
        }
        if (!trx1.getTransactionType().equals(trx2.getTransactionType())) {
            return false;
        }
        if (!trx1.getAccountTypeFrom().equals(trx2.getAccountTypeFrom())) {
            return false;
        }
        if (!trx1.getAccountTypeTo().equals(trx2.getAccountTypeTo())) {
            return false;
        }
        if (!trx1.getCardCaptureCapability().equals(trx2.getCardCaptureCapability())) {
            return false;
        }
        if (!trx1.getCardDataInputCapability().equals(trx2.getCardDataInputCapability())) {
            return false;
        }
        if (!trx1.getCardDataInputMode().equals(trx2.getCardDataInputMode())) {
            return false;
        }
        if (!trx1.getCardDataOutputCapability().equals(trx2.getCardDataOutputCapability())) {
            return false;
        }
        if (!trx1.getCardPresence().equals(trx2.getCardPresence())) {
            return false;
        }
        if (!trx1.getCardholderAuthCapability().equals(trx2.getCardholderAuthCapability())) {
            return false;
        }
        if (!trx1.getCardholderAuthEntity().equals(trx2.getCardholderAuthEntity())) {
            return false;
        }
        if (!trx1.getCardholderAuthMethod().equals(trx2.getCardholderAuthMethod())) {
            return false;
        }
        if (!trx1.getCardholderPresence().equals(trx2.getCardholderPresence())) {
            return false;
        }
        if (!trx1.getMcc().equals(trx2.getMcc())) {
            return false;
        }
        if (!trx1.getMerchantId().equals(trx2.getMerchantId())) {
            return false;
        }
        if (!trx1.getMessageFunctionCode().equals(trx2.getMessageFunctionCode())) {
            return false;
        }
        if (!trx1.getMessageReasonCode().equals(trx2.getMessageReasonCode())) {
            return false;
        }
        if (!trx1.getMessageTypeIdentifier().equals(trx2.getMessageTypeIdentifier())) {
            return false;
        }
        if (!trx1.getOperationalEnvironment().equals(trx2.getOperationalEnvironment())) {
            return false;
        }
        if (!trx1.getPinCaptureCapability().equals(trx2.getPinCaptureCapability())) {
            return false;
        }
        if (!trx1.getTerminalDataOutputCapability().equals(trx2.getTerminalDataOutputCapability())) {
            return false;
        }
        if (!trx1.getTerminalId().equals(trx2.getTerminalId())) {
            return false;
        }

        return true;
    }

    /**
     * Save file
     *
     * @param fileName file name
     * @param value data
     */
    public static final void saveToFile(String fileName, String value) throws IOException {
        FileUtils.writeStringToFile(new File(fileName), value);
    }

    private MidgardUtils() { }

}
