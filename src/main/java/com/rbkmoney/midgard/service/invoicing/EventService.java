package com.rbkmoney.midgard.service.invoicing;

public interface EventService<TEvent, TPayload> {

    void handleEvents(TEvent processingEvent, TPayload payload);

}
