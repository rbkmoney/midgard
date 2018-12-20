package com.rbkmoney.midgard.adapter.mts.data;

import lombok.Data;
import java.util.List;

/** Класс, содержащий данные, которые необходимые для формирования клирингового файла */
@Data
public class ClearingData {

    /** ID клирингового события */
    private Long clearingId;
    /** Список клиринговых транзакций */
    private List<TransactionData> transactions;
    /** Список мерчантов */
    private List<MerchantData> merchants;
    //TODO: предварительная логика взаимодействия клиринга и клирингового адаптера
    /** Номер пакета в рамкаъ передачи */
    private int packageNumber = 1;
    /** Признак последнего блока в передаче */
    private boolean isFinalPackage = true;

}
