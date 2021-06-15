package com.rbkmoney.midgard.data;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.midgard.config.props.ClearingServiceProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClearingAdapter {

    private ClearingAdapterSrv.Iface adapter;
    private String adapterName;
    private int adapterId;
    private int packageSize;
    private ClearingServiceProperties.ExcludeOperationParams excludeOperationParams;

}
