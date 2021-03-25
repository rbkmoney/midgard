package com.rbkmoney.midgard.test.unit;

import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.midgard.Transaction;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingRefund;
import com.rbkmoney.midgard.domain.tables.pojos.ClearingTransaction;
import org.junit.Test;

import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.CHANGE_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.INVOICE_ID_1;
import static com.rbkmoney.midgard.test.unit.data.InvoiceTestConstant.SEQUENCE_ID_1;
import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.getInvoicePayment;
import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.getInvoicePaymentRefund;
import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.getTestClearingRefund;
import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.getTestClearingTransaction;
import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.getTestProtoRefundTransaction;
import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.getTestProtoTransaction;
import static com.rbkmoney.midgard.utils.MappingUtils.transformClearingTransaction;
import static com.rbkmoney.midgard.utils.MappingUtils.transformRefund;
import static com.rbkmoney.midgard.utils.MappingUtils.transformRefundTransaction;
import static com.rbkmoney.midgard.utils.MappingUtils.transformTransaction;
import static org.junit.Assert.assertEquals;

public class MappingTest {

    private static final int DEFAULT_PROVIDER_ID = 1;

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
        ClearingRefund clearingRefund =
                transformRefund(refund, event, payment.getPayment(), CHANGE_ID_1, DEFAULT_PROVIDER_ID);
        ClearingRefund testClearingRefund = getTestClearingRefund();
        assertEquals("Resulting clearing refund is not equal to the reference",
                testClearingRefund, clearingRefund);

        Transaction transaction = transformRefundTransaction(getTestClearingTransaction(), clearingRefund);
        Transaction testProtoRefundTransaction = getTestProtoRefundTransaction();
        assertEquals("Resulting refund transaction is not equal to the reference",
                testProtoRefundTransaction, transaction);
    }

}