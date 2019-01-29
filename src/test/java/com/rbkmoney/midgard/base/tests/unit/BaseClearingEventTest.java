package com.rbkmoney.midgard.base.tests.unit;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingDataPackage;
import com.rbkmoney.midgard.ProviderNotFound;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.handlers.Handler;
import com.rbkmoney.midgard.service.clearing.services.ClearingEventService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static com.rbkmoney.midgard.base.tests.integration.data.ClearingEventTestData.getClearingEvent;
import static com.rbkmoney.midgard.base.tests.integration.data.ClearingEventTestData.getDataPackageTag;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseClearingEventTest {

    private static final int FAIL_PROVIDER_ID = 111;

    @Test
    public void testInvalidProviderId() throws Exception {
        boolean isThrownProviderNotFoundException = false;
        try {
            ClearingEventInfoDao clearingEventInfoDao = mock(ClearingEventInfoDao.class);
            when(clearingEventInfoDao.getClearingEvent(Mockito.any(Long.class))).thenReturn(null);
            Handler handler = mock(Handler.class);
            ClearingEventService clearingEventService =
                    new ClearingEventService(clearingEventInfoDao, handler, getClearingAdapters());
            clearingEventService.startClearingEvent(getClearingEvent(200, FAIL_PROVIDER_ID));
        } catch (ProviderNotFound providerNotFound) {
            isThrownProviderNotFoundException = true;
        }
        assertTrue("The exception was not thrown", isThrownProviderNotFoundException);
    }

    private List<ClearingAdapter> getClearingAdapters() throws Exception {
        List<ClearingAdapter> clearingAdapters = new ArrayList<>();
        AdapterWorkFlow adapterWorkFlow = new AdapterWorkFlow(1L, "upload");
        List<AdapterWorkFlow> adapterWorkFlowList = new ArrayList<>();
        adapterWorkFlowList.add(adapterWorkFlow);
        clearingAdapters.add(getClearingAdapter("MTS", 1, adapterWorkFlowList));
        clearingAdapters.add(getClearingAdapter("TEST", 2, adapterWorkFlowList));
        return clearingAdapters;
    }

    private ClearingAdapter getClearingAdapter(String adapterName,
                                              int adapterId,
                                              List<AdapterWorkFlow> adapterWorkflows) throws Exception {
        ClearingAdapterSrv.Iface adapter = mock(ClearingAdapterSrv.Iface.class);
        for (AdapterWorkFlow adapterWorkflow : adapterWorkflows) {
            Long clearingId = adapterWorkflow.getClearingId();
            String uploadId = adapterWorkflow.getUploadId();
            when(adapter.startClearingEvent(clearingId)).thenReturn(uploadId);
            when(adapter.sendClearingDataPackage(uploadId, getDataPackage(clearingId)))
                    .thenReturn(getDataPackageTag(1L, "tag_1"));
        }
        return new ClearingAdapter(adapter, adapterName, adapterId);
    }

    private ClearingDataPackage getDataPackage(long clearingId) {
        ClearingDataPackage dataPackage = new ClearingDataPackage();
        dataPackage.setClearingId(clearingId);
        dataPackage.setPackageNumber(1);
        return dataPackage;
    }

    @Getter
    @AllArgsConstructor
    private class AdapterWorkFlow {
        private Long clearingId;
        private String uploadId;
    }

}