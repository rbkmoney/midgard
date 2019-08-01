package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.ClearingDataRequest;

public interface ClearingPackageHandler {

    ClearingDataRequest getClearingPackage(Long clearingId, int packageNumber);

}
