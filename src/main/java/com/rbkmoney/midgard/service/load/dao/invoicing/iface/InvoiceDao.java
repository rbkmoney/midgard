package com.rbkmoney.midgard.service.load.dao.invoicing.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.Invoice;

public interface InvoiceDao extends Dao {

    Long save(Invoice invoice) throws DaoException;

    Invoice get(String invoiceId) throws DaoException;

    void updateNotCurrent(String invoiceId) throws DaoException;

    boolean isExist(Long sequenceId, String invoiceId, Integer changeId) throws DaoException;

    Long getLastEventId(int div, int mod) throws DaoException;
}
