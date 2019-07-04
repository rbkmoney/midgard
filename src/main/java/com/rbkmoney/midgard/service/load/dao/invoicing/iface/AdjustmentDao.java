package com.rbkmoney.midgard.service.load.dao.invoicing.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.Adjustment;

public interface AdjustmentDao extends Dao {

    Long save(Adjustment adjustment) throws DaoException;

    Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws DaoException;

    void updateCommissions(Long adjId) throws DaoException;

    void updateNotCurrent(Long id) throws DaoException;

}
