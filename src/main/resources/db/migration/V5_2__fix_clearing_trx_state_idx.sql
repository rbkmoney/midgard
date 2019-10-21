/** Обновление индекса для таблицы с возвратами */
DROP INDEX IF EXISTS clearing_refund_state_idx;

CREATE INDEX clearing_refund_state_idx ON midgard.clearing_refund (clearing_state, invoice_id)
 WHERE clearing_state in ('READY', 'FAILED');
CREATE INDEX clearing_refund_date_idx ON midgard.clearing_refund (created_at, invoice_id, payment_id, refund_id);

/** Обновление индекса для таблицы с платежами */
DROP INDEX IF EXISTS clearing_transactions_id_idx;
DROP INDEX IF EXISTS clearing_transactions_state_idx;

CREATE INDEX clearing_transactions_state_idx ON midgard.clearing_transaction (transaction_clearing_state, invoice_id)
 WHERE transaction_clearing_state in ('READY', 'FAILED');

