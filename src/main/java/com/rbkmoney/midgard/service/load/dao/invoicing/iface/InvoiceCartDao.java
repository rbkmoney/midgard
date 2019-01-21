package com.rbkmoney.midgard.service.load.dao.invoicing.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.Dao;
import org.jooq.generated.feed.tables.pojos.InvoiceCart;

import java.util.List;

public interface InvoiceCartDao extends Dao {

    void save(List<InvoiceCart> invoiceCartList) throws DaoException;

    List<InvoiceCart> getByInvId(Long invId) throws DaoException;

}
