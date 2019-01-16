package com.rbkmoney.midgard.service.load.dao.invoicing.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.Refund;

public interface RefundDao extends Dao {

    Long save(Refund refund) throws DaoException;

    Refund get(String invoiceId, String paymentId, String refundId) throws DaoException;

    void updateCommissions(Long rfndId) throws DaoException;

    void updateNotCurrent(String invoiceId, String paymentId, String refundId) throws DaoException;
}
