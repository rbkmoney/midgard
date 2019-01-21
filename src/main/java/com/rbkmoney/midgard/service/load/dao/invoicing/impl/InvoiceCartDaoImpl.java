package com.rbkmoney.midgard.service.load.dao.invoicing.impl;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.InvoiceCartDao;
import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.InvoiceCart;
import org.jooq.generated.feed.tables.records.InvoiceCartRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static org.jooq.generated.feed.Tables.INVOICE_CART;

@Component
public class InvoiceCartDaoImpl extends AbstractGenericDao implements InvoiceCartDao {

    private final RowMapper<InvoiceCart> invoiceCartRowMapper;

    @Autowired
    public InvoiceCartDaoImpl(DataSource dataSource) {
        super(dataSource);
        invoiceCartRowMapper = new RecordRowMapper<>(INVOICE_CART, InvoiceCart.class);
    }

    @Override
    public void save(List<InvoiceCart> invoiceCartList) throws DaoException {
        //todo: Batch insert
        for (InvoiceCart invoiceCart : invoiceCartList) {
            InvoiceCartRecord record = getDslContext().newRecord(INVOICE_CART, invoiceCart);
            Query query = getDslContext().insertInto(INVOICE_CART).set(record);
            execute(query);
        }
    }

    @Override
    public List<InvoiceCart> getByInvId(Long invId) throws DaoException {
        Query query = getDslContext().selectFrom(INVOICE_CART)
                .where(INVOICE_CART.INV_ID.eq(invId));
        return fetch(query, invoiceCartRowMapper);
    }
}
