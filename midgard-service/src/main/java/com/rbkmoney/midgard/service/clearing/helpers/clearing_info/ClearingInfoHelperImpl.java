package com.rbkmoney.midgard.service.clearing.helpers.clearing_info;

import com.rbkmoney.midgard.ClearingEventState;
import com.rbkmoney.midgard.ClearingEventStateResponse;
import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingInfoDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.routines.PrepareTransactionData;
import org.jooq.generated.midgard.tables.pojos.ClearingEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.jooq.generated.midgard.enums.ClearingEventStatus.STARTED;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingInfoHelperImpl implements ClearingInfoHelper {

    private final ClearingInfoDao dao;

    //TODO: рассмотреть вариант, когда для банка присутствует незавершенное клиринговое событие.
    @Override
    public Long createNewClearingEvent(long eventId, String providerId) {
        log.trace("Creating new clearing event for provider {} by event ", providerId, eventId);
        ClearingEvent clearingEvent = new ClearingEvent();
        clearingEvent.setProviderId(providerId);
        clearingEvent.setEventId(eventId);
        clearingEvent.setStatus(STARTED);
        return dao.save(clearingEvent);
    }

    @Override
    @Transactional
    public Long prepareTransactionData(long eventId, String providerId) {
        Long clearingId = createNewClearingEvent(eventId, providerId);
        // Подготовка данных в БД
        PrepareTransactionData prepareTransactionData = new PrepareTransactionData();
        prepareTransactionData.setClearingId(clearingId);
        prepareTransactionData.setProviderId(providerId);
        //TODO: проверить как выполняется - скорей всего нужно переделать
        prepareTransactionData.execute();
        return clearingId;
    }

    @Override
    public ClearingEventStateResponse getClearingEventState(long eventId) {
        ClearingEvent clearingEvent = dao.getClearingEvent(eventId);
        ClearingEventStateResponse response = new ClearingEventStateResponse();
        if (clearingEvent != null) {
            response.setClearingId(clearingEvent.getId());
            response.setEventId(clearingEvent.getEventId());
            response.setProviderId(clearingEvent.getProviderId());
            response.setClearingState(ClearingEventState.valueOf(clearingEvent.getStatus().name()));
        }
        return response;
    }

    @Override
    public void setClearingEventState(long clearingEventId, ClearingEventState state) {
        switch (state) {
            case SUCCESS:
                dao.updateClearingStatus(clearingEventId, ClearingEventStatus.SUCCESS);
                break;
            case FAILED:
                dao.updateClearingStatus(clearingEventId, ClearingEventStatus.FAILED);
                break;
        }
    }

    @Override
    public List<ClearingEvent> getAllExecuteClearingEvents() {
        return dao.getClearingEventsByStatus(ClearingEventStatus.EXECUTE);
    }

}
