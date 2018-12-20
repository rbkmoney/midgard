package com.rbkmoney.midgard.base.load.DAO.invoicing.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.Refund;

public interface RefundDao extends Dao {

    Long save(Refund refund) throws DaoException;

    Refund get(String invoiceId, String paymentId, String refundId) throws DaoException;

    void updateCommissions(Long rfndId) throws DaoException;

    void updateNotCurrent(String invoiceId, String paymentId, String refundId) throws DaoException;
}
