package com.rbkmoney.midgard.service.clearing.dao.clearing_info;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.exception.PreparingDataException;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;

import java.util.List;

public interface ClearingEventInfoDao extends ClearingDao<ClearingEventInfo, Long> {

    ClearingEventInfo getClearingEvent(long eventId);

    void updateClearingStatus(Long clearingId, ClearingEventStatus status);

    void updateClearingStatus(Long eventId, Integer providerId, ClearingEventStatus status);

    List<ClearingEventInfo> getAllClearingEventsByStatus(ClearingEventStatus status);

    List<ClearingEventInfo> getAllClearingEventsForProviderByStatus(int providerId, ClearingEventStatus status);

    ClearingEventInfo getLastClearingEvent(int providerId) throws DaoException;

}
