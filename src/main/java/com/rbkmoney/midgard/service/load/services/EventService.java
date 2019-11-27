package com.rbkmoney.midgard.service.load.services;

public interface EventService<TEvent, TPayload> {

    void handleEvents(TEvent processingEvent, TPayload payload);

}
