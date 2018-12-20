package com.rbkmoney.midgard.base.clearing.importers;

import com.rbkmoney.midgard.base.clearing.data.enums.ImporterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Импортер для получения мерчантов */
@Slf4j
@RequiredArgsConstructor
@Component
public class MerchantImporter implements Importer {

    @Override
    public void getData() {

    }

    @Override
    public boolean isInstance(ImporterType type) {
        return ImporterType.MERCHANT == type;
    }

}
