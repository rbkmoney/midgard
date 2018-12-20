package com.rbkmoney.midgard.base.load.DAO.invoicing.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.Adjustment;

public interface AdjustmentDao extends Dao {

    Long save(Adjustment adjustment) throws DaoException;

    Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws DaoException;

    void updateCommissions(Long adjId) throws DaoException;

    void updateNotCurrent(String invoiceId, String paymentId, String adjustmentId) throws DaoException;

}
