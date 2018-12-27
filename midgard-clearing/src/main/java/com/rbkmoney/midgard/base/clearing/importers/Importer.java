package com.rbkmoney.midgard.base.clearing.importers;

import com.rbkmoney.midgard.base.clearing.data.enums.ImporterType;

public interface Importer {

    void getData();

    boolean isInstance(ImporterType type);

}
