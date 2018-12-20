package com.rbkmoney.midgard.base.clearing.handlers;

import com.rbkmoney.midgard.base.clearing.data.enums.HandlerType;
import com.rbkmoney.midgard.base.clearing.helpers.ClearingInfoHelper;
import com.rbkmoney.midgard.base.clearing.helpers.MerchantHelper;
import com.rbkmoney.midgard.base.clearing.helpers.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Исполнитель операции получения клиринговых данных */
//TODO: клиринговые данные нужно предоставлять по частям. Пока что предоставляется так, но в дальнейшем нужно переделать
@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingEventHandler implements Handler {

    /** Вспомогательный класс для работы с транзакциями */
    private TransactionHelper transactionHelper;
    /** Вспомогательный класс для работы с мерчантами */
    private MerchantHelper merchantHelper;
    /** Вспомогательный класс для работы с метаинформацией */
    private ClearingInfoHelper clearingInfoHelper;

    @Override
    public void handle() {
        //TODO: перевести обработку клирингового эвента из сервиса сюда

    }

    @Override
    public boolean isInstance(HandlerType handler) {
        return HandlerType.CLEARING_EVENT == handler;
    }

}
