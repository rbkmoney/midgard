package com.rbkmoney.midgard.handler.reverse;

import com.rbkmoney.midgard.ClearingOperationInfo;
import com.rbkmoney.midgard.OperationNotFound;

public interface ReverseClearingOperationHandler {

    void reverseOperation(ClearingOperationInfo clearingOperationInfo) throws OperationNotFound;

    boolean isAccept(ClearingOperationInfo clearingOperationInfo);

}
