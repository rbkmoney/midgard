package com.rbkmoney.midgard.handler.failure;

import com.rbkmoney.midgard.dao.transaction.TransactionsDao;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingEventTransactionInfo;
import com.rbkmoney.midgard.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.rbkmoney.midgard.domain.enums.ClearingTrxType.PAYMENT;
import static com.rbkmoney.midgard.domain.enums.ClearingTrxType.REFUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceFailureTransactionHandler
        implements FailureTransactionHandler<ClearingEventTransactionInfo, String> {

    private final TransactionsDao transactionsDao;

    @Override
    public void handleTransaction(ClearingEventTransactionInfo transaction, String errorMessage) {
        try {
            saveFailureTransaction(transaction, errorMessage);
        } catch (Exception ex) {
            log.error("Received error when processing failure transaction", ex);
        }
    }

    private void saveFailureTransaction(ClearingEventTransactionInfo info, String errorMessage) throws Exception {
        switch (info.getTransactionType()) {
            case PAYMENT:
                log.error("Error was caught while clearing processed {} transaction with invoice_id {} and " +
                                "payment id {}. Reason: \n{}", info.getTransactionType(), info.getInvoiceId(),
                        info.getPaymentId(), errorMessage);
                transactionsDao.saveFailureTransaction(MappingUtils.getFailureTransaction(info, errorMessage, PAYMENT));
                break;
            case REFUND:
                log.error("Error was caught while clearing processed {} transaction with invoice_id {}, " +
                                "payment id {} and refund id {}. Reason: \n{}", info.getTransactionType(),
                        info.getInvoiceId(), info.getPaymentId(), info.getRefundId(), errorMessage);
                transactionsDao.saveFailureTransaction(MappingUtils.getFailureTransaction(info, errorMessage, REFUND));
                break;
            default:
                throw new Exception("Transaction type " + info.getTransactionType() + " not found");
        }
    }

}
