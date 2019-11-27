package com.rbkmoney.midgard.base.tests.integration;

import com.rbkmoney.midgard.base.tests.integration.dao.TestTransactionsDao;
import com.rbkmoney.midgard.service.clearing.dao.refund.RefundDao;
import com.rbkmoney.midgard.service.clearing.importers.Importer;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.sql.SQLException;
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

    @Before
    public void init() throws IOException, SQLException {
        initDb();
    }

    private static final int POOL_SIZE = 30;

    @Test
    public void migrationTest() {
        TestTransactionsDao testTransactionsDao = new TestTransactionsDao(dataSource);

        List<Integer> providerIds = new ArrayList<>();
        providerIds.add(0);
        String shopId = "Migration";
        List<Refund> refundList = new ArrayList<>();
        // Failure
        refundList.add(getRefund(1L,"invoice_1", 1L, 1, "tran_id_1", shopId));
        refundList.add(getRefund(2L,"invoice_2", 1L, 1, "tran_id_2", shopId));
        refundList.add(getRefund(3L,"invoice_3", 1L, 1, "tran_id_3", shopId));
        refundList.add(getRefund(4L,"invoice_2", 1L, 1, "tran_id_2", shopId));

        when(refundDao.getRefunds(0L, providerIds, POOL_SIZE)).thenReturn(refundList);

        try {
            refundsImporter.importData(providerIds);
        } catch (Exception ex) {
            log.error("MigrationDataIntegrationTest | Error received", ex);
        }

        Integer clearingRefundCount = testTransactionsDao.getClearingRefundCount(shopId);
        assertEquals("Count of refunds is not equal to the target", new Integer(3), clearingRefundCount);

        // Success
        refundList = new ArrayList<>();
        refundList.add(getRefund(4L,"invoice_4", 1L, 1, "tran_id_4", shopId));
        refundList.add(getRefund(5L,"invoice_5", 1L, 1, "tran_id_5", shopId));
        refundList.add(getRefund(6L,"invoice_6", 1L, 1, "tran_id_6", shopId));
        refundList.add(getRefund(7L,"invoice_7", 1L, 1, "tran_id_7", shopId));

        when(refundDao.getRefunds(0L, providerIds, POOL_SIZE)).thenReturn(refundList);

        try {
            refundsImporter.importData(providerIds);
        } catch (Exception ex) {
            log.error("MigrationDataIntegrationTest | Error received", ex);
        }

        clearingRefundCount = testTransactionsDao.getClearingRefundCount(shopId);
        assertEquals("Count of refunds is not equal to the target", new Integer(7), clearingRefundCount);

    }

}
