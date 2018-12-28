package com.rbkmoney.midgard.service.clearing.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HandlerType {

    CLEARING_EVENT("Исполнитель клирингового события"),
    CLEARING_REVISION("Исполнитель проверки статуса клирингоаого события у адаптера"),
    MIGRATE_DATA("Исполнитель миграции данных из внешней системы в хранилище клирингового сервиса");

    private String description;

}
