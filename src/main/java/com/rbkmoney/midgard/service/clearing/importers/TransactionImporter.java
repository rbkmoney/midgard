package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.payment.PaymentDao;
import com.rbkmoney.midgard.service.clearing.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionImporter implements Importer {

    private final TransactionsDao transactionsDao;

    private final PaymentDao paymentDao;

    private final ClearingCashFlowDao cashFlowDao   ;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    private static final String TRAN_ID_NULL_ERROR = "NULL value in column 'transaction_id'";

    /**
     * Метод производит импорт данных из схемы с сырыми данными feed в целевые таблицы клирингового сервиса midgard.
     * Из таблицы feed.payments забирается определенное количество записей. Далее они преобразовываются и добавляются в
     * таблицу midgard.clearing_transaction.
     *
     * Примечание: Импорт производится до тех пор пока количество полученных из таблицы схемы feed данных равно
     *             значению poolSize. Как только условие перестает выполнятся импорт завершается.
     *
     * @param providerIds список провайдеров
     * @return возвращает {@code true}, когда количество полученных из БД элементов равно максимальному размеру
     *         пачки; иначе {@code false}
     */
    @Override
    @Transactional
    public boolean importData(List<Integer> providerIds) {
        List<Payment> payments = paymentDao.getPayments(getLastTransactionRowId(), providerIds, poolSize);
        for (Payment payment : payments) {
            saveTransaction(payment);
        }
        log.info("Number of imported payments {}", payments.size());
        return !payments.isEmpty();
    }

    private void saveTransaction(Payment payment) {
        ClearingTransaction transaction = MappingUtils.transformTransaction(payment);
        log.info("Saving a clearing refund with invoice id '{}', payment id '{}' and sequence id '{}'",
                payment.getInvoiceId(), payment.getPaymentId(), payment.getSequenceId());
        log.debug("Saving a transaction {}", transaction);

        if (transaction.getTransactionId() == null) {
            transaction.setTransactionClearingState(TransactionClearingState.FATAL);
            transaction.setComment(TRAN_ID_NULL_ERROR);
            transaction.setTransactionId(transaction.getInvoiceId() + transaction.getPaymentId());
            log.error("The following error was detected during save: '{}'. \nThe following object will be saved " +
                    "to the database: {}", TRAN_ID_NULL_ERROR, transaction);
        }
        transactionsDao.save(transaction);

        List<CashFlow> cashFlow = paymentDao.getCashFlow(payment.getId());
        saveCashFlow(payment, cashFlow);
    }

    private void saveCashFlow(Payment payment, List<CashFlow> cashFlow) {
        List<ClearingTransactionCashFlow> tranCashFlow = cashFlow.stream()
                .map(flow -> MappingUtils.transformCashFlow(flow, payment.getId()))
                .collect(Collectors.toList());
        cashFlowDao.save(tranCashFlow);
    }

    private long getLastTransactionRowId() {
        ClearingTransaction clearingTransaction = transactionsDao.getLastTransaction();
        if (clearingTransaction == null) {
            log.warn("Event ID for clearing transactions was not found!");
            return 0L;
        } else {
            log.info("Last payment source row id {}", clearingTransaction.getSourceRowId());
            return clearingTransaction.getSourceRowId();
        }
    }

}
