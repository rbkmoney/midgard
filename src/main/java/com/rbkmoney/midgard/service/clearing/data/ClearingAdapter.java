package com.rbkmoney.midgard.service.clearing.data;

import com.rbkmoney.midgard.ClearingAdapterSrv;
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

}
