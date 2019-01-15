package com.rbkmoney.midgard.service.clearing.helpers;

import com.rbkmoney.midgard.service.clearing.helpers.DAO.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingCashFlowHelper {

    private final ClearingCashFlowDao dao;

    public void saveCashFlow(Payment payment, List<CashFlow> cashFlow) {
        List<ClearingTransactionCashFlow> tranCashFlow = cashFlow.stream()
                .map(flow -> {
                    ClearingTransactionCashFlow transactionCashFlow = Utils.transformCashFlow(flow);
                    transactionCashFlow.setSourceEventId(payment.getEventId());
                    return transactionCashFlow;
                })
                .collect(Collectors.toList());
        dao.save(tranCashFlow);
    }

    public List<ClearingTransactionCashFlow> getCashFlow(String sourceEventId) {
        return dao.get(sourceEventId);
    }

}
