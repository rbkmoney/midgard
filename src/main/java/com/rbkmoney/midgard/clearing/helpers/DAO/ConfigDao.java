package com.rbkmoney.midgard.clearing.helpers.DAO;

import com.rbkmoney.midgard.clearing.helpers.DAO.common.AbstractGenericDao;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.ClearingDao;
import com.rbkmoney.midgard.clearing.helpers.DAO.common.RecordRowMapper;
import org.jooq.Query;
import org.jooq.generated.midgard.tables.pojos.Config;
import org.jooq.generated.midgard.tables.records.ConfigRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import java.util.Optional;

import static org.jooq.generated.midgard.tables.Config.CONFIG;

/** Класс для взаимодействия с конфигурационной таблицей клиринга */
public class ConfigDao extends AbstractGenericDao implements ClearingDao<Config> {

    /** Логгер */
    private static final Logger log = LoggerFactory.getLogger(ConfigDao.class);
    /** Маппер */
    private final RowMapper<Config> configRowMapper;

    public ConfigDao(DataSource dataSource) {
        super(dataSource);
        configRowMapper = new RecordRowMapper<>(CONFIG, Config.class);
    }

    @Override
    public Long save(Config config) {
        log.debug("Adding new config: {}", config);
        ConfigRecord record = getDslContext().newRecord(CONFIG, config);
        Query query = getDslContext().insertInto(CONFIG).set(record);
        execute(query);
        log.debug("Config with name {} was added", config.getName());
        return 0L;
    }

    @Override
    public Config get(String name) {
        Query query = getDslContext().selectFrom(CONFIG).where(CONFIG.NAME.eq(name));
        return fetchOne(query, configRowMapper);
    }

    /**
     * Получение идентификатора последнего эвента полученного от внешней системы
     *
     * @return номер последнего эвента
     */
    public Optional<Long> getLastEventId() {
        Config config = get("last_event_id");
        if (config != null) {
            return Optional.ofNullable(Long.parseLong(config.getValue()));
        }
        return Optional.empty();
    }

}
