package com.rbkmoney.midgard.load.DAO.invoicing.iface;

import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.Payment;

public interface PaymentDao extends Dao {

    Long save(Payment payment) throws DaoException;

    Payment get(String invoiceId, String paymentId) throws DaoException;

    void updateCommissions(Long pmntId) throws DaoException;

    void updateNotCurrent(String invoiceId, String paymentId) throws DaoException;
}
