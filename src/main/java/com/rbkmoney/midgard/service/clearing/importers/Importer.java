package com.rbkmoney.midgard.service.clearing.importers;

import java.util.List;

public interface Importer {

    boolean importData(List<Integer> providerIds);

}
