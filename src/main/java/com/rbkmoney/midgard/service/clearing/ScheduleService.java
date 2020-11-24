package com.rbkmoney.midgard.service.clearing;

import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.ClearingServiceSrv;
import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static com.rbkmoney.midgard.utils.ClearingAdaptersUtils.getClearingAdapter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ClearingServiceSrv.Iface clearingEventService;

    private final ClearingEventInfoDao clearingEventInfoDao;

    private final List<ClearingAdapter> adapters;

    @Scheduled(cron = "00 01 21 * * *") // UTC
    private void scheduleProvider115() {
        scheduleClearingEventForProvider(115);
    }

    //@Scheduled(cron = "0 0 8/2 ? * *") //UTC
    private void scheduleProvider1() {
        log.info("For the test clearing: Instant time - '{}', LocalDateTime = '{}'",
                Instant.now().toString(), LocalDateTime.now().toString());
        scheduleClearingEventForProvider(1);
    }

    @Scheduled(cron = "00 01 11 * * *") // UTC
    private void scheduleProvider1at14() {
        log.info("For the test clearing at 14:01 MSK: Instant time - '{}', LocalDateTime = '{}'",
                Instant.now().toString(), LocalDateTime.now().toString());
        scheduleClearingEventForProvider(1);
    }

    @Scheduled(cron = "00 01 12 * * *") // UTC
    private void scheduleProvider1at15() {
        log.info("For the test clearing at 15:01 MSK: Instant time - '{}', LocalDateTime = '{}'",
                Instant.now().toString(), LocalDateTime.now().toString());
        scheduleClearingEventForProvider(1);
    }

    @Scheduled(cron = "00 01 13 * * *") // UTC
    private void scheduleProvider1at16() {
        log.info("For the test clearing at 16:01 MSK: Instant time - '{}', LocalDateTime = '{}'",
                Instant.now().toString(), LocalDateTime.now().toString());
        scheduleClearingEventForProvider(1);
    }

    @Scheduled(cron = "00 01 14 * * *") // UTC
    private void scheduleProvider1at17() {
        log.info("For the test clearing at 17:01 MSK: Instant time - '{}', LocalDateTime = '{}'",
                Instant.now().toString(), LocalDateTime.now().toString());
        scheduleClearingEventForProvider(1);
    }

    private void scheduleClearingEventForProvider(Integer providerId) {
        try {
            log.info("Schedule for provider with id {} get started", providerId);
            getClearingAdapter(adapters, providerId);

            ClearingEventInfo lastClearingEvent = clearingEventInfoDao.getLastClearingEvent(providerId);
            ClearingEvent clearingEvent = new ClearingEvent();
            long eventId = lastClearingEvent == null || lastClearingEvent.getEventId() == null ? 0 : lastClearingEvent.getEventId() + 1;
            clearingEvent.setEventId(eventId);
            clearingEvent.setProviderId(providerId);
            clearingEventService.startClearingEvent(clearingEvent);
            log.info("Schedule for provider with id {} finished", providerId);
        } catch (Exception ex) {
            log.error("Error was received when performing a scheduled clearing task", ex);
        }
    }

}
