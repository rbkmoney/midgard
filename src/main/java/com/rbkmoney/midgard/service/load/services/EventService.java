package com.rbkmoney.midgard.service.load.services;

import java.util.Optional;

public interface EventService<TEvent, TPayload> {

    Optional<Long> getLastEventId() throws Exception;

    Optional<Long> getLastEventId(int div, int mod) throws Exception;

    void handleEvents(TEvent processingEvent, TPayload payload);

}
