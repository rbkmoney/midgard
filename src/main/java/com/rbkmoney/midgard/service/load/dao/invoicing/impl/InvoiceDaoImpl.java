package com.rbkmoney.midgard.service.load.dao.invoicing.impl;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.dao.common.RecordRowMapper;
import com.rbkmoney.midgard.service.load.dao.invoicing.iface.InvoiceDao;
import com.rbkmoney.midgard.service.clearing.dao.common.AbstractGenericDao;
import org.jooq.Query;
import org.jooq.generated.feed.tables.pojos.Invoice;
import org.jooq.generated.feed.tables.records.InvoiceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.jooq.generated.feed.tables.Invoice.INVOICE;

@Component
public class InvoiceDaoImpl extends AbstractGenericDao implements InvoiceDao {

    private final RowMapper<Invoice> invoiceRowMapper;

    @Autowired
    public InvoiceDaoImpl(DataSource dataSource) {
        super(dataSource);
        invoiceRowMapper = new RecordRowMapper<>(INVOICE, Invoice.class);
    }

    @Override
    public Long save(Invoice invoice) throws DaoException {
        InvoiceRecord invoiceRecord = getDslContext().newRecord(INVOICE, invoice);
        Query query = getDslContext().insertInto(INVOICE)
                .set(invoiceRecord)
                .onConflict(INVOICE.INVOICE_ID, INVOICE.CHANGE_ID, INVOICE.SEQUENCE_ID)
                .doUpdate()
                .set(invoiceRecord)
                .returning(INVOICE.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeWithReturn(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Invoice get(String invoiceId) throws DaoException {
        Query query = getDslContext().selectFrom(INVOICE)
                .where(INVOICE.INVOICE_ID.eq(invoiceId).and(INVOICE.CURRENT));
        return fetchOne(query, invoiceRowMapper);
    }

    @Override
    public void updateNotCurrent(String invoiceId) throws DaoException {
        Query query = getDslContext().update(INVOICE).set(INVOICE.CURRENT, false)
                .where(INVOICE.INVOICE_ID.eq(invoiceId).and(INVOICE.CURRENT));
        execute(query);
    }
}
