package com.rbkmoney.midgard.test.unit;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.ClearingDataRequest;
import com.rbkmoney.midgard.ProviderNotFound;
import com.rbkmoney.midgard.config.props.ClearingServiceProperties;
import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.handler.Handler;
import com.rbkmoney.midgard.service.clearing.ClearingEventService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.rbkmoney.midgard.test.integration.data.ClearingEventTestData.getClearingEvent;
import static com.rbkmoney.midgard.test.integration.data.ClearingEventTestData.getDataPackageTag;
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
            when(clearingEventInfoDao.getClearingEvent(Mockito.any(Long.class), Mockito.any(Integer.class)))
                    .thenReturn(null);
            Handler handler = mock(Handler.class);
            ClearingEventService clearingEventService =
                    new ClearingEventService(clearingEventInfoDao, new ArrayList<>(), getClearingAdapters());
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
        ClearingServiceProperties.ExcludeOperationParams
                excludeOperationParams = new ClearingServiceProperties.ExcludeOperationParams();
        excludeOperationParams.setTypes(Collections.emptyList());
        clearingAdapters.add(getClearingAdapter("BANK_1", 1, adapterWorkFlowList, excludeOperationParams));
        clearingAdapters.add(getClearingAdapter("TEST", 2, adapterWorkFlowList, excludeOperationParams));
        return clearingAdapters;
    }

    private ClearingAdapter getClearingAdapter(String adapterName,
                                               int adapterId,
                                               List<AdapterWorkFlow> adapterWorkflows,
                                               ClearingServiceProperties.ExcludeOperationParams excludeOperationParams

    ) throws Exception {
        ClearingAdapterSrv.Iface adapter = mock(ClearingAdapterSrv.Iface.class);
        for (AdapterWorkFlow adapterWorkflow : adapterWorkflows) {
            Long clearingId = adapterWorkflow.getClearingId();
            String uploadId = adapterWorkflow.getUploadId();
            when(adapter.startClearingEvent(clearingId)).thenReturn(uploadId);
            when(adapter.sendClearingDataPackage(uploadId, getDataPackage(clearingId)))
                    .thenReturn(getDataPackageTag(1, "tag_1"));
        }
        return new ClearingAdapter(adapter, adapterName, adapterId, 1000, excludeOperationParams);
    }

    private ClearingDataRequest getDataPackage(long clearingId) {
        ClearingDataRequest request = new ClearingDataRequest();
        request.setClearingId(clearingId);
        request.setPackageNumber(1);
        return request;
    }

    @Getter
    @AllArgsConstructor
    private class AdapterWorkFlow {
        private Long clearingId;
        private String uploadId;
    }

}
