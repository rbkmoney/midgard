package com.rbkmoney.midgard.service.clearing.helpers;

import com.rbkmoney.midgard.service.clearing.helpers.DAO.PaymentDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.CashFlow;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentHelper {

    private final PaymentDao paymentDao;

    public List<Payment> getPayments(long eventId, List<Integer> providerIds, int poolSize) {
        log.debug("Taking list of events with event ID {} and pool size {}", eventId, poolSize);
        return paymentDao.getPayments(eventId, providerIds, poolSize);
    }

    public List<CashFlow> getCashFlow(long objId) {
        return paymentDao.getCashFlow(objId);
    }

}
