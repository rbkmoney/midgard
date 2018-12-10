package com.rbkmoney.midgard.clearing.data.enums;

/** Список банков */
public enum Bank {

    MTS("MTS", "МТС банк"),
    VTB("VTB", "ВТБ"),
    SBERBANK("SBERBANK", "Сбербанк");

    /** Наимаенование банка */
    private String bankName;
    /** Описание */
    private String description;

    Bank(String bankName, String description) {
        this.bankName = bankName;
        this.description = description;
    }

    public String getBankName() {
        return bankName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Получение типа банка по имени
     *
     * @param bankName наименование банка
     * @return тип
     */
    public static Bank typeOf(String bankName) {
        for (Bank value : values()) {
            if (value.bankName.equals(bankName)) {
                return value;
            }
        }
        return null;
    }

}
