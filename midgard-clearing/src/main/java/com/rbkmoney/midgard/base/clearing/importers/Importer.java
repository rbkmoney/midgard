package com.rbkmoney.midgard.base.clearing.importers;

import com.rbkmoney.midgard.base.clearing.data.enums.ImporterType;

/** Интерфейс для реализации классов импортирующих данные из внешней системы */
public interface Importer {

    /** Получение данных из внешней системы */
    void getData();

    /** Проверка на принадлежность импортера определенной группе */
    boolean isInstance(ImporterType type);

}
