DO $$
BEGIN
    /** Modification Invoice table */
    IF ((SELECT count(column_name)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE table_schema = 'feed' AND table_name = 'invoice' AND column_name = 'sequence_id') = 0)
    THEN
        ALTER TABLE feed.invoice ALTER COLUMN event_id DROP NOT NULL;
        ALTER TABLE feed.invoice ADD COLUMN sequence_id BIGINT;
        ALTER TABLE feed.invoice ADD COLUMN change_id INT;
        CREATE UNIQUE INDEX IF NOT EXISTS invoice_uniq ON feed.invoice(invoice_id, sequence_id, change_id);
    END IF;

    /** Modification Payment table */
    IF ((SELECT count(column_name)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE table_schema = 'feed' AND table_name = 'payment' AND column_name = 'sequence_id') = 0)
    THEN
        ALTER TABLE feed.payment ALTER COLUMN event_id DROP NOT NULL;
        ALTER TABLE feed.payment ADD COLUMN sequence_id BIGINT;
        ALTER TABLE feed.payment ADD COLUMN change_id INT;
        CREATE UNIQUE INDEX IF NOT EXISTS payment_uniq ON feed.payment(invoice_id, sequence_id, change_id);
    END IF;


    /** Modification Refund table */
    IF ((SELECT count(column_name)
         FROM INFORMATION_SCHEMA.COLUMNS
         WHERE table_schema = 'feed' AND table_name = 'refund' AND column_name = 'sequence_id') = 0)
    THEN
        ALTER TABLE feed.refund ALTER COLUMN event_id DROP NOT NULL;
        ALTER TABLE feed.refund ADD COLUMN sequence_id BIGINT;
        ALTER TABLE feed.refund ADD COLUMN change_id INT;
        CREATE UNIQUE INDEX IF NOT EXISTS refund_uniq ON feed.refund(invoice_id, sequence_id, change_id);
    END IF;


    /** Modification adjustment table */
    IF ((SELECT count(column_name)
         FROM INFORMATION_SCHEMA.COLUMNS
         WHERE table_schema = 'feed' AND table_name = 'adjustment' AND column_name = 'sequence_id') = 0)
    THEN
        ALTER TABLE feed.adjustment ALTER COLUMN event_id DROP NOT NULL;
        ALTER TABLE feed.adjustment ADD COLUMN sequence_id BIGINT;
        ALTER TABLE feed.adjustment ADD COLUMN change_id INT;
        CREATE UNIQUE INDEX IF NOT EXISTS adjustment_uniq ON feed.adjustment(invoice_id, sequence_id, change_id);
    END IF;


    /** Modification clearing_transaction table */
    IF ((SELECT count(column_name)
         FROM INFORMATION_SCHEMA.COLUMNS
         WHERE table_schema = 'midgard' AND table_name = 'clearing_transaction' AND column_name = 'sequence_id') = 0)
    THEN
        ALTER TABLE midgard.clearing_transaction DROP CONSTRAINT clearing_transaction_PK;
        ALTER TABLE midgard.clearing_transaction ALTER COLUMN event_id DROP NOT NULL;
        ALTER TABLE midgard.clearing_transaction ADD COLUMN sequence_id BIGINT;
        ALTER TABLE midgard.clearing_transaction ADD COLUMN change_id INT;
        CREATE UNIQUE INDEX IF NOT EXISTS clearing_transaction_uniq ON midgard.clearing_transaction(invoice_id, sequence_id, change_id);
    END IF;


    /** Modification clearing_refund table */
    IF ((SELECT count(column_name)
         FROM INFORMATION_SCHEMA.COLUMNS
         WHERE table_schema = 'midgard' AND table_name = 'clearing_refund' AND column_name = 'sequence_id') = 0)
    THEN
        ALTER TABLE midgard.clearing_refund DROP CONSTRAINT clearing_refund_pkey;
        ALTER TABLE midgard.clearing_refund ALTER COLUMN event_id DROP NOT NULL;
        ALTER TABLE midgard.clearing_refund ADD COLUMN sequence_id BIGINT;
        ALTER TABLE midgard.clearing_refund ADD COLUMN change_id INT;
        CREATE UNIQUE INDEX IF NOT EXISTS clearing_refund_uniq ON midgard.clearing_refund(invoice_id, sequence_id, change_id);
    END IF;
END; $$
LANGUAGE plpgsql;

