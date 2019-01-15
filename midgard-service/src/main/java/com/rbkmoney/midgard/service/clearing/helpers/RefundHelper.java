package com.rbkmoney.midgard.service.clearing.helpers;

import com.rbkmoney.midgard.service.clearing.helpers.DAO.ClearingRefundDao;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.RefundDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RefundHelper {

    private final RefundDAO refundDAO;

    private final ClearingRefundDao clearingRefundDao;

    public List<Refund> getRefunds(long eventId, List<Integer> providerIds, int poolSize) {
        log.debug("Taking list of refund events with event ID {} and pool size {}", eventId, poolSize);
        return refundDAO.getRefunds(eventId, providerIds, poolSize);
    }

    public void saveClearingRefund(ClearingRefund clearingRefund) {
        clearingRefundDao.save(clearingRefund);
    }

}
