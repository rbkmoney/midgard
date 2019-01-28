package com.rbkmoney.midgard.base.tests.integration;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingDataPackage;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.services.ClearingEventService;
import com.rbkmoney.midgard.service.clearing.services.ClearingRevisionService;
import com.rbkmoney.midgard.service.clearing.services.MigrationDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.rbkmoney.midgard.base.tests.integration.data.ClearingEventTestData.getClearingEvent;
import static com.rbkmoney.midgard.base.tests.integration.data.ClearingEventTestData.getDataPackageTag;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class ClearingEventIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClearingEventService clearingEventService;

    @Autowired
    private MigrationDataService migrationDataService;

    @Autowired
    private ClearingRevisionService clearingRevisionService;

    @Autowired
    private TransactionsDao transactionsDao;

    @Autowired
    private ClearingEventInfoDao clearingEventInfoDao;

    @Autowired
    private List<ClearingAdapter> adapters;

    private static final int CLEARING_EVENTS_COUNT = 5;

    @Test
    public void clearingEventIntegrationTest() throws Exception {
        prepareTestEnvironment();

        long outerEventId = 1L;
        int providerId = 1;
        clearingEventService.startClearingEvent(getClearingEvent(outerEventId, providerId));

        ClearingEventInfo clearingEvent = clearingEventInfoDao.getClearingEvent(outerEventId);
        assertNotNull("No clearing event was created for an external event ID " + outerEventId, clearingEvent);

        clearingRevisionService.process();
        Thread.sleep(5000);
        ClearingTransaction lastTransaction = transactionsDao.getLastTransaction();
        assertNotNull("Table with transactions is empty", lastTransaction);

        // TODO: write more cases
    }

    private void prepareTestEnvironment() throws TException, InterruptedException {
        ClearingAdapterSrv.Iface adapterSrv = mock(ClearingAdapterSrv.Iface.class);
        for (int clearingId = 1; clearingId <= CLEARING_EVENTS_COUNT; clearingId++) {
            String uploadId = "uploadId_" + clearingId;
            when(adapterSrv.startClearingEvent(clearingId)).thenReturn(uploadId);
            when(adapterSrv.sendClearingDataPackage(Mockito.any(String.class), Mockito.any(ClearingDataPackage.class)))
                    .thenReturn(getDataPackageTag(1L, "tag_1"));
        }
        adapters.forEach(clearingAdapter -> clearingAdapter.setAdapter(adapterSrv));
        migrationDataService.process();
        Thread.sleep(5000);
    }

}
