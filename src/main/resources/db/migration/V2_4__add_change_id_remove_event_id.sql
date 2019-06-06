ALTER TABLE feed.invoice ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.invoice ADD COLUMN change_id INT;
ALTER TABLE feed.invoice DROP COLUMN event_id;
ALTER TABLE feed.invoice ADD CONSTRAINT invoice_uniq UNIQUE (invoice_id, sequence_id, change_id);

ALTER TABLE feed.payment ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.payment ADD COLUMN change_id INT;
ALTER TABLE feed.payment DROP COLUMN event_id;
ALTER TABLE feed.payment ADD CONSTRAINT payment_uniq UNIQUE (invoice_id, sequence_id, change_id);

ALTER TABLE feed.refund ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.refund ADD COLUMN change_id INT;
ALTER TABLE feed.refund DROP COLUMN event_id;
ALTER TABLE feed.refund ADD CONSTRAINT refund_uniq UNIQUE (invoice_id, sequence_id, change_id);

ALTER TABLE feed.adjustment ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.adjustment ADD COLUMN change_id INT;
ALTER TABLE feed.adjustment DROP COLUMN event_id;
ALTER TABLE feed.adjustment ADD CONSTRAINT adjustment_uniq UNIQUE (invoice_id, sequence_id, change_id);

ALTER TABLE midgard.clearing_transaction DROP COLUMN event_id;
ALTER TABLE midgard.clearing_transaction ADD COLUMN sequence_id BIGINT PRIMARY KEY;
ALTER TABLE midgard.clearing_transaction ADD COLUMN change_id INT;
ALTER TABLE midgard.clearing_transaction ADD CONSTRAINT clearing_transaction_uniq UNIQUE (invoice_id, sequence_id, change_id);

ALTER TABLE midgard.clearing_refund DROP COLUMN event_id;
ALTER TABLE midgard.clearing_refund ADD COLUMN sequence_id BIGINT PRIMARY KEY;
ALTER TABLE midgard.clearing_refund ADD COLUMN change_id INT;
ALTER TABLE midgard.clearing_refund ADD CONSTRAINT clearing_refund_uniq UNIQUE (invoice_id, sequence_id, change_id);
