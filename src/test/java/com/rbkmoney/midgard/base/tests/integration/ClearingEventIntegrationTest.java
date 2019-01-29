package com.rbkmoney.midgard.base.tests.integration;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingDataPackage;
import com.rbkmoney.midgard.ProviderNotFound;
import com.rbkmoney.midgard.base.tests.integration.dao.TestTransactionsDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.dao.payment.PaymentDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.services.ClearingEventService;
import com.rbkmoney.midgard.service.clearing.services.ClearingRevisionService;
import com.rbkmoney.midgard.service.clearing.services.MigrationDataService;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.rbkmoney.midgard.base.tests.integration.data.ClearingEventTestData.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class ClearingEventIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

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

    private TestTransactionsDao testTransactionsDao;

    private static final int PROVIDER_ID = 1;

    private static final int FAIL_PROVIDER_ID = 111;

    private static final int CLEARING_EVENTS_COUNT = 5;

    private static final int CLEARING_TRX_COUNT = 7;

    private static final int RETRY_COUNT = 12;

    private static final long SLEEP_TIME = 5000;

    @Autowired
    private PaymentDao paymentDao;

    @Test
    public void clearingEventIntegrationTest() throws Exception {
        testTransactionsDao = new TestTransactionsDao(dataSource);

        prepareTestEnvironment();

        long outerEventId = 1L;
        ClearingEventInfo clearingEventInfo = initClearingEventTest(outerEventId);

        revisionTest(outerEventId, clearingEventInfo.getId());

        testInvalidProviderId();
    }

    private ClearingEventInfo initClearingEventTest(long outerEventId) throws Exception {
        clearingEventService.startClearingEvent(getClearingEvent(outerEventId, PROVIDER_ID));
        Thread.sleep(SLEEP_TIME);
        ClearingEventInfo clearingEvent = clearingEventInfoDao.getClearingEvent(outerEventId);
        assertNotNull("No clearing event was created for an external event ID " + outerEventId, clearingEvent);

        Long clearingId = clearingEvent.getId();
        Supplier processedSupplier = () -> transactionsDao.getProcessedClearingTransactionCount(clearingId);
        waitingFillingTable(processedSupplier, CLEARING_TRX_COUNT, "ClearingEventTransactionInfo");
        int transactionCount = transactionsDao.getProcessedClearingTransactionCount(clearingId);
        assertEquals("Count of the prepared clearing transactions is not equal to the target",
                CLEARING_TRX_COUNT, transactionCount);

        return clearingEvent;
    }

    private void revisionTest(long outerEventId, long clearingId) throws Exception {
        clearingRevisionService.process();
        Thread.sleep(SLEEP_TIME);

        ClearingTransaction lastTransaction = transactionsDao.getLastTransaction();
        assertNotNull("Table with transactions is empty", lastTransaction);

        List<ClearingTransaction> activeTransactions =
                testTransactionsDao.getAllTransactionsByState(clearingId, TransactionClearingState.ACTIVE);
        assertEquals("Count of the active clearing transactions is not equal to the target",
                CLEARING_TRX_COUNT, activeTransactions.size());

        ClearingEventInfo clearingEvent = clearingEventInfoDao.getClearingEvent(outerEventId);
        assertNotEquals("The status of a clearing event is not equal to the target",
                ClearingEventStatus.EXECUTE, clearingEvent.getStatus());
    }

    private void testInvalidProviderId() {
        boolean isThrownProviderNotFoundException = false;
        try {
            clearingEventService.startClearingEvent(getClearingEvent(200, FAIL_PROVIDER_ID));
        } catch (ProviderNotFound providerNotFound) {
            isThrownProviderNotFoundException = true;
        }
        assertTrue("The exception was not thrown", isThrownProviderNotFoundException);
    }

    private void prepareTestEnvironment() throws Exception {
        ClearingAdapterSrv.Iface adapterSrv = mock(ClearingAdapterSrv.Iface.class);
        for (int clearingId = 1; clearingId <= CLEARING_EVENTS_COUNT; clearingId++) {
            String uploadId = "uploadId_" + clearingId;
            when(adapterSrv.startClearingEvent(clearingId)).thenReturn(uploadId);
            when(adapterSrv.sendClearingDataPackage(Mockito.any(String.class), Mockito.any(ClearingDataPackage.class)))
                    .thenReturn(getDataPackageTag(1L, "tag_1"));
            when(adapterSrv.getBankResponse(clearingId)).thenReturn(getSuccessClearingEventTestRespornse(clearingId));
        }

        adapters.forEach(clearingAdapter -> clearingAdapter.setAdapter(adapterSrv));
        migrationDataService.process();

        List<Integer> providerIds = new ArrayList<>();
        providerIds.add(PROVIDER_ID);
        Supplier paymentSupplier = () -> paymentDao.getPayments(0, providerIds, 100).size();
        waitingFillingTable(paymentSupplier, CLEARING_TRX_COUNT, "Payment");

        Supplier trxSupplier = () -> testTransactionsDao.getReadyClearingTransactionsCount(PROVIDER_ID);
        waitingFillingTable(trxSupplier, CLEARING_TRX_COUNT, "ClearingTransactions");
    }

    private void waitingFillingTable(Supplier supplier, int threshold, String tableName) throws Exception {
        int count = 0;
        do {
            count++;
            int size = (int) supplier.get();
            if (size >= threshold) {
                break;
            } else {
                Thread.sleep(SLEEP_TIME);
            }
            if (count == RETRY_COUNT) {
                throw new Exception(tableName + " table is empty. Current count of elements " + size);
            }
        } while(count < RETRY_COUNT);
    }

}
