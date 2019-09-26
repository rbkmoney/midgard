/** Исправления для таблицы midgard.clearing_transaction */
DROP INDEX IF EXISTS midgard.clearing_transaction_uniq;

CREATE UNIQUE INDEX IF NOT EXISTS clearing_transaction_uniq
           ON midgard.clearing_transaction(invoice_id, sequence_id, change_id, trx_version);

ALTER TABLE midgard.clearing_transaction DROP CONSTRAINT IF EXISTS clearing_transaction_source_row_id_idx;

CREATE UNIQUE INDEX IF NOT EXISTS clearing_transaction_source_row_id_idx
           ON midgard.clearing_transaction(source_row_id, trx_version);

/** Исправления для таблицы midgard.clearing_refund */
DROP INDEX IF EXISTS midgard.clearing_refund_uniq;

CREATE UNIQUE INDEX IF NOT EXISTS clearing_refund_uniq
           ON midgard.clearing_refund(invoice_id, sequence_id, change_id, trx_version);

ALTER TABLE midgard.clearing_refund DROP CONSTRAINT IF EXISTS clearing_refund_source_row_id_idx;

CREATE UNIQUE INDEX IF NOT EXISTS clearing_refund_source_row_id_idx
           ON midgard.clearing_refund(source_row_id, trx_version);

