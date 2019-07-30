ALTER TABLE midgard.failure_transaction DROP CONSTRAINT failure_transaction_PK;

DROP INDEX IF EXISTS failure_transaction_idx;

CREATE UNIQUE INDEX IF NOT EXISTS failure_transaction_uniq_idx
    ON midgard.failure_transaction(clearing_id, invoice_id, payment_id, refund_id);
