package com.rbkmoney.midgard.service.clearing.dao.clearing_info;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.ClearingEvent;

import java.util.List;

public interface ClearingInfoDao extends ClearingDao<ClearingEvent> {

    ClearingEvent getClearingEvent(long eventId);

    void updateClearingStatus(Long clearingId, ClearingEventStatus status);

    List<ClearingEvent> getClearingEventsByStatus(ClearingEventStatus status);

}
