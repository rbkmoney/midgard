package com.rbkmoney.midgard.base.clearing.handlers;

import com.rbkmoney.midgard.base.clearing.data.enums.HandlerType;
import com.rbkmoney.midgard.base.clearing.helpers.ClearingInfoHelper;
import com.rbkmoney.midgard.base.clearing.helpers.MerchantHelper;
import com.rbkmoney.midgard.base.clearing.helpers.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingEventHandler implements Handler {

    private final TransactionHelper transactionHelper;

    private final MerchantHelper merchantHelper;

    private final ClearingInfoHelper clearingInfoHelper;

    @Override
    public void handle() {
        //TODO: перевести обработку клирингового эвента из сервиса сюда

    }

    @Override
    public boolean isInstance(HandlerType handler) {
        return HandlerType.CLEARING_EVENT == handler;
    }

}
