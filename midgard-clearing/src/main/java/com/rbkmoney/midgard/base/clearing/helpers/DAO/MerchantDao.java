package com.rbkmoney.midgard.base.clearing.helpers.DAO;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.ClearingDao;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.RecordRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.generated.midgard.tables.pojos.ClearingMerchant;
import org.jooq.generated.midgard.tables.records.ClearingMerchantRecord;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.generated.midgard.Tables.CLEARING_MERCHANT;
import static org.jooq.generated.midgard.enums.MerchantState.*;

/**
 * Класс для взаимодействия с таблицей merchant.
 * В данной таблице хранится информация о точках, в которых была инициирована транзакция
 */
@Slf4j
public class MerchantDao extends AbstractGenericDao implements ClearingDao<ClearingMerchant> {

    /** Маппер */
    private final RowMapper<ClearingMerchant> merchantRowMapper;

    public MerchantDao(DataSource dataSource) {
        super(dataSource);
        merchantRowMapper = new RecordRowMapper<>(CLEARING_MERCHANT, ClearingMerchant.class);
    }

    @Override
    public Long save(ClearingMerchant merchant) throws DaoException {
        log.debug("Adding new merchant: {}", merchant);
        ClearingMerchantRecord record = getDslContext().newRecord(CLEARING_MERCHANT, merchant);
        Query query = getDslContext().insertInto(CLEARING_MERCHANT).set(record);
        execute(query);
        log.debug("New merchant with id {} was added", merchant.getMerchantId());
        return 0L;
    }

    @Override
    public ClearingMerchant get(String merchantId) throws DaoException {
        log.debug("Getting a merchant with id {}", merchantId);
        Query query = getDslContext().selectFrom(CLEARING_MERCHANT)
                .where(CLEARING_MERCHANT.MERCHANT_ID.eq(merchantId).and(CLEARING_MERCHANT.STATUS.eq(OPEN)));
        ClearingMerchant merchants = fetchOne(query, merchantRowMapper);
        log.debug("A merchant with id {} {}", merchantId, merchants == null ? "not found" : "found");
        return merchants;
    }

    /**
     * Закрытие версии мерчанта. Используется в ситуациях, когда пришли обновленные данные
     *
     * @param merchantId идентификатор мерчанта
     */
    public void closeMerchant(String merchantId) throws DaoException {
        log.debug("Closing a merchant with id {}", merchantId);
        Query query = getDslContext().update(CLEARING_MERCHANT)
                .set(CLEARING_MERCHANT.STATUS, CLOSE)
                .set(CLEARING_MERCHANT.VALID_TO, LocalDateTime.now())
                .where(CLEARING_MERCHANT.MERCHANT_ID.eq(merchantId).and(CLEARING_MERCHANT.STATUS.eq(OPEN)));
        execute(query);
        log.debug("The merchant {} was closed", merchantId);
    }

    /**
     * Получение истории изменений по мерчанту
     *
     * @param merchantId идентификатор мерчанта
     * @return список изменений по мерчанту
     */
    public List<ClearingMerchant> getMerchantHistory(String merchantId) {
        Query query = getDslContext().selectFrom(CLEARING_MERCHANT)
                .where(CLEARING_MERCHANT.MERCHANT_ID.eq(merchantId));
        return fetch(query, merchantRowMapper);
    }

}
