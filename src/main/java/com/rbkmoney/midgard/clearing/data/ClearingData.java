package com.rbkmoney.midgard.clearing.data;

import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.Merchant;

import java.util.List;

/** Класс, содержащий данные, которые необходимые для формирования клирингового файла */
public class ClearingData {

    /** Список клиринговых транзакций */
    private List<ClearingTransaction> transactions;
    /** Список мерчантов */
    private List<Merchant> merchants;

    public ClearingData(List<ClearingTransaction> transactions, List<Merchant> merchants) {
        this.transactions = transactions;
        this.merchants = merchants;
    }

    public List<ClearingTransaction> getTransactions() {
        return transactions;
    }

    public List<Merchant> getMerchants() {
        return merchants;
    }

    @Override
    public String toString() {
        return "ClearingData{" +
                "transactions=" + transactions +
                ", merchants=" + merchants +
                '}';
    }
}
