package com.rbkmoney.midgard.base.clearing.helpers;

import com.rbkmoney.midgard.base.clearing.helpers.dao.MerchantDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.ClearingMerchant;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class MerchantHelper {

    private final MerchantDao dao;

    public void saveMerchant(ClearingMerchant merchant) {
        log.debug("Saving a merchant {}...", merchant);
        String merchantId = merchant.getMerchantId();
        ClearingMerchant tmpMerchant = dao.get(merchantId);
        if (tmpMerchant == null) {
            dao.save(merchant);
        } else if (!tmpMerchant.equals(merchant)) {
            log.debug("A merchant with other data was found in the database ({}). The existing object will be " +
                    "closed and a new one will be added", tmpMerchant);
            dao.closeMerchant(merchantId);
            dao.save(merchant);
        }
        log.debug("The merchant with id {} was saved", merchant.getMerchantId());
    }

    public ClearingMerchant getMerchant(String merchantId) {
        return dao.get(merchantId);
    }

    public List<ClearingMerchant> getMerchantHistory(String merchantId) {
        return dao.getMerchantHistory(merchantId);
    }

    public long getMaxMerchantEventId() {
        Long eventId = dao.getMaxMerchantEventId();
        if (eventId == null) {
            log.warn("Event ID for clearing merchants was not found!");
            return 0L;
        } else {
            return eventId;
        }
    }

}
