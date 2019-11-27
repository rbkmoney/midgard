ALTER TABLE midgard.clearing_transaction DROP COLUMN source_row_id;
ALTER TABLE midgard.clearing_transaction ADD COLUMN id BIGSERIAL NOT NULL;
CREATE UNIQUE INDEX clearing_transaction_id_idx ON midgard.clearing_transaction (id);

ALTER TABLE midgard.clearing_refund DROP COLUMN source_row_id;
ALTER TABLE midgard.clearing_refund ADD COLUMN id BIGSERIAL NOT NULL;
CREATE UNIQUE INDEX clearing_refund_id_idx ON midgard.clearing_refund (id);
