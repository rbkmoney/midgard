package com.rbkmoney.midgard.base.clearing.handlers;

import com.rbkmoney.midgard.base.clearing.data.enums.HandlerType;

public interface Handler {

    void handle();

    boolean isInstance(HandlerType handler);

}
