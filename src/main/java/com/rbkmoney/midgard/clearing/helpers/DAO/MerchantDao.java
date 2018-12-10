package com.rbkmoney.midgard.clearing.helpers.DAO;

import com.rbkmoney.midgard.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.ClearingDao;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.RecordRowMapper;
import com.rbkmoney.midgard.clearing.exception.DaoException;
import org.jooq.Query;
import org.jooq.generated.midgard.tables.pojos.Merchant;
import org.jooq.generated.midgard.tables.records.MerchantRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.generated.midgard.enums.MerchantState.*;
import static org.jooq.generated.midgard.tables.Merchant.MERCHANT;

/**
 * Класс для взаимодействия с таблицей merchant.
 * В данной таблице хранится информация о точках, в которых была инициирована транзакция
 */
public class MerchantDao extends AbstractGenericDao implements ClearingDao<Merchant> {

    /** Логгер */
    private static final Logger log = LoggerFactory.getLogger(MerchantDao.class);
    /** Маппер */
    private final RowMapper<Merchant> merchantRowMapper;

    public MerchantDao(DataSource dataSource) {
        super(dataSource);
        merchantRowMapper = new RecordRowMapper<>(MERCHANT, Merchant.class);
    }

    @Override
    public Long save(Merchant merchant) throws DaoException {
        log.debug("Adding new merchant: {}", merchant);
        MerchantRecord record = getDslContext().newRecord(MERCHANT, merchant);
        Query query = getDslContext().insertInto(MERCHANT).set(record);
        execute(query);
        log.debug("New merchant with id {} was added", merchant.getMerchantId());
        return 0L;
    }

    @Override
    public Merchant get(String merchantId) throws DaoException {
        log.debug("Getting a merchant with id {}", merchantId);
        Query query = getDslContext().selectFrom(MERCHANT)
                .where(MERCHANT.MERCHANT_ID.eq(merchantId).and(MERCHANT.STATUS.eq(OPEN)));
        Merchant merchants = fetchOne(query, merchantRowMapper);
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
        Query query = getDslContext().update(MERCHANT)
                .set(MERCHANT.STATUS, CLOSE)
                .set(MERCHANT.VALID_TO, LocalDateTime.now())
                .where(MERCHANT.MERCHANT_ID.eq(merchantId).and(MERCHANT.STATUS.eq(OPEN)));
        execute(query);
        log.debug("The merchant {} was closed", merchantId);
    }

    /**
     * Получение истории изменений по мерчанту
     *
     * @param merchantId идентификатор мерчанта
     * @return список изменений по мерчанту
     */
    public List<Merchant> getMerchantHistory(String merchantId) {
        Query query = getDslContext().selectFrom(MERCHANT)
                .where(MERCHANT.MERCHANT_ID.eq(merchantId));
        return fetch(query, merchantRowMapper);
    }

}
