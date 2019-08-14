ALTER TABLE midgard.clearing_transaction ADD COLUMN route_terminal_id INT;
ALTER TABLE midgard.clearing_transaction ADD COLUMN is_recurrent BOOL;
ALTER TABLE midgard.clearing_transaction ADD COLUMN payer_type CHARACTER VARYING;
ALTER TABLE midgard.clearing_transaction ADD COLUMN payer_recurrent_parent_invoice_id CHARACTER VARYING;
ALTER TABLE midgard.clearing_transaction ADD COLUMN payer_recurrent_parent_payment_id CHARACTER VARYING;
ALTER TABLE midgard.clearing_transaction ADD COLUMN trx_version INT;

ALTER TABLE midgard.clearing_refund ADD COLUMN clearing_id INT;
ALTER TABLE midgard.clearing_refund ADD COLUMN trx_version INT;
