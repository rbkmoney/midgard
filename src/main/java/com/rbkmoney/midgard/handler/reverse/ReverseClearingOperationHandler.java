package com.rbkmoney.midgard.handler.reverse;

import com.rbkmoney.midgard.ClearingOperationInfo;

public interface ReverseClearingOperationHandler {

    void reverseOperation(ClearingOperationInfo clearingOperationInfo);

    boolean isAccept(ClearingOperationInfo clearingOperationInfo);

}
