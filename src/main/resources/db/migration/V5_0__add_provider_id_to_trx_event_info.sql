ALTER TABLE midgard.clearing_event_transaction_info ADD COLUMN provider_id INTEGER;
ALTER TABLE midgard.clearing_event_transaction_info ALTER COLUMN row_number TYPE BIGINT;

ALTER TABLE midgard.clearing_refund ADD COLUMN provider_id INTEGER;
ALTER TABLE midgard.clearing_refund ALTER COLUMN clearing_id TYPE BIGINT;

