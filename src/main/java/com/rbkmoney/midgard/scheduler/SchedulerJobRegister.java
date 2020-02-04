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
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "service.schedulator", name = "registerEnable",
        havingValue = "true", matchIfMissing = true)
public class SchedulerJobRegister implements ApplicationListener<ApplicationReadyEvent> {

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
        log.info("Start synchronization of jobs...");
        String serviceCallbackPath = clearingServiceProperties.getServiceCallbackPath();
        clearingServiceProperties.getAdapters().forEach(properties -> syncJobs(properties, serviceCallbackPath));
        log.info("Synchronization of jobs finished");
    }

    private void syncJobs(ClearingServiceProperties.AdapterProperties properties, String serviceCallbackPath) {
        ClearingServiceProperties.SchedulerProperties scheduler = properties.getScheduler();
        try {
            if (scheduler.isEnabled()) {
                registerJob(properties, serviceCallbackPath);
            } else {
                deregisterJob(properties);
            }
        } catch (Exception ex) {
            log.error("Failed to sync job for scheduler '{}'", scheduler, ex);
            throw new RuntimeException(String.format("Failed to sync job for scheduler '%s'", scheduler), ex);
        }
    }

    private void registerJob(ClearingServiceProperties.AdapterProperties properties, String serviceCallbackPath) {
        String adapterName = properties.getName();
        try {
            log.info("Register 'clearing' job for '{}' adapter", adapterName);
            AdapterJobContext adapterJobContext = new AdapterJobContext();
            adapterJobContext.setName(adapterName);
            adapterJobContext.setUrl(properties.getUrl().getURL().toString());
            adapterJobContext.setNetworkTimeout(properties.getNetworkTimeout());
            adapterJobContext.setProviderId(properties.getProviderId());

            ClearingServiceProperties.SchedulerProperties schedulerProperties = properties.getScheduler();

            RegisterJobRequest registerJobRequest = new RegisterJobRequest();
            registerJobRequest.setContext(scheduleJobSerializer.writeByte(adapterJobContext));
            registerJobRequest.setExecutorServicePath(serviceCallbackPath);

            Schedule schedule = buildsSchedule(
                    schedulerProperties.getSchedulerId(),
                    schedulerProperties.getCalendarId(),
                    schedulerProperties.getRevisionId()
            );
            registerJobRequest.setSchedule(schedule);
            retryTemplate.execute(context ->
                    registerJob(schedulerProperties.getJobId(), registerJobRequest));
        } catch (Exception ex) {
            throw new RegisterAdapterJobException("Adapter registration failed: " + adapterName, ex);
        }
    }

    private void deregisterJob(ClearingServiceProperties.AdapterProperties properties) throws TException {
        log.info("Deregister a job for provider with id {}", properties.getProviderId());
        ClearingServiceProperties.SchedulerProperties scheduler = properties.getScheduler();
        schedulatorClient.deregisterJob(scheduler.getJobId());
    }

    private Void registerJob(String jobId, RegisterJobRequest registerJobRequest) {
        try {
            log.info("Register '{}' job", jobId);
            schedulatorClient.registerJob(jobId, registerJobRequest);
        } catch (ScheduleAlreadyExists e) {
            log.warn("Scheduler '{}' already exists", jobId);
        } catch (BadContextProvided e) {
            log.error("Context validation failed. JobId={}", jobId, e);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
        return null;
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
        public <T, E extends Throwable> void onError(RetryContext context,
                                                     RetryCallback<T, E> callback,
                                                     Throwable throwable) {
            log.error("Register job failed. Retry count: {}",
                    context.getRetryCount(), context.getLastThrowable());
        }
    }

}
