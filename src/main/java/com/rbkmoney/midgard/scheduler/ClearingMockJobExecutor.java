package com.rbkmoney.midgard.scheduler;

import com.rbkmoney.damsel.schedule.*;
import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.ClearingServiceSrv;
import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventInfo;
import com.rbkmoney.midgard.scheduler.model.AdapterJobContext;
import com.rbkmoney.midgard.scheduler.serialize.ScheduleJobSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClearingMockJobExecutor implements ScheduledJobExecutorSrv.Iface {

    private static final String MTS_ADAPTER_NAME = "mts";

    private final ScheduleJobSerializer scheduleJobSerializer;

    private final ClearingServiceSrv.Iface clearingEventService;

    private final ClearingEventInfoDao clearingEventInfoDao;

    @Override
    public ContextValidationResponse validateExecutionContext(ByteBuffer byteBuffer) throws TException {
        AdapterJobContext adapterJobContext = scheduleJobSerializer.read(byteBuffer.array());
        if (adapterJobContext.getProviderId() == null) {
            throw new IllegalArgumentException("Adapter provider id cannot be null");
        }
        ContextValidationResponse contextValidationResponse = new ContextValidationResponse();
        ValidationResponseStatus validationResponseStatus = new ValidationResponseStatus();
        validationResponseStatus.setSuccess(new ValidationSuccess());
        contextValidationResponse.setResponseStatus(validationResponseStatus);
        return contextValidationResponse;
    }

    @Override
    public ByteBuffer executeJob(ExecuteJobRequest executeJobRequest) throws TException {
        log.info("Execute 'clearing' job: {}", executeJobRequest);
        AdapterJobContext adapterJobContext = scheduleJobSerializer.read(executeJobRequest.getServiceExecutionContext());
        log.info("Clearing adapter job context: {}", adapterJobContext);
        try {
            ClearingEventInfo lastClearingEvent = clearingEventInfoDao.getLastClearingEvent(adapterJobContext.getProviderId());
            ClearingEvent clearingEvent = new ClearingEvent();
            clearingEvent.setEventId(lastClearingEvent.getEventId() + 1);
            clearingEvent.setProviderId(adapterJobContext.getProviderId());
            clearingEventService.startClearingEvent(clearingEvent);
            log.info("Schedule job for provider with id {} finished", adapterJobContext.getProviderId());

            return ByteBuffer.wrap(scheduleJobSerializer.writeByte(adapterJobContext));
        } catch (Exception ex) {
            log.error("Error was received when performing a scheduled clearing task", ex);
            throw new IllegalStateException(String.format("Execute job '%s' failed", adapterJobContext.getName()));
        }
    }

}
