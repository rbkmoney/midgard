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

    @Scheduled(cron = "00 01 00 * * *")
    private void scheduleProvider115() {
        scheduleClearingEventForProvider(115);
    }

    @Scheduled(cron = "0 0 11/2 ? * *")
    private void scheduleProvider1() {
        log.info("For the test clearing: Instant time - '{}', LocalDateTime = '{}'",
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
