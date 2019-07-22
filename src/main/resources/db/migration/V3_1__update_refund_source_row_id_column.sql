UPDATE midgard.clearing_refund
SET source_row_id = refund.id
FROM feed.refund
WHERE clearing_refund.invoice_id = refund.invoice_id
  AND clearing_refund.payment_id = refund.payment_id
  AND refund.status = 'succeeded'
  and refund.current;
