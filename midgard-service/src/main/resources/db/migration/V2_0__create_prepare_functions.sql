CREATE OR REPLACE FUNCTION midgard.prepare_transaction_data(clearing_id bigint, provider_id varchar(100))
RETURNS VOID
LANGUAGE PLPGSQL
IMMUTABLE
PARALLEL SAFE
AS $$
BEGIN
    /** Получение списка готовых к клиринговому событию транзакций */
    WITH clearing_trx_cte as (
        SELECT    clearing_id,
                  trx.transaction_id,
                  'PAYMENT' as trx_type
        FROM      midgard.clearing_transaction trx
        WHERE     trx.provider_id = provider_id
              AND trx.transaction_clearing_state in ('READY', 'FAILED')
    ),

    clearing_refund_trx_cte as (
        SELECT    clearing_id,
                  refund_trx.transaction_id,
                  'REFUND' as trx_type
        FROM      midgard.clearing_refund refund_trx
        JOIN      midgard.clearing_transaction trx
               ON trx.provider_id = provider_id
              AND refund_trx.invoice_id = trx.invoice_id
              AND refund_trx.payment_id = trx.payment_id
        WHERE     refund_trx.transaction_clearing_state in ('READY', 'FAILED')
    ),

    ordered_clearing_trx_cte as (
        SELECT clearing_id, transaction_id, trx_type, trx_state,
               row_number() over(partition BY clearing_id ORDER BY trx_state, transaction_id) as row_num
        FROM   (
               SELECT * FROM clearing_trx_cte
                 UNION ALL
               SELECT * FROM clearing_refund_trx_cte
        ) cte
    )

    INSERT INTO midgard.clearing_event_info(clearing_id, transaction_id, transaction_type, state,  row_number)
    SELECT clearing_id, transaction_id, trx_type, trx_state, row_num
    FROM   ordered_clearing_trx_cte;

    /** Перевести статус добавленных в клиринговый эвент транзакций в статус "ACTIVE" */
    UPDATE midgard.clearing_transaction
    SET    transaction_clearing_state = 'ACTIVE'
    WHERE  transaction_id IN (SELECT transaction_id
                              FROM   midgard.clearing_event_info cei
                              WHERE  cei.clearing_id = clearing_id
                                 and transaction_type = 'PAYMENT');

    UPDATE midgard.clearing_refund
    SET    clearing_state = 'ACTIVE'
    WHERE  transaction_id IN (SELECT transaction_id
                              FROM   midgard.clearing_event_info cei
                              WHERE  cei.clearing_id = clearing_id
                                 and transaction_type = 'REFUND');

END;
$$;




