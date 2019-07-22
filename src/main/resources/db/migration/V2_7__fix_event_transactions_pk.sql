ALTER TABLE midgard.clearing_event_transaction_info DROP CONSTRAINT clearing_event_transaction_info_pk;

CREATE UNIQUE INDEX IF NOT EXISTS clearing_event_transaction_info_pk_uniq
    ON midgard.clearing_event_transaction_info(clearing_id, invoice_id, payment_id, transaction_type);
