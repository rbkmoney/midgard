package com.rbkmoney.midgard.service.load.services;

import java.util.Optional;

public interface EventService<TEvent, TPayload> {

    Optional<Long> getLastEventId();

    Optional<Long> getLastEventId(int div, int mod);

    void handleEvents(TEvent processingEvent, TPayload payload);

}
