package com.rbkmoney.midgard.base.tests.integration.data;

import com.rbkmoney.midgard.ClearingDataPackageTag;
import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.ClearingEventResponse;
import com.rbkmoney.midgard.ClearingEventState;

public class ClearingEventTestData {

    public static ClearingDataPackageTag getDataPackageTag(Long packageNumber, String tagName) {
        ClearingDataPackageTag tag = new ClearingDataPackageTag();
        tag.setPackageNumber(packageNumber);
        tag.setPackageTagId(tagName);
        return tag;
    }

    public static ClearingEvent getClearingEvent(long eventId, int providerId) {
        ClearingEvent clearingEvent = new ClearingEvent();
        clearingEvent.setEventId(eventId);
        clearingEvent.setProviderId(providerId);
        return clearingEvent;
    }

    public static ClearingEventResponse getSuccessClearingEventTestResponse(long clearingId) {
        ClearingEventResponse response = new ClearingEventResponse();
        response.setClearingId(clearingId);
        response.setClearingState(ClearingEventState.SUCCESS);
        return response;
    }

}
