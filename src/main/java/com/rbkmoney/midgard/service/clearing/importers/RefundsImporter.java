package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.dao.clearing_refund.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.dao.payment.PaymentDao;
import com.rbkmoney.midgard.service.clearing.dao.refund.RefundDao;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import com.rbkmoney.midgard.service.config.props.AdapterProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.transformCashFlow;

@Slf4j
@RequiredArgsConstructor
@Component
public class RefundsImporter implements Importer {

    private final PaymentDao paymentDao;

    private final RefundDao refundDao;

    private final ClearingRefundDao clearingRefundDao;

    private final ClearingCashFlowDao clearingCashFlowDao;

    private final List<AdapterProps> adaptersProps;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    @Override
    public void getData() {
        long eventId = getLastTransactionEventId();
        log.info("Refunds data import will start with event id {}", eventId);

        List<Integer> providerIds = adaptersProps.stream()
                .map(adapterProps -> adapterProps.getProviderId())
                .collect(Collectors.toList());

        int obtainRefundsSize;
        do {
            obtainRefundsSize = pollRefunds(eventId, providerIds);
        } while(obtainRefundsSize == poolSize);
        log.info("Refunds data import have finished");
    }

    private int pollRefunds(long eventId, List<Integer> providerIds) {
        List<Refund> refunds = refundDao.getRefunds(eventId, providerIds, poolSize);
        for (Refund refund : refunds) {
            saveClearingRefundData(refund);
        }
        return refunds.size();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveClearingRefundData(Refund refund) {
        ClearingRefund clearingRefund = MappingUtils.transformRefund(refund);
        clearingRefundDao.save(clearingRefund);
        List<CashFlow> cashFlow = paymentDao.getCashFlow(refund.getId());
        List<ClearingTransactionCashFlow> transactionCashFlowList =
                transformCashFlow(cashFlow, clearingRefund.getEventId());
        clearingCashFlowDao.save(transactionCashFlowList);
    }

    private long getLastTransactionEventId() {
        ClearingRefund clearingRefund = clearingRefundDao.getLastTransactionEvent();
        if (clearingRefund == null) {
            log.warn("Event ID for clearing refund was not found!");
            return 0L;
        } else {
            return clearingRefund.getEventId();
        }
    }

}
