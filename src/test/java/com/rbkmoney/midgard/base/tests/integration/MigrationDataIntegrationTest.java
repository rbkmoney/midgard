package com.rbkmoney.midgard.base.tests.integration;

import com.rbkmoney.midgard.base.tests.integration.dao.TestTransactionsDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.payment.PaymentDao;
import com.rbkmoney.midgard.service.clearing.dao.refund.RefundDao;
import com.rbkmoney.midgard.service.clearing.importers.RefundsImporter;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.rbkmoney.midgard.base.tests.integration.data.ClearingEventTestData.getRefund;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@TestPropertySource(properties = {"import:trx-pool-size=3"})
public class MigrationDataIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private HikariDataSource dataSource;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private ClearingRefundDao clearingRefundDao;

    @Autowired
    private ClearingCashFlowDao clearingCashFlowDao;

    @Test
    public void migrationTest() throws NoSuchFieldException, IllegalAccessException {
        TestTransactionsDao testTransactionsDao = new TestTransactionsDao(dataSource);

        RefundDao refundDao = mock(RefundDao.class);
        List<Integer> providerIds = new ArrayList<>();
        List<Refund> refundList = new ArrayList<>();
        refundList.add(getRefund(1L, "test_1"));
        refundList.add(getRefund(2L, "test_2"));
        refundList.add(getRefund(1L, "test_3"));
        refundList.add(getRefund(3L, "test_2"));
        when(refundDao.getRefunds(0L, providerIds, 75)).thenReturn(refundList);

        RefundsImporter refundsImporter = new RefundsImporter(paymentDao, refundDao, clearingRefundDao, clearingCashFlowDao);
        Field poolSize = refundsImporter.getClass().getDeclaredField("poolSize");
        poolSize.setAccessible(true);
        poolSize.set(refundsImporter, new Integer(75));
        refundsImporter.getData(providerIds);

        Integer clearingRefundCount = testTransactionsDao.getClearingRefundCount();
        Integer countElementsAfterError = 0;
        assertEquals("Count of refunds is not equal to the target", countElementsAfterError, clearingRefundCount);



//        refundList = new ArrayList<>();
//        refundList.add(getRefund(4L, "test_1"));
//        refundList.add(getRefund(5L, "test_2"));
//        refundList.add(getRefund(6L, "test_3"));
//        refundList.add(getRefund(7L, "test_2"));
//        when(refundDao.getRefunds(0L, providerIds, 75)).thenReturn(refundList);
//
//        refundsImporter = new RefundsImporter(paymentDao, refundDao, clearingRefundDao, clearingCashFlowDao);
//        poolSize = refundsImporter.getClass().getDeclaredField("poolSize");
//        poolSize.setAccessible(true);
//        poolSize.set(refundsImporter, new Integer(75));
//        refundsImporter.getData(providerIds);
//
//        clearingRefundCount = testTransactionsDao.getClearingRefundCount();
//        assertEquals("Count of refunds is not equal to the target", new Integer(6), clearingRefundCount);

    }

}
