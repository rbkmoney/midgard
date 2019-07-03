ALTER TABLE midgard.clearing_transaction
        ADD COLUMN source_row_id BIGINT;

ALTER TABLE midgard.clearing_refund
        ADD COLUMN source_row_id BIGINT;
