package com.rbkmoney.midgard.adapter.mts.data;

import lombok.Data;

/** Класс содержит необходимые карточные данные */
@Data
public class CardData {

    /** PAN */
    private String pan;
    /** Дата истечения срока действия карты */
    private String expDate;

}
