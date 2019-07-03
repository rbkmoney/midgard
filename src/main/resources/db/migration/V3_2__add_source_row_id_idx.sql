ALTER TABLE midgard.clearing_transaction
        ADD CONSTRAINT clearing_transaction_source_row_id_idx UNIQUE (source_row_id);

ALTER TABLE midgard.clearing_refund
        ADD CONSTRAINT clearing_refund_source_row_id_idx UNIQUE (source_row_id);
