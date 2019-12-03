package com.rbkmoney.midgard.test.integration.data;

import com.rbkmoney.midgard.*;

public class ClearingEventTestData {

    public static ClearingDataResponse getDataPackageTag(int packageNumber, String tagName) {
        ClearingDataPackageTag tag = new ClearingDataPackageTag();
        tag.setPackageNumber(packageNumber);
        tag.setPackageTagId(tagName);
        ClearingDataResponse response = new ClearingDataResponse();
        response.setClearingDataPackageTag(tag);
        return response;
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
