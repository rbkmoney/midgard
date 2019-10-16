package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.service.clearing.data.ClearingDataPackage;

public interface ClearingPackageHandler {

    ClearingDataPackage getClearingPackage(Long clearingId, int providerId, long lastRowId, int packageNumber);

}
