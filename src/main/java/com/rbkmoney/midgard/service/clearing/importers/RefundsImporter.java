package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.payment.PaymentDao;
import com.rbkmoney.midgard.service.clearing.dao.refund.RefundDao;
import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.transformCashFlow;

@Slf4j
@RequiredArgsConstructor
@Component
public class RefundsImporter implements Importer {

    private final PaymentDao paymentDao;

    private final RefundDao refundDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ClearingCashFlowDao clearingCashFlowDao;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    private static final String TRAN_ID_NULL_ERROR = "NULL value in column 'transaction_id'";

    /**
     * Метод производит импорт данных из схемы с сырыми данными feed в целевые таблицы клирингового сервиса midgard.
     * Из таблицы feed.refunds забирается определенное количество записей, преобразовываются и добавляются в
     * таблицу midgard.clearing_refund.
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
    public boolean importData(List<Integer> providerIds) throws DaoException {
        List<Refund> refunds = refundDao.getRefunds(getLastTransactionRowId(), providerIds, poolSize);
        for (Refund refund : refunds) {
            saveClearingRefundData(refund);
        }
        log.info("Number of imported refunds {}", refunds.size());
        return refunds.size() > 0;
    }

    private void saveClearingRefundData(Refund refund) throws DaoException {
        ClearingRefund clearingRefund = MappingUtils.transformRefund(refund);
        log.info("Saving a clearing refund with sequence id '{}' and invoice id '{}'",
                refund.getSequenceId(), refund.getInvoiceId());
        log.debug("Saving a clearing refund {}", clearingRefund);

        if (clearingRefund.getTransactionId() == null) {
            clearingRefund.setClearingState(TransactionClearingState.FATAL);
            clearingRefund.setComment(TRAN_ID_NULL_ERROR);
            clearingRefund.setTransactionId(clearingRefund.getInvoiceId() + clearingRefund.getPaymentId());
            log.error("The following error was detected during save: {}. \nThe following object will be saved " +
                    "to the database: {}", TRAN_ID_NULL_ERROR, clearingRefund);
        }
        clearingRefundDao.save(clearingRefund);

        List<CashFlow> cashFlow = paymentDao.getCashFlow(refund.getId());
        List<ClearingTransactionCashFlow> transactionCashFlowList =
                transformCashFlow(cashFlow, clearingRefund.getSequenceId());
        clearingCashFlowDao.save(transactionCashFlowList);
    }

    private long getLastTransactionRowId() {
        ClearingRefund clearingRefund = clearingRefundDao.getLastTransactionEvent();
        if (clearingRefund == null) {
            log.warn("Event ID for clearing refund was not found!");
            return 0L;
        } else {
            log.info("Last refund sequence id {}", clearingRefund.getSourceRowId());
            return clearingRefund.getSourceRowId();
        }
    }

}
