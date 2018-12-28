package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.data.enums.ImporterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Импортер для получения транзакций готовых к проведению клиринга */
@Slf4j
@RequiredArgsConstructor
@Component
public class ClearingTransactionImporter implements Importer {

    @Override
    public void getData() {

    }

    @Override
    public boolean isInstance(ImporterType type) {
        return ImporterType.TRANSACTION == type;
    }

}
