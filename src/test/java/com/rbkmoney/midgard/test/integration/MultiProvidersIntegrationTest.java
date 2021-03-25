package com.rbkmoney.midgard.test.integration;

import com.rbkmoney.midgard.ClearingAdapterException;
import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingDataPackageTag;
import com.rbkmoney.midgard.ClearingDataRequest;
import com.rbkmoney.midgard.ClearingDataResponse;
import com.rbkmoney.midgard.ClearingEventResponse;
import com.rbkmoney.midgard.ClearingEventState;
import com.rbkmoney.midgard.ClearingServiceSrv;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.domain.enums.TransactionClearingState;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingTransaction;
import com.rbkmoney.midgard.service.clearing.ClearingRevisionService;
import com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant;
import io.github.benas.randombeans.api.EnhancedRandom;
import lombok.Data;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.rbkmoney.midgard.test.integration.data.ClearingEventTestData.getClearingEvent;
import static org.junit.Assert.assertEquals;

public class MultiProvidersIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TransactionsDao transactionsDao;
    @Autowired
    private ClearingServiceSrv.Iface clearingEventService;
    @Autowired
    private ClearingRevisionService clearingRevisionService;
    @Autowired
    private List<ClearingAdapter> adapters;

    @Before
    public void before() {
        for (ClearingAdapter adapter : adapters) {
            ClearingAdapterSrv.Iface testClearingAdapter = new TestClearingAdapter();
            adapter.setAdapter(testClearingAdapter);
        }
    }

    @Test
    public void multiProvidersTest() throws TException {
        // Write multiple transactions to the database for different providers
        int targetProviderId = 115;
        int secondProviderId = 121;
        int targetOpersCount = 10;
        int opersCountForSecondProvider = 15;

        createTestClearingTransactionsByPtovider(targetProviderId, targetOpersCount);
        createTestClearingTransactionsByPtovider(secondProviderId, opersCountForSecondProvider);

        // Start clearing for a specific provider
        clearingEventService.startClearingEvent(getClearingEvent(11, targetProviderId));
        clearingRevisionService.process();

        // Check the result of the clearing
        ClearingAdapter clearingAdapter = adapters.stream()
                .filter(adapter -> adapter.getAdapterId() == targetProviderId)
                .findFirst()
                .orElseThrow();
        TestClearingAdapter testClearingAdapter = (TestClearingAdapter) clearingAdapter.getAdapter();
        int totalTranListSize = testClearingAdapter.getTotalTransactionsList().size();
        assertEquals("The number of operations sent to the adapter is not expected",
                targetOpersCount, totalTranListSize);

        List<ClearingTransaction> readyClearingTransactions =
                transactionsDao.getReadyClearingTransactions(secondProviderId, 1000);
        assertEquals("The number of ready operations for the second adapter is not equal to expected",
                opersCountForSecondProvider, readyClearingTransactions.size());
    }

    private int createTestClearingTransactionsByPtovider(int providerId, int count) {
        Collection<ClearingTransaction> clearingTransactions =
                EnhancedRandom.randomCollectionOf(count, ClearingTransaction.class, "providerId");
        for (ClearingTransaction clearingTransaction : clearingTransactions) {
            clearingTransaction.setId(Math.abs(clearingTransaction.getId()));
            clearingTransaction.setProviderId(providerId);
            clearingTransaction.setTransactionCurrency("RUB");
            clearingTransaction.setTransactionClearingState(TransactionClearingState.READY);
            clearingTransaction.setPayerBankCardExpiredDateMonth(InvoiceTestConstant.CARD_EXP_DATE_MONTH);
            clearingTransaction.setPayerBankCardExpiredDateYear(InvoiceTestConstant.CARD_EXP_DATE_YEAR);
            transactionsDao.save(clearingTransaction);
        }
        return clearingTransactions.size();
    }

    @Data
    private static class TestClearingAdapter implements ClearingAdapterSrv.Iface {

        private final List<Transaction> totalTransactionsList = new ArrayList<>();

        @Override
        public String startClearingEvent(long clearingId) throws TException {
            return "TestUploadId";
        }

        @Override
        public ClearingDataResponse sendClearingDataPackage(String uploadId,
                                                            ClearingDataRequest clearingDataRequest)
                throws TException {
            totalTransactionsList.addAll(clearingDataRequest.getTransactions());
            ClearingDataResponse response = new ClearingDataResponse();
            response.setClearingDataPackageTag(new ClearingDataPackageTag(
                    "PackageId-" + clearingDataRequest.getPackageNumber(),
                    clearingDataRequest.getPackageNumber())
            );
            return response;
        }

        @Override
        public void completeClearingEvent(String uploadId,
                                          long clearingId,
                                          List<ClearingDataPackageTag> tags)
                throws TException {

        }

        @Override
        public ClearingEventResponse getBankResponse(long clearingId)
                throws TException {
            return new ClearingEventResponse(clearingId, ClearingEventState.EXECUTE);
        }
    }

}
