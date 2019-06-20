UPDATE midgard.clearing_event_transaction_info
SET invoice_id = payment.invoice_id,
    payment_id = payment.payment_id
FROM feed.payment
WHERE clearing_event_transaction_info.invoice_id is null
  and clearing_event_transaction_info.transaction_type = 'PAYMENT'
  and payment.session_payload_transaction_bound_trx_id = clearing_event_transaction_info.transaction_id
  and payment.current;

UPDATE midgard.clearing_event_transaction_info
SET invoice_id = refund.invoice_id,
	payment_id = refund.payment_id
FROM feed.refund
WHERE clearing_event_transaction_info.invoice_id is null
  and clearing_event_transaction_info.transaction_type = 'REFUND'
  and refund.session_payload_transaction_bound_trx_id = clearing_event_transaction_info.transaction_id
  and refund.current;