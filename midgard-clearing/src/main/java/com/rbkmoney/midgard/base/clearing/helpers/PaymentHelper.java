package com.rbkmoney.midgard.base.clearing.helpers;

import com.rbkmoney.midgard.base.clearing.helpers.dao.PaymentDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentHelper {

    private final PaymentDao paymentDao;

    public List<Payment> getPayments(long eventId, int poolSize) {
        log.debug("Taking list of events with event ID {} and pool size {}", eventId, poolSize);
        return paymentDao.getPayments(eventId, poolSize);
    }

}
