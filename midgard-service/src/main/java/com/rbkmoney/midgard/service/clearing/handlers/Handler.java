package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.service.clearing.data.enums.HandlerType;

public interface Handler {

    void handle();

    boolean isInstance(HandlerType handler);

}
