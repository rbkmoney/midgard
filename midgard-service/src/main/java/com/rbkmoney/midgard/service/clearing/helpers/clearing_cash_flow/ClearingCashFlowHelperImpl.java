package com.rbkmoney.midgard.service.clearing.helpers.clearing_cash_flow;

import com.rbkmoney.midgard.service.clearing.dao.clearing_cash_flow.ClearingCashFlowDao;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
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
public class ClearingCashFlowHelperImpl implements ClearingCashFlowHelper {

    private final ClearingCashFlowDao dao;

    public void saveCashFlow(Payment payment, List<CashFlow> cashFlow) {
        List<ClearingTransactionCashFlow> tranCashFlow = cashFlow.stream()
                .map(flow -> {
                    ClearingTransactionCashFlow transactionCashFlow = MappingUtils.transformCashFlow(flow);
                    transactionCashFlow.setSourceEventId(payment.getEventId());
                    return transactionCashFlow;
                })
                .collect(Collectors.toList());
        dao.save(tranCashFlow);
    }

}
