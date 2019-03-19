package com.rbkmoney.midgard.base.tests.integration.data;

import com.rbkmoney.midgard.ClearingDataPackageTag;
import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.ClearingEventResponse;
import com.rbkmoney.midgard.ClearingEventState;
import org.jooq.generated.feed.enums.*;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;

import java.time.LocalDateTime;

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

    public static Refund getRefund(Long eventId, String transactionId) {
        Refund refund = new Refund();
        refund.setId(0L);
        refund.setEventId(eventId);
        refund.setInvoiceId("inv_id");
        refund.setPaymentId("pmt_id");
        refund.setRefundId("rfnd_id");
        refund.setSessionPayloadTransactionBoundTrxId(transactionId);
        refund.setPartyId("party");
        refund.setShopId("shop");
        refund.setCreatedAt(LocalDateTime.now());
        refund.setAmount(1000L);
        refund.setCurrencyCode("RUB");
        refund.setReason("");
        refund.setDomainRevision(1L);
        refund.setSessionPayloadTransactionBoundTrxExtraJson("extra_json");
        return refund;
    }

}
