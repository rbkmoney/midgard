package com.rbkmoney.midgard.service.clearing.data;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClearingAdapter {

    private ClearingAdapterSrv.Iface adapter;

    private String adapterName;

    private int adapterId;

}
