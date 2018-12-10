package com.rbkmoney.midgard.clearing.data.enums;

/** Список исполнителей в сервисе */
public enum ServiceHandlers {

    CLEARING("CLEARING", "Исполнитель клиринга"),
    MIGRATE_DATA("MIGRATE_DATA", "Исполнитель миграции данных"),
    SEND_DATA("SEND_DATA", "Исполнитель отправки даных");

    /** Наимаенование исполнителя */
    private String handlerName;
    /** Описание */
    private String description;

    ServiceHandlers(String bankName, String description) {
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
    public static ServiceHandlers typeOf(String handlerName) {
        for (ServiceHandlers value : values()) {
            if (value.getHandlerName().equalsIgnoreCase(handlerName)) {
                return value;
            }
        }
        return null;
    }

}
