package com.rbkmoney.midgard.base.tests.integration;

import com.rbkmoney.midgard.base.tests.integration.dao.TestTransactionsDao;
import com.rbkmoney.midgard.service.clearing.dao.refund.RefundDao;
import com.rbkmoney.midgard.service.clearing.importers.Importer;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static com.rbkmoney.midgard.base.tests.integration.data.ClearingEventTestData.getRefund;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@Slf4j
public class MigrationDataIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private HikariDataSource dataSource;

    @MockBean
    private RefundDao refundDao;

    @Autowired
    private Importer refundsImporter;

    @Test
    public void migrationTest() {
        TestTransactionsDao testTransactionsDao = new TestTransactionsDao(dataSource);

        List<Integer> providerIds = new ArrayList<>();
        List<Refund> refundList = new ArrayList<>();
        // Failure
        refundList.add(getRefund(1L, "test_1"));
        refundList.add(getRefund(2L, "test_2"));
        refundList.add(getRefund(1L, "test_3"));
        refundList.add(getRefund(3L, "test_2"));
        when(refundDao.getRefunds(0L, providerIds, 75)).thenReturn(refundList);

        try {
            refundsImporter.importData(providerIds);
        } catch (Exception ex) {
            log.error("MigrationDataIntegrationTest | Error received", ex);
        }

        Integer clearingRefundCount = testTransactionsDao.getClearingRefundCount();
        assertEquals("Count of refunds is not equal to the target", new Integer(0), clearingRefundCount);

        // Success
        refundList = new ArrayList<>();
        refundList.add(getRefund(4L, "test_1"));
        refundList.add(getRefund(5L, "test_2"));
        refundList.add(getRefund(6L, "test_3"));
        refundList.add(getRefund(7L, "test_2"));
        when(refundDao.getRefunds(0L, providerIds, 75)).thenReturn(refundList);

        try {
            refundsImporter.importData(providerIds);
        } catch (Exception ex) {
            log.error("MigrationDataIntegrationTest | Error received", ex);
        }

        clearingRefundCount = testTransactionsDao.getClearingRefundCount();
        assertEquals("Count of refunds is not equal to the target", new Integer(4), clearingRefundCount);

    }

}
