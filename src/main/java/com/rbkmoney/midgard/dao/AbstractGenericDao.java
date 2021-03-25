package com.rbkmoney.midgard.dao;

import com.rbkmoney.midgard.exception.DaoException;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@DependsOn("flywayInitializer")
public abstract class AbstractGenericDao extends NamedParameterJdbcDaoSupport implements Dao {

    private final DSLContext dslContext;

    public AbstractGenericDao(DataSource dataSource) {
        setDataSource(dataSource);
        Configuration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.POSTGRES);
        configuration.set(dataSource);
        this.dslContext = DSL.using(configuration);
    }

    protected DSLContext getDslContext() {
        return dslContext;
    }

    @Override
    public void executeProc(AbstractRoutine<Void> procedure) {
        procedure.execute(dslContext.configuration());
    }

    @Override
    public int execute(Query query) {
        return execute(query, -1);
    }

    @Override
    public int execute(Query query, int expectedRowsAffected) {
        return execute(query, expectedRowsAffected, getNamedParameterJdbcTemplate());
    }

    @Override
    public int execute(Query query,
                       int expectedRowsAffected,
                       NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return execute(query.getSQL(ParamType.NAMED),
                toSqlParameterSource(query.getParams()),
                expectedRowsAffected,
                namedParameterJdbcTemplate);
    }

    @Override
    public int execute(String namedSql,
                       SqlParameterSource parameterSource,
                       int expectedRowsAffected,
                       NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        try {
            int rowsAffected = namedParameterJdbcTemplate.update(
                    namedSql,
                    parameterSource);

            if (expectedRowsAffected != -1 && rowsAffected != expectedRowsAffected) {
                throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(
                        namedSql, expectedRowsAffected, rowsAffected
                );
            }

            return rowsAffected;
        } catch (NestedRuntimeException ex) {
            throw new DaoException(ex);
        }
    }

    @Override
    public <T> T fetchOne(Query query, Class<T> type) {
        return fetchOne(query, type, getNamedParameterJdbcTemplate());
    }

    @Override
    public <T> T fetchOne(Query query, Class<T> type, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return fetchOne(query, new SingleColumnRowMapper<>(type), namedParameterJdbcTemplate);
    }

    @Override
    public <T> T fetchOne(Query query, RowMapper<T> rowMapper) {
        return fetchOne(query, rowMapper, getNamedParameterJdbcTemplate());
    }

    @Override
    public <T> T fetchOne(Query query,
                          RowMapper<T> rowMapper,
                          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return fetchOne(query.getSQL(ParamType.NAMED),
                toSqlParameterSource(query.getParams()),
                rowMapper,
                namedParameterJdbcTemplate);
    }

    @Override
    public <T> T fetchOne(String namedSql,
                          SqlParameterSource parameterSource,
                          RowMapper<T> rowMapper,
                          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        try {
            return namedParameterJdbcTemplate.queryForObject(
                    namedSql,
                    parameterSource,
                    rowMapper
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        } catch (NestedRuntimeException ex) {
            throw new DaoException(ex);
        }
    }

    @Override
    public <T> List<T> fetch(Query query, RowMapper<T> rowMapper) {
        return fetch(query, rowMapper, getNamedParameterJdbcTemplate());
    }

    @Override
    public <T> List<T> fetch(String namedSql,
                             SqlParameterSource parameterSource,
                             RowMapper<T> rowMapper) {
        return fetch(namedSql, parameterSource, rowMapper, getNamedParameterJdbcTemplate());
    }

    @Override
    public <T> List<T> fetch(Query query,
                             RowMapper<T> rowMapper,
                             NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return fetch(query.getSQL(ParamType.NAMED),
                toSqlParameterSource(query.getParams()),
                rowMapper,
                namedParameterJdbcTemplate);
    }

    @Override
    public <T> List<T> fetch(String namedSql,
                             SqlParameterSource parameterSource,
                             RowMapper<T> rowMapper,
                             NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        try {
            return namedParameterJdbcTemplate.query(namedSql, parameterSource, rowMapper);
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public int executeWithReturn(Query query, KeyHolder keyHolder) {
        return executeWithReturn(
                query.getSQL(ParamType.NAMED),
                toSqlParameterSource(query.getParams()),
                -1,
                keyHolder
        );
    }

    @Override
    public int executeWithReturn(Query query, int expectedRowsAffected, KeyHolder keyHolder) {
        return executeWithReturn(
                query.getSQL(ParamType.NAMED),
                toSqlParameterSource(query.getParams()),
                expectedRowsAffected,
                getNamedParameterJdbcTemplate(),
                keyHolder
        );
    }

    @Override
    public int executeWithReturn(Query query,
                                 int expectedRowsAffected,
                                 NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                 KeyHolder keyHolder) {
        return executeWithReturn(
                query.getSQL(ParamType.NAMED),
                toSqlParameterSource(query.getParams()),
                expectedRowsAffected,
                namedParameterJdbcTemplate,
                keyHolder
        );
    }

    @Override
    public int executeWithReturn(String namedSql, KeyHolder keyHolder) {
        return executeWithReturn(namedSql, EmptySqlParameterSource.INSTANCE, keyHolder);
    }

    @Override
    public int executeWithReturn(String namedSql, SqlParameterSource parameterSource, KeyHolder keyHolder) {
        return executeWithReturn(namedSql, parameterSource, -1, keyHolder);
    }

    @Override
    public int executeWithReturn(String namedSql,
                                 SqlParameterSource parameterSource,
                                 int expectedRowsAffected,
                                 KeyHolder keyHolder) {
        return executeWithReturn(
                namedSql, parameterSource, expectedRowsAffected, getNamedParameterJdbcTemplate(), keyHolder
        );
    }

    @Override
    public int executeWithReturn(String namedSql,
                                 SqlParameterSource parameterSource,
                                 int expectedRowsAffected,
                                 NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                 KeyHolder keyHolder) {
        try {
            int rowsAffected = namedParameterJdbcTemplate.update(
                    namedSql,
                    parameterSource,
                    keyHolder);

            if (expectedRowsAffected != -1 && rowsAffected != expectedRowsAffected) {
                throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(
                        namedSql, expectedRowsAffected, rowsAffected
                );
            }
            return rowsAffected;
        } catch (NestedRuntimeException ex) {
            throw new DaoException(ex);
        }
    }

    /**
     * Метод преобразовывает структуру JOOQ параметров в список параметров Spring.
     *
     * @param params спосок jooq параметров
     * @return возвращает spring структуру
     */
    protected SqlParameterSource toSqlParameterSource(Map<String, Param<?>> params) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        for (Map.Entry<String, Param<?>> entry : params.entrySet()) {
            Param<?> param = entry.getValue();
            if (param.getValue() instanceof String) {
                sqlParameterSource.addValue(entry.getKey(),
                        ((String) param.getValue()).replace("\u0000", "\\u0000"));
            } else if (param.getValue() instanceof LocalDateTime || param.getValue() instanceof EnumType) {
                sqlParameterSource.addValue(entry.getKey(), param.getValue(), Types.OTHER);
            } else {
                sqlParameterSource.addValue(entry.getKey(), param.getValue());
            }
        }
        return sqlParameterSource;
    }

}
