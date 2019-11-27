package com.rbkmoney.midgard.service.clearing;

import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.ClearingServiceSrv;
import com.rbkmoney.midgard.dao.info.ClearingEventInfoDao;
import com.rbkmoney.midgard.data.ClearingAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ClearingServiceSrv.Iface clearingEventService;

    private final ClearingEventInfoDao clearingEventInfoDao;

    private final List<ClearingAdapter> adapters;

    @Scheduled(cron = "${clearing-service.schedule.mts}")
    private void scheduleMts() {
        try {
            log.info("Schedule for MTS get started");
            ClearingAdapter mts = adapters.stream()
                    .filter(adapters -> "mts".equalsIgnoreCase(adapters.getAdapterName()))
                    .findFirst()
                    .orElseThrow();

            int providerId = mts.getAdapterId();
            ClearingEventInfo lastClearingEvent = clearingEventInfoDao.getLastClearingEvent(providerId);

            ClearingEvent clearingEvent = new ClearingEvent();
            clearingEvent.setEventId(lastClearingEvent.getEventId() + 1);
            clearingEvent.setProviderId(providerId);
            clearingEventService.startClearingEvent(clearingEvent);
            log.info("Schedule for MTS finished");
        } catch (Exception ex) {
            log.error("Error was received when performing a scheduled clearing task", ex);
        }
    }

}