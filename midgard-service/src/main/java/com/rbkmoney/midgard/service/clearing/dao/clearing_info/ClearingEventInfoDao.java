package com.rbkmoney.midgard.service.clearing.dao.clearing_info;

import com.rbkmoney.midgard.service.clearing.dao.common.ClearingDao;
import org.jooq.generated.midgard.enums.ClearingEventStatus;
import org.jooq.generated.midgard.tables.pojos.ClearingEventInfo;

import java.util.List;

public interface ClearingEventInfoDao extends ClearingDao<ClearingEventInfo> {

    ClearingEventInfo getClearingEvent(long eventId);

    void updateClearingStatus(Long clearingId, ClearingEventStatus status);

    List<ClearingEventInfo> getClearingEventsByStatus(ClearingEventStatus status);

    Long prepareTransactionData(long clearingId, String providerId);

}
