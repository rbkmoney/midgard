package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.data.enums.ImporterType;

import java.util.List;

public interface Importer {

    void getData(/*List<Integer> providerIds*/);

    boolean isInstance(ImporterType type);

}
