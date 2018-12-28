package data;

import com.rbkmoney.midgard.adapter.mts.data.TransactionData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Класс с тестовыми данными транзакций для тестов */
public final class ClearingTransactionsTestData {

    public static List<TransactionData> getClearingTransactions() {
        List<TransactionData> transactions = new ArrayList<>();
        transactions.add(getClearingTransaction());
        return transactions;
    }

    public static TransactionData getClearingTransaction() {
        TransactionData transaction = new TransactionData();
        transaction.setTransactionId("tran_1");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionAmount(100L);
        transaction.setTransactionCurrency("RUB");
        transaction.setTransactionType("00");
        transaction.setMcc(5734);
        transaction.setMerchantId("29003001");
        transaction.setTerminalId("29003001");

        return transaction;
    }

    private ClearingTransactionsTestData() {}

}
