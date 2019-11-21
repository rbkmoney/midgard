package com.rbkmoney.midgard.service.load.handler.invoicing;

import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.midgard.service.load.model.SimpleEvent;
import com.rbkmoney.midgard.service.load.handler.Handler;

public abstract class AbstractInvoicingHandler implements Handler<InvoiceChange, SimpleEvent> {

    private static final String USER_INFO_ID = "admin";

    public UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(USER_INFO_ID);
        userInfo.setType(UserType.service_user(new ServiceUser()));
        return userInfo;
    }

    public EventRange getEventRange() {
        EventRange eventRange = new EventRange();
        eventRange.setLimit(Integer.MAX_VALUE);
        return eventRange;
    }

}