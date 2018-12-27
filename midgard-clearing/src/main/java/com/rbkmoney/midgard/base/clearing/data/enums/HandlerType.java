package com.rbkmoney.midgard.base.clearing.data.enums;

public enum HandlerType {

    CLEARING_EVENT("Исполнитель клирингового события"),
    CLEARING_REVISION("Исполнитель проверки статуса клирингоаого события у адаптера"),
    MIGRATE_DATA("Исполнитель миграции данных из внешней системы в хранилище клирингового сервиса");

    private String description;

    HandlerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
