package com.rbkmoney.midgard.load.DAO.invoicing.iface;

import com.rbkmoney.midgard.clearing.exception.DaoException;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.Dao;
import org.jooq.generated.feed.tables.pojos.InvoiceCart;

import java.util.List;

public interface InvoiceCartDao extends Dao {

    void save(List<InvoiceCart> invoiceCartList) throws DaoException;

    List<InvoiceCart> getByInvId(Long invId) throws DaoException;

}
