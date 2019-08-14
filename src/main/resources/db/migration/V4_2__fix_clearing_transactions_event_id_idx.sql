/** Обновление индекса для таблицы с платежами - добавление версионирования транзакций */
DROP INDEX IF EXISTS clearing_transactions_event_id_idx;

CREATE UNIQUE INDEX clearing_transactions_event_id_idx ON midgard.clearing_transaction (invoice_id, payment_id, trx_version);

/** Обновление индекса для таблицы с возвратами - добавление версионирования транзакций */
DROP INDEX IF EXISTS clearing_refund_state_idx;
DROP INDEX IF EXISTS clearing_refund_pkey;

CREATE INDEX clearing_refund_state_idx ON midgard.clearing_refund (payment_id, invoice_id, event_id, trx_version);
