package com.rbkmoney.midgard.base.load.dao.invoicing.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.InvoiceCart;

import java.util.List;

public interface InvoiceCartDao extends Dao {

    void save(List<InvoiceCart> invoiceCartList) throws DaoException;

    List<InvoiceCart> getByInvId(Long invId) throws DaoException;

}
