package com.rbkmoney.midgard.handler;

import com.rbkmoney.midgard.data.ClearingDataPackage;

public interface ClearingPackageHandler {

    ClearingDataPackage getClearingPackage(Long clearingId, int providerId, long lastRowId, int packageNumber);

}
