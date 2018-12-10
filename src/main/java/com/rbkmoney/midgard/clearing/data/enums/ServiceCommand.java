package com.rbkmoney.midgard.clearing.data.enums;

/** Список доступных команд к сервису клиринга */
public enum ServiceCommand {

    START_CLEARING("START_CLEARING", "Запуск клиринга"),
    GET_CLEARING_STATUS("GET_CLEARING_STATUS", "Получение статуса клиринга"),
    CREATE_REPORT("CREATE_REPORT", "Создание отчета по клиринговым операциям");

    /** Наимаенование команды */
    private String commandName;
    /** Описание */
    private String description;

    ServiceCommand(String bankName, String description) {
        this.commandName = bankName;
        this.description = description;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Получение типа банка по имени
     *
     * @param commandName наименование банка
     * @return тип
     */
    public static ServiceCommand typeOf(String commandName) {
        for (ServiceCommand value : values()) {
            if (value.getCommandName().equalsIgnoreCase(commandName)) {
                return value;
            }
        }
        return null;
    }

}
