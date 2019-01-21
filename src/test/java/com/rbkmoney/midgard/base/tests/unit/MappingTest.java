package com.rbkmoney.midgard.base.tests.unit;

import com.rbkmoney.midgard.Transaction;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.ClearingTransactionCashFlow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.rbkmoney.midgard.base.tests.unit.data.TestTransactionsData.*;
import static com.rbkmoney.midgard.service.clearing.utils.MappingUtils.*;
import static org.junit.Assert.assertEquals;

public class MappingTest {

    @Test
    public void transactionTest() {
        Payment payment = getTestPayment();
        ClearingTransaction transaction = transformTransaction(payment);
        ClearingTransaction testClearingTransaction = getTestClearingTransaction();
        assertEquals("Resulting clearing transaction is not equal to the reference",
                transaction, testClearingTransaction);

        List<ClearingTransactionCashFlow> clearingTransactionCashFlowList = new ArrayList<>();
        clearingTransactionCashFlowList.add(getTestClearingTransactionCashFlow());
        Transaction protoTransaction = transformClearingTransaction(transaction, clearingTransactionCashFlowList);
        Transaction testProtoTransaction = getTestProtoTransaction();
        assertEquals("Resulting transaction is not equal to the reference",
                protoTransaction, testProtoTransaction);
    }

    @Test
    public void refundTransactionTest() {
        Refund testRefund = getTestRefund();
        ClearingRefund clearingRefund = transformRefund(testRefund);
        ClearingRefund testClearingRefund = getTestClearingRefund();
        assertEquals("Resulting clearing refund transaction is not equal to the reference",
                clearingRefund, testClearingRefund);
    }

}