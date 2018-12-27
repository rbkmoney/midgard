package com.rbkmoney.midgard.base.load.dao.invoicing.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.Payment;

public interface PaymentDao extends Dao {

    Long save(Payment payment) throws DaoException;

    Payment get(String invoiceId, String paymentId) throws DaoException;

    void updateCommissions(Long pmntId) throws DaoException;

    void updateNotCurrent(String invoiceId, String paymentId) throws DaoException;
}
