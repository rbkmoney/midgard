package com.rbkmoney.midgard.base.clearing.data.enums;

public enum ImporterType {

    TRANSACTION("Импортер транзакций из внешней системы"),
    MERCHANT("Импортер мерчантов из внешней системы"),
    REFUND("Импортер возвратов из внешней системы");

    private String description;

    ImporterType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
