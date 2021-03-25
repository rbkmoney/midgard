package com.rbkmoney.midgard.test.integration.handler;

import com.rbkmoney.midgard.ClearingOperationInfo;
import com.rbkmoney.midgard.ClearingOperationType;
import com.rbkmoney.midgard.OperationNotFound;
import com.rbkmoney.midgard.dao.refund.ClearingRefundDao;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingRefund;
import com.rbkmoney.midgard.handler.reverse.ReverseClearingRefundHandler;
import com.rbkmoney.midgard.test.integration.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReverseClearingOperationHandlerTest extends AbstractIntegrationTest {

    @Autowired
    private ReverseClearingRefundHandler reverseClearingRefundHandler;

    @Autowired
    private ClearingRefundDao clearingRefundDao;

    @Test
    public void reverseRefundOperationTest() throws OperationNotFound {
        ClearingRefund sourceRefund = random(ClearingRefund.class);
        ClearingOperationInfo operationInfo = getTestClearingOperationInfo(sourceRefund);

        assertTrue(reverseClearingRefundHandler.isAccept(operationInfo));

        clearingRefundDao.save(sourceRefund);
        reverseClearingRefundHandler.reverseOperation(operationInfo);
        ClearingRefund resultRefund = clearingRefundDao.getRefund(
                operationInfo.getInvoiceId(),
                operationInfo.getPaymentId(),
                operationInfo.getRefundId(),
                operationInfo.getVersion() + 1);
        assertNotNull(resultRefund);
        assertTrue(resultRefund.getIsReversed());
    }

    private ClearingOperationInfo getTestClearingOperationInfo(ClearingRefund sourceRefund) {
        ClearingOperationInfo operationInfo = new ClearingOperationInfo();
        operationInfo.setInvoiceId(sourceRefund.getInvoiceId());
        operationInfo.setPaymentId(sourceRefund.getPaymentId());
        operationInfo.setRefundId(sourceRefund.getRefundId());
        operationInfo.setAmount(sourceRefund.getAmount());
        operationInfo.setTransactionType(ClearingOperationType.refund);
        operationInfo.setVersion(sourceRefund.getTrxVersion());
        return operationInfo;
    }

}
