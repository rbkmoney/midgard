package com.rbkmoney.midgard.service.load.services;

import java.util.Optional;

public interface EventService<TEvent, TPayload> {

    Optional<Long> getLastEventId();

    default Optional<Long> getLastEventId(int div, int mod) throws Exception {
        throw new Exception("The method is not implemented");
    }

    void handleEvents(TEvent processingEvent, TPayload payload);

}
