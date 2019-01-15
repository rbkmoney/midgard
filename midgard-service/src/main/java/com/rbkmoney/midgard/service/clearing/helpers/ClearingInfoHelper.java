package com.rbkmoney.midgard.service.clearing.helpers;

import com.rbkmoney.midgard.ClearingEventState;
import com.rbkmoney.midgard.ClearingEventStateResponse;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.ClearingInfoDao;
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
public class ClearingInfoHelper {

    private final ClearingInfoDao dao;

    //TODO: рассмотреть вариант, когда для банка присутствует незавершенное клиринговое событие.
    public Long createNewClearingEvent(long eventId, String providerId) {
        log.trace("Creating new clearing event for provider {} by event ", providerId, eventId);
        ClearingEvent clearingEvent = new ClearingEvent();
        clearingEvent.setProviderId(providerId);
        clearingEvent.setEventId(eventId);
        clearingEvent.setState(STARTED);
        return dao.save(clearingEvent);
    }

    @Transactional
    public Long prepareTransactionData(String providerId, long eventId) {
        Long clearingId = createNewClearingEvent(eventId, providerId);
        // Подготовка данных в БД
        PrepareTransactionData prepareTransactionData = new PrepareTransactionData();
        prepareTransactionData.setClearingId(clearingId);
        prepareTransactionData.setProviderId(providerId);
        //TODO: проверить как выполняется - скорей всего нужно переделать
        prepareTransactionData.execute();
        return clearingId;
    }

    public ClearingEventStateResponse getClearingEventState(long eventId) {
        ClearingEvent clearingEvent = dao.getClearingEvent(eventId);
        ClearingEventStateResponse response = new ClearingEventStateResponse();
        if (clearingEvent != null) {
            response.setClearingId(clearingEvent.getId());
            response.setEventId(clearingEvent.getEventId());
            response.setProviderId(clearingEvent.getProviderId());
            response.setClearingState(ClearingEventState.valueOf(clearingEvent.getState().name()));
        }
        return response;
    }

    public void setClearingEventState(long clearingEventId, ClearingEventState state) {
        switch (state) {
            case SUCCESS: {
                dao.updateClearingState(clearingEventId, ClearingEventStatus.SUCCESS);
            }
            case FAILED: {
                dao.updateClearingState(clearingEventId, ClearingEventStatus.FAILED);
            }
        }
    }

    public List<ClearingEvent> getAllExecuteClearingEvents() {
        return dao.getClearingEventsByState(ClearingEventStatus.EXECUTE);
    }

}
