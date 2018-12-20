package com.rbkmoney.midgard.base.clearing.helpers.DAO.common;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.impl.TableRecordImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;

public class RecordRowMapper<T> implements RowMapper<T> {

    private final Table table;

    private final Class<T> type;

    public RecordRowMapper(Table table, Class<T> type) {
        this.table = table;
        this.type = type;
    }

    @Override
    public T mapRow(ResultSet resultSet, int i) throws SQLException {
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        int columnCount = rsMetaData.getColumnCount();

        TableRecord record = new TableRecordImpl(table);
        for (int column = 1; column <= columnCount; column++) {
            String columnName = rsMetaData.getColumnName(column);
            Field field = record.field(columnName);

            Object value = getFieldValue(field, resultSet);
            if (!resultSet.wasNull()) {
                record.set(field, value);
            }
        }
        return record.into(type);
    }

    private Object getFieldValue(Field field, ResultSet resultSet) throws SQLException {
        if (field.getDataType().isBinary()) {
            return resultSet.getBytes(field.getName());
        }
        if (field.getType().isEnum()) {
            return toEnumField(resultSet.getString(field.getName()), field.getType());
        }
        return resultSet.getObject(field.getName(), field.getType());
    }

    public static <T extends Enum<T>> T toEnumField(String name, Class<T> enumType) {
        return Optional.ofNullable(name)
                .map(value -> Enum.valueOf(enumType, name))
                .orElse(null);
    }
}
