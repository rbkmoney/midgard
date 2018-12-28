package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.data.enums.ImporterType;
import com.rbkmoney.midgard.service.clearing.helpers.MerchantHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MerchantImporter implements Importer {

    private final MerchantHelper merchantHelper;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    @Override
    public void getData() {
        long eventId = merchantHelper.getLastMerchantEventId();
        log.info("Merchant data import will start with event id {}", eventId);

    }

    @Override
    public boolean isInstance(ImporterType type) {
        return ImporterType.MERCHANT == type;
    }

}
