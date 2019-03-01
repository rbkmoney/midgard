package com.rbkmoney.midgard.service.clearing.importers;

import java.util.List;

public interface Importer {

    void getData(List<Integer> providerIds);

}
