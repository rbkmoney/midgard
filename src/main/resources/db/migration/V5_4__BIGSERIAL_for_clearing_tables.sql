ALTER TABLE midgard.clearing_transaction DROP COLUMN source_row_id;
ALTER TABLE midgard.clearing_transaction ADD COLUMN id BIGSERIAL PRIMARY KEY;

ALTER TABLE midgard.clearing_refund DROP COLUMN source_row_id;
ALTER TABLE midgard.clearing_refund ADD COLUMN id BIGSERIAL PRIMARY KEY;
