package com.rbkmoney.midgard.test.unit;

import com.rbkmoney.midgard.Transaction;
import org.junit.Test;

import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.getTestClearingTransaction;
import static com.rbkmoney.midgard.test.unit.data.TestTransactionsData.getTestProtoTransaction;
import static com.rbkmoney.midgard.utils.MappingUtils.transformClearingTransaction;
import static org.junit.Assert.assertEquals;

public class MappingTest {

    @Test
    public void transactionTest() {
        //TODO: сделать тест на маппер из machineEvent -> clearingEvent

        Transaction protoTransaction = transformClearingTransaction(getTestClearingTransaction());
        Transaction testProtoTransaction = getTestProtoTransaction();
        assertEquals("Resulting transaction is not equal to the reference",
                protoTransaction, testProtoTransaction);
    }

    //TODO: сделать маппер для возврата

}