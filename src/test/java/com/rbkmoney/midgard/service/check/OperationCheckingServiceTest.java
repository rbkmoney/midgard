package com.rbkmoney.midgard.service.check;

import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.*;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.FILL_IN_TRANSACTION_TYPE_ON;
import static com.rbkmoney.midgard.utils.OperationCheckingServiceUtils.createClearingAdapters;
import static com.rbkmoney.midgard.utils.OperationCheckingServiceUtils.createInvoicePayment;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationCheckingServiceTest {

    @Test
    public void isOperationForSkipTest() {
        List<ClearingAdapter> clearingAdapters = createClearingAdapters();
        OperationCheckingService operationCheckingService = new OperationCheckingService(clearingAdapters);

        InvoicePayment invoicePayment = createInvoicePayment(FILL_IN_TRANSACTION_TYPE_ON, PROVIDER_ID_HAS_AFT);
        assertTrue(operationCheckingService.isOperationForSkip(invoicePayment));

        invoicePayment = createInvoicePayment(FILL_IN_TRANSACTION_TYPE_OFF, PROVIDER_ID_HAS_AFT);
        assertFalse(operationCheckingService.isOperationForSkip(invoicePayment));

        invoicePayment = createInvoicePayment(FILL_IN_TRANSACTION_TYPE_OFF, PROVIDER_ID_DONT_HAS_AFT);
        assertFalse(operationCheckingService.isOperationForSkip(invoicePayment));

        invoicePayment = createInvoicePayment(FILL_IN_TRANSACTION_TYPE_OFF, PROVIDER_ID_DONT_HAS_AFT);
        ;
        assertFalse(operationCheckingService.isOperationForSkip(invoicePayment));
    }

}