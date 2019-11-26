package com.rbkmoney.midgard.service.clearing.utils;

import com.rbkmoney.midgard.service.clearing.data.ClearingAdapter;
import com.rbkmoney.midgard.service.clearing.exception.AdapterNotFoundException;

import java.util.List;

public final class ClearingAdaptersUtils {

    public static ClearingAdapter getClearingAdapter(List<ClearingAdapter> adapters, int providerId)
            throws AdapterNotFoundException {
        return adapters.stream()
                .filter(clrAdapter -> clrAdapter.getAdapterId() == providerId)
                .findFirst()
                .orElseThrow(() ->
                        new AdapterNotFoundException(String.format("Adapter with provider id %d not found", providerId)));
    }

    private ClearingAdaptersUtils() {}

}
