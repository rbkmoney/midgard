package com.rbkmoney.midgard.service.check;

import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant;
import com.rbkmoney.midgard.utils.OperationCheckingServiceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.rbkmoney.midgard.utils.OperationCheckingServiceUtils.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationCheckingServiceTest {

    private OperationCheckingService operationCheckingService;

    @BeforeEach
    public void before() {
        List<ClearingAdapter> clearingAdapters = createClearingAdapters();
        operationCheckingService = new OperationCheckingService(clearingAdapters);
    }

    @Test
    public void isOperationForSkipTest() {
        InvoicePayment invoicePayment = createInvoicePayment(true, InvoiceTestConstant.PROVIDER_ID_HAS_AFT);
        assertTrue(operationCheckingService.isOperationForSkip(invoicePayment));

        invoicePayment = createInvoicePayment(false, InvoiceTestConstant.PROVIDER_ID_HAS_AFT);
        assertFalse(operationCheckingService.isOperationForSkip(invoicePayment));

        invoicePayment = createInvoicePayment(false, InvoiceTestConstant.PROVIDER_ID_DONT_HAS_AFT);
        assertFalse(operationCheckingService.isOperationForSkip(invoicePayment));

        invoicePayment = createInvoicePayment(false, InvoiceTestConstant.PROVIDER_ID_DONT_HAS_AFT);
        ;
        assertFalse(operationCheckingService.isOperationForSkip(invoicePayment));
    }

}