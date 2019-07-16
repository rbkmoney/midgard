/** Modificate Invoice table */
ALTER TABLE feed.invoice ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE feed.invoice ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.invoice ADD COLUMN change_id INT;

UPDATE feed.invoice SET sequence_id = event_id, change_id = id;

ALTER TABLE feed.invoice ADD CONSTRAINT invoice_uniq UNIQUE (invoice_id, sequence_id, change_id);

/** Modificate Payment table */
ALTER TABLE feed.payment ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE feed.payment ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.payment ADD COLUMN change_id INT;

UPDATE feed.payment SET sequence_id = event_id, change_id = id;

ALTER TABLE feed.payment ADD CONSTRAINT payment_uniq UNIQUE (invoice_id, sequence_id, change_id);

/** Modificate Refund table */
ALTER TABLE feed.refund ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE feed.refund ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.refund ADD COLUMN change_id INT;

UPDATE feed.refund SET sequence_id = event_id, change_id = id;

ALTER TABLE feed.refund ADD CONSTRAINT refund_uniq UNIQUE (invoice_id, sequence_id, change_id);

/** Modificate adjustment table */
ALTER TABLE feed.adjustment ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE feed.adjustment ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.adjustment ADD COLUMN change_id INT;

UPDATE feed.adjustment SET sequence_id = event_id, change_id = id;

ALTER TABLE feed.adjustment ADD CONSTRAINT adjustment_uniq UNIQUE (invoice_id, sequence_id, change_id);

/** Modificate clearing_transaction table */
ALTER TABLE midgard.clearing_transaction DROP CONSTRAINT clearing_transaction_PK;
ALTER TABLE midgard.clearing_transaction ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE midgard.clearing_transaction ADD COLUMN sequence_id BIGINT;
ALTER TABLE midgard.clearing_transaction ADD COLUMN change_id INT;

UPDATE midgard.clearing_transaction SET sequence_id = event_id, change_id = event_id;

ALTER TABLE midgard.clearing_transaction ADD CONSTRAINT clearing_transaction_uniq UNIQUE (invoice_id, sequence_id, change_id);

/** Modificate clearing_refund table */
ALTER TABLE midgard.clearing_refund DROP CONSTRAINT clearing_refund_pkey;
ALTER TABLE midgard.clearing_refund ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE midgard.clearing_refund ADD COLUMN sequence_id BIGINT;
ALTER TABLE midgard.clearing_refund ADD COLUMN change_id INT;

UPDATE midgard.clearing_refund SET sequence_id = event_id, change_id = event_id;

ALTER TABLE midgard.clearing_refund ADD CONSTRAINT clearing_refund_uniq UNIQUE (invoice_id, sequence_id, change_id);
