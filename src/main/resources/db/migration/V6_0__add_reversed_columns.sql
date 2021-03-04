ALTER TABLE midgard.clearing_transaction ADD COLUMN is_reversed BOOLEAN DEFAULT false;

ALTER TABLE midgard.clearing_refund ADD COLUMN is_reversed BOOLEAN DEFAULT false;