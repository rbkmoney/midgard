package com.rbkmoney.midgard.service.clearing.helpers.clearing_cash_flow;

import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;

import java.util.List;

public interface ClearingCashFlowHelper {

    void saveCashFlow(Payment payment, List<CashFlow> cashFlow);

}
