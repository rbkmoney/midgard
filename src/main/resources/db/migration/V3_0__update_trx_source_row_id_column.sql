UPDATE midgard.clearing_transaction
SET source_row_id = payment.id
FROM feed.payment
WHERE clearing_transaction.invoice_id = payment.invoice_id
  AND clearing_transaction.payment_id = payment.payment_id
  AND payment.status = 'captured'
  and payment.current;
