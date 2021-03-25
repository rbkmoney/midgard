package com.rbkmoney.midgard.service.invoicing;

public interface EventService<E, P> {

    void handleEvents(E event, P payload);

}
