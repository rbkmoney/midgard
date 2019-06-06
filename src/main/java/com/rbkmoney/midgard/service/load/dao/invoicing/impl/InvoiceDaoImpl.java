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

    @Override
    public Long getLastEventId(int div, int mod) throws DaoException {
        String sql = "with event_ids as (" +
                "(select event_id from feed.invoice where ('x0'||substr(md5(invoice_id), 1, 7))::bit(32)::int % :div = :mod order by event_id desc limit 1) " +
                "union all " +
                "(select event_id from feed.payment where ('x0'||substr(md5(invoice_id), 1, 7))::bit(32)::int % :div = :mod order by event_id desc limit 1) " +
                "union all " +
                "(select event_id from feed.refund where ('x0'||substr(md5(invoice_id), 1, 7))::bit(32)::int % :div = :mod order by event_id desc limit 1) " +
                "union all " +
                "(select event_id from feed.adjustment where ('x0'||substr(md5(invoice_id), 1, 7))::bit(32)::int % :div = :mod order by event_id desc limit 1) " +
                ") " +
                "select max(event_id) from event_ids";

        return getNamedParameterJdbcTemplate().queryForObject(sql, new MapSqlParameterSource("div", div).addValue("mod", mod), Long.class);
    }

}
