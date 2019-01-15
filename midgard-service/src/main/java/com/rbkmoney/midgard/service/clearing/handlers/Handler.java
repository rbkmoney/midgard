package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.service.clearing.data.enums.HandlerType;

public interface Handler {

    void handle(Long id);

    boolean isInstance(HandlerType handler);

}
