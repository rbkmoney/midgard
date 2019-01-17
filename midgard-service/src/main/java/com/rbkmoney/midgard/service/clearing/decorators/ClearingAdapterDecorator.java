package com.rbkmoney.midgard.service.clearing.decorators;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClearingAdapterDecorator {

    private ClearingAdapterSrv.Iface adapter;

    private String adapterName;

    private int adapterId;

}
