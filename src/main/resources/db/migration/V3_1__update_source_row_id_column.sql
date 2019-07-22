UPDATE midgard.clearing_transaction
SET source_row_id = payment.id
FROM feed.payment
WHERE clearing_transaction.invoice_id = payment.invoice_id
  AND clearing_transaction.payment_id = payment.payment_id
  AND payment.status = 'captured'
  and payment.current;

UPDATE midgard.clearing_refund
SET source_row_id = refund.id
FROM feed.refund
WHERE clearing_refund.invoice_id = refund.invoice_id
  AND clearing_refund.payment_id = refund.payment_id
  AND refund.status = 'succeeded'
  and refund.current;