package com.rbkmoney.midgard.base.clearing.helpers;

import com.rbkmoney.midgard.base.clearing.helpers.DAO.MerchantDao;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.midgard.tables.pojos.ClearingMerchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/** Вспомогательный класс для работы с таблицей мерчантов */
@Slf4j
@Component
public class MerchantHelper {

    /** Объект для работы с данными в БД */
    private final MerchantDao dao;

    @Autowired
    public MerchantHelper(DataSource dataSource) {
        dao = new MerchantDao(dataSource);
    }

    /**
     * Сохранение нового мерчанта в БД
     *
     * @param merchant новый мерчант
     */
    public void saveMerchant(ClearingMerchant merchant) {
        log.debug("Saving a merchant {}...", merchant);
        String merchantId = merchant.getMerchantId();
        ClearingMerchant tmpMerchant = dao.get(merchantId);
        if (tmpMerchant == null) {
            dao.save(merchant);
        } else {
            if (!tmpMerchant.equals(merchant)) {
                log.debug("A merchant with other data was found in the database ({}). The existing object will be " +
                        "closed and a new one will be added", tmpMerchant);
                dao.closeMerchant(merchantId);
                dao.save(merchant);
            }
        }
        log.debug("The merchant with id {} was saved", merchant.getMerchantId());
    }

    /** Получение мерчанта по его ID */
    public ClearingMerchant getMerchant(String merchantId) {
        return dao.get(merchantId);
    }

    /** Получение истории изменения состояний мерчанта */
    public List<ClearingMerchant> getMerchantHistory(String merchantId) {
        return dao.getMerchantHistory(merchantId);
    }

}
