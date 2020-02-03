package com.rbkmoney.midgard.test.integration.job;

import com.rbkmoney.damsel.schedule.*;
import com.rbkmoney.midgard.ClearingServiceSrv;
import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventInfo;
import com.rbkmoney.midgard.scheduler.model.AdapterJobContext;
import com.rbkmoney.midgard.scheduler.serialize.ScheduleJobSerializer;
import com.rbkmoney.midgard.test.integration.AbstractIntegrationTest;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.nio.ByteBuffer;

import static org.mockito.Mockito.*;

public class ClearingJobExecutorTest extends AbstractIntegrationTest {

    @Autowired
    private ScheduleJobSerializer scheduleJobSerializer;

    @Autowired
    private ScheduledJobExecutorSrv.Iface scheduledJobExecutor;

    @MockBean
    private ClearingServiceSrv.Iface clearingEventService;

    @MockBean
    private ClearingEventInfoDao clearingEventInfoDao;

    @Before
    public void setUp() throws Exception {
        ClearingEventInfo clearingEventInfo = mock(ClearingEventInfo.class);
        when(clearingEventInfo.getEventId()).thenReturn(1L);
        when(clearingEventInfoDao.getLastClearingEvent(anyInt())).thenReturn(clearingEventInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExecutionContextProviderTest() throws TException {
        AdapterJobContext adapterJobContext = new AdapterJobContext();
        adapterJobContext.setUrl("testUrl");
        adapterJobContext.setName("testName");
        adapterJobContext.setNetworkTimeout(60000);
        byte[] jobContext = scheduleJobSerializer.writeByte(adapterJobContext);
        scheduledJobExecutor.validateExecutionContext(ByteBuffer.wrap(jobContext));
    }

    @Test
    public void validateExecutioContextSuccessTest() throws TException {
        byte[] jobContext = scheduleJobSerializer.writeByte(createAdapterJobContext());

        ContextValidationResponse contextValidationResponse =
                scheduledJobExecutor.validateExecutionContext(ByteBuffer.wrap(jobContext));
        Assert.assertEquals(
                ValidationResponseStatus.success(new ValidationSuccess()),
                contextValidationResponse.getResponseStatus()
        );
    }

    @Test
    public void executeJobTest() throws TException {
        AdapterJobContext adapterJobContext = createAdapterJobContext();
        byte[] jobContext = scheduleJobSerializer.writeByte(adapterJobContext);
        ExecuteJobRequest executeJobRequest = new ExecuteJobRequest(null, ByteBuffer.wrap(jobContext));

        ByteBuffer executeJobResult = scheduledJobExecutor.executeJob(executeJobRequest);

        AdapterJobContext adapterJobContextResult = scheduleJobSerializer.read(executeJobResult.array());

        Assert.assertEquals(adapterJobContext.getUrl(), adapterJobContextResult.getUrl());
        Assert.assertEquals(adapterJobContext.getName(), adapterJobContextResult.getName());
        Assert.assertEquals(adapterJobContext.getNetworkTimeout(), adapterJobContextResult.getNetworkTimeout());
        Assert.assertEquals(adapterJobContext.getProviderId(), adapterJobContextResult.getProviderId());
    }

    private static AdapterJobContext createAdapterJobContext() {
        AdapterJobContext adapterJobContext = new AdapterJobContext();
        adapterJobContext.setUrl("testUrl");
        adapterJobContext.setName("testName");
        adapterJobContext.setNetworkTimeout(60000);
        adapterJobContext.setProviderId(1);
        return adapterJobContext;
    }

}
