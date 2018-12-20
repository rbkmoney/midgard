package com.rbkmoney.midgard.base.clearing.data.enums;

/** Список импортеров клирингового сервиса */
public enum ImporterType {

    /** Импортер транзакций из внешней системы */
    TRANSACTION,
    /** Импортер мерчантов из внешней системы */
    MERCHANT,
    /** Импортер возвратов из внешней системы */
    REFUND;

}
