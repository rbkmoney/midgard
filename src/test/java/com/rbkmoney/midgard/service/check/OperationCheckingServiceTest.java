package com.rbkmoney.midgard.service.check;

import com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant;
import org.junit.jupiter.api.Test;

import static com.rbkmoney.midgard.utils.OperationCheckingServiceUtils.checkType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationCheckingServiceTest {

    @Test
    public void isOperationForSkipTest() {
        boolean checkType = checkType(true, InvoiceTestConstant.PROVIDER_ID_HAS_AFT);
        assertTrue(checkType);

        checkType = checkType(false, InvoiceTestConstant.PROVIDER_ID_HAS_AFT);
        assertFalse(checkType);

        checkType = checkType(false, InvoiceTestConstant.PROVIDER_ID_DONT_HAS_AFT);
        assertFalse(checkType);

        checkType = checkType(false, InvoiceTestConstant.PROVIDER_ID_DONT_HAS_AFT);
        assertFalse(checkType);
    }

}