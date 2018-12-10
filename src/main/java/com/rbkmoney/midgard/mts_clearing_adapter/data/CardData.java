package com.rbkmoney.midgard.mts_clearing_adapter.data;

/** Класс содержит необходимые карточные данные */
public class CardData {

    /** PAN */
    private String pan;
    /** Дата истечения срока действия карты */
    private String expDate;

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

}
