DROP INDEX IF EXISTS clearing_refund_state_idx;
CREATE UNIQUE INDEX clearing_refund_state_idx ON midgard.clearing_refund (invoice_id, payment_id, refund_id, trx_version);

DROP INDEX IF EXISTS clearing_transaction_source_row_id_idx;
DROP INDEX IF EXISTS clearing_refund_source_row_id_idx;
