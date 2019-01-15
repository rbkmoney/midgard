package com.rbkmoney.midgard.service.clearing.dao.clearing_info;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.ClearingEvent;

import java.util.List;

public interface ClearingInfoDao extends ClearingDao<ClearingEvent> {

    ClearingEvent getClearingEvent(long eventId);

    void updateClearingState(Long clearingId, ClearingEventStatus state);

    List<ClearingEvent> getClearingEventsByState(ClearingEventStatus state);

}
