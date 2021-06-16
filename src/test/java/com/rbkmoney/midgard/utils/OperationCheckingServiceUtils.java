package com.rbkmoney.midgard.utils;

import com.rbkmoney.damsel.domain.InvoicePaymentCaptured;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.PaymentRoute;
import com.rbkmoney.damsel.domain.Provider;
import com.rbkmoney.damsel.domain.ProviderRef;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.config.props.ClearingServiceProperties;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.service.check.OperationCheckingService;
import com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.INSTANT_DATE_TIME;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.LOCAL_DATE_TIME;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.PAYMENT_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.TRANSACTION_ID_1;
import static org.mockito.Mockito.mock;

public class OperationCheckingServiceUtils {

    public static InvoicePayment createInvoicePayment(boolean onTransactionType, int providerId) {
        InvoicePayment invoicePayment = OperationCheckingServiceUtils.createInvoicePayment(
                OperationCheckingServiceUtils.createTrxExtraMap(onTransactionType)
        );
        invoicePayment.setRoute(OperationCheckingServiceUtils.extractedPaymentRoute(providerId));
        return invoicePayment;
    }

    public static PaymentRoute extractedPaymentRoute(int providerId) {
        PaymentRoute paymentRoute = new PaymentRoute();
        paymentRoute.setProvider(extractedProvider(providerId));
        return paymentRoute;
    }

    public static ProviderRef extractedProvider(int providerId) {
        ProviderRef provider = new ProviderRef();
        provider.setId(providerId);
        return provider;
    }

    public static List<ClearingAdapter> createClearingAdapters() {
        List<ClearingAdapter> clearingAdapters = new ArrayList<>();
        clearingAdapters.add(createClearingAdapter(
                "BANK_1",
                InvoiceTestConstant.PROVIDER_ID_HAS_AFT,
                createExcludeTransactions(List.of("AFT")))
        );
        clearingAdapters.add(createClearingAdapter(
                "TEST",
                InvoiceTestConstant.PROVIDER_ID_DONT_HAS_AFT,
                createExcludeTransactions(null))
        );
        return clearingAdapters;
    }

    public static ClearingServiceProperties.ExcludeOperationParams createExcludeTransactions(List<String> list) {
        ClearingServiceProperties.ExcludeOperationParams
                excludeOperationParams = new ClearingServiceProperties.ExcludeOperationParams();
        if (list != null) {
            excludeOperationParams.setTypes(list);
        }
        return excludeOperationParams;
    }

    public static ClearingAdapter createClearingAdapter(
            String adapterName,
            int adapterId,
            ClearingServiceProperties.ExcludeOperationParams excludeOperationParams
    ) {
        ClearingAdapterSrv.Iface adapter = mock(ClearingAdapterSrv.Iface.class);
        return new ClearingAdapter(adapter, adapterName, adapterId, 1000, excludeOperationParams);
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

    public static List<InvoicePaymentSession> createInvoicePaymentSessions(Map<String, String> trxExtra) {
        List<InvoicePaymentSession> sessionList = new ArrayList<>();
        sessionList.add(createInvoicePaymentSession(trxExtra));
        return sessionList;
    }

    public static InvoicePaymentSession createInvoicePaymentSession(Map<String, String> trxExtra) {
        return new InvoicePaymentSession()
                .setTargetStatus(createPaymentStatusCaptured())
                .setTransactionInfo(createTransactionInfo(TRANSACTION_ID_1, trxExtra));
    }

    public static TargetInvoicePaymentStatus createPaymentStatusCaptured() {
        return TargetInvoicePaymentStatus.captured(new InvoicePaymentCaptured());
    }

    public static TransactionInfo createTransactionInfo(String transactionId, Map<String, String> trxExtra) {
        return new TransactionInfo()
                .setId(transactionId)
                .setTimestamp(LOCAL_DATE_TIME.toString())
                .setExtra(trxExtra);
    }

    public static Map<String, String> createTrxExtraMap(boolean onTransactionType) {
        Map<String, String> extra = new HashMap<>();
        if (onTransactionType) {
            extra.put("transaction_type", "aft");
        }
        return extra;
    }

}