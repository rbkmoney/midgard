package com.rbkmoney.midgard.service.load.services;

import java.util.Optional;

public interface EventService<TEvent, TPayload> {

    default Optional<Long> getLastEventId() throws Exception {
        throw new Exception("The method is not implemented");
    }

    default Optional<Long> getLastEventId(int div, int mod) throws Exception {
        throw new Exception("The method is not implemented");
    }

    void handleEvents(TEvent processingEvent, TPayload payload);

}
