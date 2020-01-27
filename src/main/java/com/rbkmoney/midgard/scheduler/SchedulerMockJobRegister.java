package com.rbkmoney.midgard.scheduler;

import com.rbkmoney.damsel.domain.BusinessScheduleRef;
import com.rbkmoney.damsel.domain.CalendarRef;
import com.rbkmoney.damsel.schedule.*;
import com.rbkmoney.midgard.config.props.ClearingServiceProperties;
import com.rbkmoney.midgard.scheduler.model.AdapterJobContext;
import com.rbkmoney.midgard.scheduler.serialize.ScheduleJobSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "service.schedulator", name = "registerEnable", havingValue = "true", matchIfMissing = true)
public class SchedulerMockJobRegister implements ApplicationListener<ApplicationReadyEvent> {

    private static final String MOCK_ADAPTER_NAME = "mock";

    private final SchedulatorSrv.Iface schedulatorClient;

    private final ScheduleJobSerializer scheduleJobSerializer;

    private final ClearingServiceProperties clearingServiceProperties;

    private final RetryTemplate retryTemplate;

    @PostConstruct
    public void init() {
        retryTemplate.registerListener(new RegisterJobFailListener());
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Trying to find mock adapter");
        Optional<ClearingServiceProperties.AdapterProperties> mockAdapter = findMockAdapter();
        if (mockAdapter.isPresent()) {
            registerAdapterJob(mockAdapter.get());
        } else {
            log.warn("Mock adapter not found");
        }
    }

    private void registerAdapterJob(ClearingServiceProperties.AdapterProperties adapterProperties) {
        try {
            log.info("Register 'clearing' job for '{}' adapter", adapterProperties.getName());
            RegisterJobRequest registerJobRequest = new RegisterJobRequest();
            AdapterJobContext adapterJobContext = new AdapterJobContext();
            adapterJobContext.setName(adapterProperties.getName());
            adapterJobContext.setUrl(adapterProperties.getUrl().getURL().toString());
            adapterJobContext.setNetworkTimeout(adapterProperties.getNetworkTimeout());
            adapterJobContext.setProviderId(adapterProperties.getProviderId());
            registerJobRequest.setContext(scheduleJobSerializer.writeByte(adapterJobContext));
            registerJobRequest.setExecutorServicePath(adapterProperties.getScheduler().getServiceCallbackPath());
            Schedule schedule = buildsSchedule(
                    adapterProperties.getScheduler().getSchedulerId(),
                    adapterProperties.getScheduler().getCalendarId(),
                    adapterProperties.getScheduler().getRevisionId());
            registerJobRequest.setSchedule(schedule);
            retryTemplate.execute(context -> {
                try {
                    log.info("Register '{}' job", adapterProperties.getScheduler().getJobId());
                    schedulatorClient.registerJob(adapterProperties.getScheduler().getJobId(), registerJobRequest);
                } catch (ScheduleAlreadyExists e) {
                    log.warn("Scheduler '{}' already exists", adapterProperties.getScheduler().getJobId());
                } catch (BadContextProvided e) {
                    log.error("Context validation failed. JobId={}", adapterProperties.getScheduler().getJobId(), e);
                } catch (TException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        } catch (Exception e) {
            throw new RegisterAdapterJobException("Adapter registration failed: " + adapterProperties.getName(), e);
        }
    }

    private Optional<ClearingServiceProperties.AdapterProperties> findMockAdapter() {
        return clearingServiceProperties.getAdapters().stream()
                .filter(adapterProperties -> adapterProperties.getName().equalsIgnoreCase(MOCK_ADAPTER_NAME))
                .findFirst();
    }

    private Schedule buildsSchedule(int scheduleRefId, int calendarRefId, long revision) {
        Schedule schedule = new Schedule();
        DominantBasedSchedule dominantBasedSchedule = new DominantBasedSchedule()
                .setBusinessScheduleRef(new BusinessScheduleRef().setId(scheduleRefId))
                .setCalendarRef(new CalendarRef().setId(calendarRefId))
                .setRevision(revision);
        schedule.setDominantSchedule(dominantBasedSchedule);

        return schedule;
    }

    private static final class RegisterJobFailListener extends RetryListenerSupport {
        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            log.error("Register job failed. Retry count: {}", context.getRetryCount(), context.getLastThrowable());
        }
    }

}
