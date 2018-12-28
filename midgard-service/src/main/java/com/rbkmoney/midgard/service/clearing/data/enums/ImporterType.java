package com.rbkmoney.midgard.service.clearing.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImporterType {

    TRANSACTION("Импортер транзакций из внешней системы"),
    MERCHANT("Импортер мерчантов из внешней системы"),
    REFUND("Импортер возвратов из внешней системы");

    private String description;

}
