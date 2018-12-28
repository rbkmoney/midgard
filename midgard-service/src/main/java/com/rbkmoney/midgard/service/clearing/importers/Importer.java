package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.data.enums.ImporterType;

public interface Importer {

    void getData();

    boolean isInstance(ImporterType type);

}
