package com.rbkmoney.midgard.service.check;

import com.rbkmoney.damsel.domain.InvoicePaymentCaptured;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.config.props.ClearingServiceProperties;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.INSTANT_DATE_TIME;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.LOCAL_DATE_TIME;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.PAYMENT_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.TRANSACTION_ID_1;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CheckTransactionTypeTest {

    public static final int PROVIDER_ID_HAS_AFT = 1;
    public static final int PROVIDER_ID_DONT_HAS_AFT = 2;

    private CheckTransactionType checkTransactionType;

    @BeforeEach
    public void before() {
        List<ClearingAdapter> clearingAdapters = createClearingAdapters();
        checkTransactionType = new CheckTransactionType(clearingAdapters);
    }

    @Test
    public void isTypeTransactionToSkippedTrueTest() {
        boolean checkType = checkType(true, PROVIDER_ID_HAS_AFT);
        assertTrue(checkType);

        checkType = checkType(false, PROVIDER_ID_HAS_AFT);
        assertFalse(checkType);

        checkType = checkType(false, PROVIDER_ID_DONT_HAS_AFT);
        assertFalse(checkType);

        checkType = checkType(false, PROVIDER_ID_DONT_HAS_AFT);
        assertFalse(checkType);
    }

    private boolean checkType(boolean onTransactionType, int providerId) {
        return checkTransactionType.isTypeTransactionToSkipped(
                createInvoicePayment(createTrxExtraMap(onTransactionType)),
                InvoiceTestConstant.INVOICE_ID_1,
                InvoiceTestConstant.CHANGE_ID_1,
                InvoiceTestConstant.SEQUENCE_ID_1,
                providerId
        );
    }

    private List<ClearingAdapter> createClearingAdapters() {
        List<ClearingAdapter> clearingAdapters = new ArrayList<>();
        clearingAdapters.add(createClearingAdapter(
                "BANK_1",
                PROVIDER_ID_HAS_AFT,
                createExcludeTransactions(new String[] {"AFT"}))
        );
        clearingAdapters.add(createClearingAdapter(
                "TEST",
                PROVIDER_ID_DONT_HAS_AFT,
                createExcludeTransactions(null))
        );
        return clearingAdapters;
    }

    @NotNull
    private ClearingServiceProperties.ExcludeTransactions createExcludeTransactions(String[] excludeTraansaction) {
        ClearingServiceProperties.ExcludeTransactions
                excludeTransactions = new ClearingServiceProperties.ExcludeTransactions();
        if (excludeTraansaction != null) {
            excludeTransactions.setTypes(excludeTraansaction);
        }
        return excludeTransactions;
    }

    private ClearingAdapter createClearingAdapter(
            String adapterName,
            int adapterId,
            ClearingServiceProperties.ExcludeTransactions excludeTransactions
    ) {
        ClearingAdapterSrv.Iface adapter = mock(ClearingAdapterSrv.Iface.class);
        return new ClearingAdapter(adapter, adapterName, adapterId, 1000, excludeTransactions);
    }

    public static InvoicePayment createInvoicePayment(Map<String, String> trxExtra) {
        var domainInvoicePayment = new com.rbkmoney.damsel.domain.InvoicePayment()
                .setId(PAYMENT_ID_1)
                .setCreatedAt(INSTANT_DATE_TIME.toString())
                .setStatus(InvoicePaymentStatus.captured(new InvoicePaymentCaptured()));

        return new InvoicePayment()
                .setPayment(domainInvoicePayment)
                .setSessions(createInvoicePaymentSessions(trxExtra));
    }

    private static List<InvoicePaymentSession> createInvoicePaymentSessions(Map<String, String> trxExtra) {
        List<InvoicePaymentSession> sessionList = new ArrayList<>();
        sessionList.add(createInvoicePaymentSession(trxExtra));
        return sessionList;
    }

    private static InvoicePaymentSession createInvoicePaymentSession(Map<String, String> trxExtra) {
        return new InvoicePaymentSession()
                .setTargetStatus(createPaymentStatusCaptured())
                .setTransactionInfo(createTransactionInfo(TRANSACTION_ID_1, trxExtra));
    }

    @NotNull
    private static TargetInvoicePaymentStatus createPaymentStatusCaptured() {
        return TargetInvoicePaymentStatus.captured(new InvoicePaymentCaptured());
    }

    private static TransactionInfo createTransactionInfo(String transactionId, Map<String, String> trxExtra) {
        return new TransactionInfo()
                .setId(transactionId)
                .setTimestamp(LOCAL_DATE_TIME.toString())
                .setExtra(trxExtra);
    }

    private static Map<String, String> createTrxExtraMap(boolean onTransactionType) {
        Map<String, String> extra = new HashMap<>();
        if (onTransactionType) {
            extra.put("transaction_type", "aft");
        }
        return extra;
    }

}