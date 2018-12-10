package com.rbkmoney.midgard.load.DAO.invoicing.iface;

import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.Refund;

public interface RefundDao extends Dao {

    Long save(Refund refund) throws DaoException;

    Refund get(String invoiceId, String paymentId, String refundId) throws DaoException;

    void updateCommissions(Long rfndId) throws DaoException;

    void updateNotCurrent(String invoiceId, String paymentId, String refundId) throws DaoException;
}
