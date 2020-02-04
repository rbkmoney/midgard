package com.rbkmoney.midgard.utils;

import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.exception.AdapterNotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClearingAdaptersUtils {

    public static ClearingAdapter getClearingAdapter(List<ClearingAdapter> adapters, int providerId)
            throws AdapterNotFoundException {
        return adapters.stream()
                .filter(clrAdapter -> clrAdapter.getAdapterId() == providerId)
                .findFirst()
                .orElseThrow(() ->
                        new AdapterNotFoundException(
                                String.format("Adapter with provider id %d not found", providerId))
                );
    }

}
