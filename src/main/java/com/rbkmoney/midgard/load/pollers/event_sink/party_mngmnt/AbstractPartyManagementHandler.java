package com.rbkmoney.midgard.load.pollers.event_sink.party_mngmnt;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.midgard.load.pollers.event_sink.Handler;

public abstract class AbstractPartyManagementHandler implements Handler<PartyChange, Event> {
}
