package com.rbkmoney.midgard.test.integration;

import com.rbkmoney.midgard.ClearingDataRequest;
import com.rbkmoney.midgard.dao.refund.ClearingRefundDao;
import com.rbkmoney.midgard.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.data.ClearingDataPackage;
import com.rbkmoney.midgard.domain.enums.ClearingTrxType;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventTransactionInfo;
import com.rbkmoney.midgard.handler.ClearingPackageHandler;
import com.rbkmoney.midgard.test.unit.data.TestTransactionsData;
import com.rbkmoney.midgard.utils.MappingUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class ErrorTrxProcessingIntegrationTest extends AbstractIntegrationTest {

    private static final long CLEARING_ID = 1;
    private static final int PROVIDER_ID = 1;
    private static final int TRX_INFO_COUNT = 10;
    private static final int REFUND_INFO_COUNT = 3;
    @Autowired
    private ClearingPackageHandler clearingTransactionPackageHandler;
    @MockBean
    private TransactionsDao transactionsDao;
    @MockBean
    private ClearingRefundDao clearingRefundDao;

    @Test
    public void testFailureProcessing() {
        mockDao();
        ClearingDataPackage clearingPackage = clearingTransactionPackageHandler.getClearingPackage(
                CLEARING_ID,
                PROVIDER_ID,
                1000,
                0L,
                1
        );
        assertNotNull("The clearing package must not be null", clearingPackage);
        ClearingDataRequest request = clearingPackage.getClearingDataRequest();
        assertNotNull("The list of transactions must not be null", request.getTransactions());
        assertEquals("Count of expected transactions is not equal to the received",
                TRX_INFO_COUNT, request.getTransactions().size());
    }

    private void mockDao() {
        when(transactionsDao.getClearingTransactionsByClearingId(Mockito.any(Long.class),
                Mockito.any(Integer.class), Mockito.any(Long.class), Mockito.any(Integer.class)))
                .thenReturn(getTrxList(TRX_INFO_COUNT, REFUND_INFO_COUNT));

        when(transactionsDao.getLastTransaction())
                .thenReturn(TestTransactionsData.getTestClearingTransaction());

        when(transactionsDao.getTransaction(Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(Integer.class))).thenReturn(TestTransactionsData.getTestClearingTransaction());

        when(clearingRefundDao.getRefund(Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(String.class), Mockito.any(Integer.class))).thenReturn(null);

    }

    private List<ClearingEventTransactionInfo> getTrxList(int trxCount, int refundCount) {
        List<ClearingEventTransactionInfo> trxList = new ArrayList<>();
        for (int i = 0; i < trxCount; i++) {
            trxList.add(getClearingTrxInfo(i, ClearingTrxType.PAYMENT));
        }

        for (int i = 0; i < refundCount; i++) {
            trxList.add(getClearingTrxInfo(i, ClearingTrxType.REFUND));
        }
        return trxList;
    }

    private ClearingEventTransactionInfo getClearingTrxInfo(long number, ClearingTrxType type) {
        ClearingEventTransactionInfo info = new ClearingEventTransactionInfo();
        info.setClearingId(CLEARING_ID);
        info.setInvoiceId("inv_" + number);
        info.setPaymentId("pay_" + number);
        info.setRowNumber(number);
        info.setTransactionId("tr_id_" + number);
        if (type == ClearingTrxType.REFUND) {
            info.setRefundId("ref_" + number);
            info.setTransactionType(ClearingTrxType.REFUND);
        } else {
            info.setTransactionType(ClearingTrxType.PAYMENT);
        }
        info.setTrxVersion(MappingUtils.DEFAULT_TRX_VERSION);
        return info;
    }

}
