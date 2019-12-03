package com.rbkmoney.midgard.handler.invoicing;

import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.machinegun.eventsink.MachineEvent;

public abstract class AbstractInvoicingEventHandler implements EventHandler<InvoiceChange, MachineEvent> {

    private static final String USER_INFO_ID = "admin";

    public static final UserInfo USER_INFO = new UserInfo()
            .setId(USER_INFO_ID)
            .setType(UserType.service_user(new ServiceUser()));

    public EventRange getEventRange(int sequenceId) {
        return new EventRange().setLimit(sequenceId);
    }

    @Override
    public boolean accept(InvoiceChange change) {
        return getFilter().match(change);
    }

}