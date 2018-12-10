package com.rbkmoney.midgard.clearing.commands.handlers;

import com.rbkmoney.midgard.clearing.helpers.ClearingInfoHelper;
import com.rbkmoney.midgard.clearing.helpers.MerchantHelper;
import com.rbkmoney.midgard.clearing.helpers.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**  */
public class ClearingHandler implements Handler {

    /** Логгер */
    private static final Logger log = LoggerFactory.getLogger(ClearingHandler.class);
    /** Вспомогательный класс для работы с транзакциями */
    private TransactionHelper transactionHelper;
    /** Вспомогательный класс для работы с мерчантами */
    private MerchantHelper merchantHelper;
    /** Вспомогательный класс для работы с метаинформацией */
    private ClearingInfoHelper clearingInfoHelper;


    public ClearingHandler(TransactionHelper transactionHelper,
                           MerchantHelper merchantHelper,
                           ClearingInfoHelper clearingInfoHelper) {
        this.transactionHelper = transactionHelper;
        this.merchantHelper = merchantHelper;
        this.clearingInfoHelper = clearingInfoHelper;
    }

    @Override
    public void handle() {

    }

}
