package com.rbkmoney.midgard.base.clearing.data.enums;

/** Список исполнителей в сервисе */
public enum HandlerType {

    CLEARING_EVENT("CLEARING_EVENT", "Исполнитель клирингового события"),
    CLEARING_REVISION("CLEARING_REVISION", "Исполнитель проверки статуса клирингоаого события у адаптера"),
    MIGRATE_DATA("MIGRATE_DATA", "Исполнитель миграции данных из внешней системы в хранилище клирингового сервиса");

    /** Наимаенование исполнителя */
    private String handlerName;
    /** Описание */
    private String description;

    HandlerType(String bankName, String description) {
        this.handlerName = bankName;
        this.description = description;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Получение типа банка по имени
     *
     * @param handlerName наименование банка
     * @return тип
     */
    public static HandlerType typeOf(String handlerName) {
        for (HandlerType value : values()) {
            if (value.getHandlerName().equalsIgnoreCase(handlerName)) {
                return value;
            }
        }
        return null;
    }

}
