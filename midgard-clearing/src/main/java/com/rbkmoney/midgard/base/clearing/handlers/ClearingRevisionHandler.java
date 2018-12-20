package com.rbkmoney.midgard.base.clearing.handlers;

import com.rbkmoney.midgard.base.clearing.data.enums.HandlerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Исполнитель операции проверки статуса клирингового события */
@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingRevisionHandler implements Handler {

    @Override
    public void handle() {

    }

    @Override
    public boolean isInstance(HandlerType handlerType) {
        return HandlerType.CLEARING_REVISION == handlerType;
    }
}
