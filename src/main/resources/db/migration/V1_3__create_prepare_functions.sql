CREATE OR REPLACE FUNCTION midgard.prepare_transaction_data(src_clearing_id bigint, src_provider_id int)
RETURNS VOID
LANGUAGE PLPGSQL
VOLATILE
PARALLEL SAFE
AS $$
BEGIN
    /** Получение списка готовых к клиринговому событию транзакций */
    WITH clearing_trx_cte as (
        SELECT    src_clearing_id as clearing_id,
                  trx.transaction_id,
                  cast('PAYMENT' as midgard.clearing_trx_type) as trx_type
        FROM      midgard.clearing_transaction trx
        WHERE     trx.provider_id = src_provider_id
              AND trx.transaction_clearing_state in ('READY', 'FAILED')
    ),

    clearing_refund_trx_cte as (
        SELECT    src_clearing_id as clearing_id,
                  refund_trx.transaction_id,
                  cast('REFUND' as midgard.clearing_trx_type) as trx_type
        FROM      midgard.clearing_refund refund_trx
        JOIN      midgard.clearing_transaction trx
               ON trx.provider_id = src_provider_id
              AND refund_trx.invoice_id = trx.invoice_id
              AND refund_trx.payment_id = trx.payment_id
        WHERE     refund_trx.clearing_state in ('READY', 'FAILED')
    ),

    ordered_clearing_trx_cte as (
        SELECT clearing_id, transaction_id, trx_type,
               row_number() over(partition BY clearing_id ORDER BY trx_type, transaction_id) as row_num
        FROM   (
               SELECT clearing_id, transaction_id, trx_type
               FROM clearing_trx_cte
                 UNION ALL
               SELECT clearing_id, transaction_id, trx_type
               FROM clearing_refund_trx_cte
        ) cte
    )

    INSERT INTO midgard.clearing_event_transaction_info(clearing_id, transaction_id, transaction_type, row_number)
    SELECT clearing_id, transaction_id, trx_type, row_num
    FROM   ordered_clearing_trx_cte;

    /** Перевести статус добавленных в клиринговый эвент транзакций в статус "ACTIVE" */
    UPDATE midgard.clearing_transaction
    SET    transaction_clearing_state = cast('ACTIVE' as midgard.transaction_clearing_state),
           clearing_id = src_clearing_id
    WHERE  transaction_id IN (SELECT transaction_id
                              FROM   midgard.clearing_event_transaction_info cei
                              WHERE  cei.clearing_id = src_clearing_id
                                 and transaction_type = 'PAYMENT');

    UPDATE midgard.clearing_refund
    SET    clearing_state = cast('ACTIVE' as midgard.transaction_clearing_state)
    WHERE  transaction_id IN (SELECT transaction_id
                              FROM   midgard.clearing_event_transaction_info cei
                              WHERE  cei.clearing_id = src_clearing_id
                                 and transaction_type = 'REFUND');

END;
$$;




