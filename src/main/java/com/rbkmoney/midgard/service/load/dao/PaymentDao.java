package com.rbkmoney.midgard.service.load.dao;

import com.rbkmoney.midgard.service.clearing.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.Payment;

import java.util.List;

public interface PaymentDao extends Dao {

    Long save(Payment payment);

    List<Long> save(List<Payment> payments);

    Payment get(String invoiceId, String paymentId);

    void updateCommissions(Long pmntId);

    void updateNotCurrent(Long id);

}
