package com.rbkmoney.midgard.data;

import com.rbkmoney.midgard.ClearingDataRequest;
import lombok.Data;

@Data
public class ClearingDataPackage {

    private ClearingDataRequest clearingDataRequest;
    private long lastRowId;

}
