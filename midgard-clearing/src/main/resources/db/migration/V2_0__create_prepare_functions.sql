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
                  trx.merchant_id,
                  CASE WHEN merch.merchant_id IS NULL
                      THEN 'REFUSED'
                      ELSE 'PROCESSED'
                  END AS trx_state
        FROM      midgard.clearing_transaction trx
        LEFT JOIN midgard.merchant merch
               ON merch.status = 'OPEN'
              AND trx.merchant_id = merch.merchant_id
        WHERE     trx.provider_id = provider_id
              AND trx.transaction_clearing_state in ('READY', 'FAILED')
    ),

    ordered_clearing_trx_cte as (
        SELECT clearing_id, transaction_id, merchant_id, trx_state,
               row_number() over(partition BY clearing_id ORDER BY trx_state, merchant_id, transaction_id) as row_num
        FROM   clearing_trx_cte cte
    )

    INSERT INTO midgard.clearing_event_info(clearing_id, transaction_id, merchant_id, state,  row_number)
    SELECT clearing_id, transaction_id, merchant_id, trx_state, row_num
    FROM   ordered_clearing_trx_cte;

    /** Добавление в список сбойных трназаций тех, для которых не найдено соответствующего мерчанта */
    INSERT INTO midgard.failure_transaction(clearing_id, transaction_id, reason)
    SELECT clearing_id, transaction_id, 'Not found merchant'
    FROM   midgard.clearing_event_info cei
    WHERE  cei.clearing_id = clearing_id AND state = 'REFUSED';

    /** Перевести статус добавленных в клиринговый эвент транзакций в статус "ACTIVE" */
    UPDATE midgard.clearing_transaction
    SET    transaction_clearing_state = 'ACTIVE'
    WHERE  transaction_id IN (SELECT transaction_id
                              FROM   midgard.clearing_event_info cei
                              WHERE  cei.clearing_id = clearing_id and state = 'PROCESSED');

    UPDATE midgard.clearing_transaction
    SET    transaction_clearing_state = 'FAILED'
    WHERE  transaction_id IN (SELECT transaction_id
                              FROM   midgard.clearing_event_info cei
                              WHERE  cei.clearing_id = clearing_id and state = 'REFUSED');

    --TODO: вохможно имеет смысл удалить REFUSE транзакции, так как информация о них уже существует в failure_transaction
END;
$$;




