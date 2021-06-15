package com.rbkmoney.midgard.service.check;

import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant;
import com.rbkmoney.midgard.utils.OperationCheckingServiceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationCheckingServiceTest {

    private OperationCheckingService operationCheckingService;

    @BeforeEach
    public void before() {
        List<ClearingAdapter> clearingAdapters = OperationCheckingServiceUtils.createClearingAdapters();
        operationCheckingService = new OperationCheckingService(clearingAdapters);
    }

    @Test
    public void isOperationForSkipTest() {
        boolean checkType = checkOperationType(true, InvoiceTestConstant.PROVIDER_ID_HAS_AFT);
        assertTrue(checkType);

        checkType = checkOperationType(false, InvoiceTestConstant.PROVIDER_ID_HAS_AFT);
        assertFalse(checkType);

        checkType = checkOperationType(false, InvoiceTestConstant.PROVIDER_ID_DONT_HAS_AFT);
        assertFalse(checkType);

        checkType = checkOperationType(false, InvoiceTestConstant.PROVIDER_ID_DONT_HAS_AFT);
        assertFalse(checkType);
    }

    public boolean checkOperationType(boolean onTransactionType, int providerId) {
        InvoicePayment invoicePayment = OperationCheckingServiceUtils.createInvoicePayment(
                OperationCheckingServiceUtils.createTrxExtraMap(onTransactionType)
        );
        invoicePayment.setRoute(OperationCheckingServiceUtils.extractedPaymentRoute(providerId));
        return operationCheckingService.isOperationForSkip(invoicePayment);
    }

}