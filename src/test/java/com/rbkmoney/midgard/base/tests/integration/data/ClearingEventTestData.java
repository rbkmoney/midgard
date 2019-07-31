package com.rbkmoney.midgard.base.tests.integration.data;

import com.rbkmoney.midgard.*;
import org.jooq.generated.feed.tables.pojos.Refund;

import java.time.LocalDateTime;

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

    public static Refund getRefund(Long id,
                                   String invoiceId,
                                   Long sequenceId,
                                   Integer changeId,
                                   String transactionId,
                                   String shopId) {
        Refund refund = new Refund();
        refund.setId(id);
        refund.setInvoiceId(invoiceId);
        refund.setSequenceId(sequenceId);
        refund.setChangeId(changeId);
        refund.setPaymentId("pmt_id");
        refund.setRefundId("rfnd_id");
        refund.setSessionPayloadTransactionBoundTrxId(transactionId);
        refund.setPartyId("party");
        refund.setShopId(shopId);
        refund.setCreatedAt(LocalDateTime.now());
        refund.setAmount(1000L);
        refund.setCurrencyCode("RUB");
        refund.setReason("");
        refund.setDomainRevision(1L);
        refund.setSessionPayloadTransactionBoundTrxExtraJson("extra_json");
        return refund;
    }

}
