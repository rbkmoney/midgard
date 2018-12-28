package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.service.clearing.data.enums.HandlerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingRevisionHandler implements Handler {

    private final ClearingAdapterSrv.Iface clearingAdapterService;

    @Override
    public void handle() {

    }

    @Override
    public boolean isInstance(HandlerType handlerType) {
        return HandlerType.CLEARING_REVISION == handlerType;
    }
}
