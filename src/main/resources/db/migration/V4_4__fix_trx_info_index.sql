DROP INDEX IF EXISTS clearing_event_transaction_info_uniq;

CREATE UNIQUE INDEX IF NOT EXISTS clearing_event_transaction_info_uniq
    ON midgard.clearing_event_transaction_info(clearing_id, invoice_id, payment_id, refund_id, transaction_type, trx_version);
