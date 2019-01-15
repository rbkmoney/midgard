package com.rbkmoney.midgard.service.clearing.helpers.clearing_info;

import com.rbkmoney.midgard.ClearingEventState;
import com.rbkmoney.midgard.ClearingEventStateResponse;
import org.jooq.generated.midgard.tables.pojos.ClearingEvent;

import java.util.List;

public interface ClearingInfoHelper {

    Long createNewClearingEvent(long eventId, String providerId);

    Long prepareTransactionData(long eventId, String providerId);

    ClearingEventStateResponse getClearingEventState(long eventId);

    void setClearingEventState(long clearingEventId, ClearingEventState state);

    List<ClearingEvent> getAllExecuteClearingEvents();

}
