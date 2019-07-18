/** Modificate INVOICE table */
ALTER TABLE feed.invoice ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE feed.invoice ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.invoice ADD COLUMN change_id INT;


/** Modificate Payment table */
ALTER TABLE feed.payment ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE feed.payment ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.payment ADD COLUMN change_id INT;


/** Modificate Refund table */
ALTER TABLE feed.refund ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE feed.refund ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.refund ADD COLUMN change_id INT;


/** Modificate adjustment table */
ALTER TABLE feed.adjustment ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE feed.adjustment ADD COLUMN sequence_id BIGINT;
ALTER TABLE feed.adjustment ADD COLUMN change_id INT;


/** Modificate clearing_transaction table */
ALTER TABLE midgard.clearing_transaction DROP CONSTRAINT clearing_transaction_PK;
ALTER TABLE midgard.clearing_transaction ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE midgard.clearing_transaction ADD COLUMN sequence_id BIGINT;
ALTER TABLE midgard.clearing_transaction ADD COLUMN change_id INT;


/** Modificate clearing_refund table */
ALTER TABLE midgard.clearing_refund DROP CONSTRAINT clearing_refund_pkey;
ALTER TABLE midgard.clearing_refund ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE midgard.clearing_refund ADD COLUMN sequence_id BIGINT;
ALTER TABLE midgard.clearing_refund ADD COLUMN change_id INT;
