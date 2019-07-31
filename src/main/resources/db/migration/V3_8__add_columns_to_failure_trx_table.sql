ALTER TABLE midgard.failure_transaction ADD COLUMN invoice_id        CHARACTER VARYING;
ALTER TABLE midgard.failure_transaction ADD COLUMN payment_id        CHARACTER VARYING;
ALTER TABLE midgard.failure_transaction ADD COLUMN refund_id         CHARACTER VARYING;
ALTER TABLE midgard.failure_transaction ADD COLUMN transaction_type  clearing_trx_type;
ALTER TABLE midgard.failure_transaction ADD COLUMN error_reason      CHARACTER VARYING;

ALTER TABLE midgard.failure_transaction DROP COLUMN reason;
