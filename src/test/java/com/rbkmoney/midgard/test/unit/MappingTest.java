package com.rbkmoney.midgard.test.unit;

import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.Transaction;
import org.jooq.generated.tables.pojos.ClearingRefund;
import org.jooq.generated.tables.pojos.ClearingTransaction;
import org.junit.Test;

import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.*;
import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.*;
import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.getTestClearingTransaction;
import static com.rbkmoney.midgard.utils.MappingUtils.*;
import static org.junit.Assert.assertEquals;

public class MappingTest {

    @Test
    public void transactionMappingTest() {
        MachineEvent event = new MachineEvent();
        event.setEventId(SEQUENCE_ID_1);
        InvoicePayment payment = getInvoicePayment();
        String invoiceId = INVOICE_ID_1;
        ClearingTransaction clearingTransaction = transformTransaction(payment, event, invoiceId, CHANGE_ID_1);
        ClearingTransaction testClearingTransaction = getTestClearingTransaction();
        assertEquals("Resulting clearing transaction is not equal to the reference",
                testClearingTransaction, clearingTransaction);

        Transaction protoTransaction = transformClearingTransaction(testClearingTransaction);
        Transaction testProtoTransaction = getTestProtoTransaction();
        assertEquals("Resulting transaction is not equal to the reference",
                protoTransaction, testProtoTransaction);
    }

    @Test
    public void refundMappingTest() {
        MachineEvent event = new MachineEvent()
                .setEventId(SEQUENCE_ID_1)
                .setSourceId(INVOICE_ID_1);
        InvoicePaymentRefund refund = getInvoicePaymentRefund();
        InvoicePayment payment = getInvoicePayment();
        ClearingRefund clearingRefund = transformRefund(refund, event, payment.getPayment(), CHANGE_ID_1);
        ClearingRefund testClearingRefund = getTestClearingRefund();
        assertEquals("Resulting clearing refund is not equal to the reference",
                testClearingRefund, clearingRefund);

        Transaction transaction = transformRefundTransaction(getTestClearingTransaction(), clearingRefund);
        Transaction testProtoRefundTransaction = getTestProtoRefundTransaction();
        assertEquals("Resulting refund transaction is not equal to the reference",
                testProtoRefundTransaction, transaction);
    }

}