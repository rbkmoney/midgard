package com.rbkmoney.midgard.service.load.handler.invoicing;

import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.handler.Handler;

public abstract class AbstractInvoicingHandler implements Handler<InvoiceChange, SimpleEvent> {

    private static final String USER_INFO_ID = "admin";

    public static final UserInfo USER_INFO = new UserInfo()
            .setId(USER_INFO_ID)
            .setType(UserType.service_user(new ServiceUser()));

    public EventRange getEventRange(int sequenceId) {
        EventRange eventRange = new EventRange();
        eventRange.setLimit(sequenceId);
        return eventRange;
    }

}