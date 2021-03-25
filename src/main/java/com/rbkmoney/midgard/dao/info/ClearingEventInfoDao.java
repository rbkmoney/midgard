package com.rbkmoney.midgard.dao.info;

import com.rbkmoney.midgard.dao.ClearingDao;
import com.rbkmoney.midgard.domain.enums.ClearingEventStatus;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventInfo;
import com.rbkmoney.midgard.exception.DaoException;

import java.util.List;

public interface ClearingEventInfoDao extends ClearingDao<ClearingEventInfo, Long> {

    ClearingEventInfo getClearingEvent(long eventId, int providerId);

    void updateClearingStatus(Long clearingId, ClearingEventStatus status, int providerId);

    void updateClearingStatus(Long eventId, Integer providerId, ClearingEventStatus status);

    List<ClearingEventInfo> getAllClearingEventsByStatus(ClearingEventStatus status);

    List<ClearingEventInfo> getAllClearingEventsForProviderByStatus(int providerId, ClearingEventStatus status);

    ClearingEventInfo getLastClearingEvent(int providerId) throws DaoException;

}
