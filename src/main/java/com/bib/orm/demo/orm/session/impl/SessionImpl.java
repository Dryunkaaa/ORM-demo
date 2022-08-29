package com.bib.orm.demo.orm.session.impl;

import com.bib.orm.demo.annotation.Column;
import com.bib.orm.demo.annotation.Id;
import com.bib.orm.demo.annotation.Table;
import com.bib.orm.demo.orm.session.Session;
import com.bib.orm.demo.orm.session.util.EntityKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@RequiredArgsConstructor
public class SessionImpl implements Session {

    private final DataSource dataSource;
    private final Map<EntityKey<?>, Object> cachedMap = new HashMap<>();

    @SneakyThrows
    @Override
    public <T> T find(Class<T> clazz, Object id) {
        var entityKey = new EntityKey<>(id, clazz);

        var entity = cachedMap.computeIfAbsent(entityKey, ek -> loadFromDb(entityKey));
        return clazz.cast(entity);
    }

    @SneakyThrows
    private <T> T loadFromDb(EntityKey<T> entityKey) {
        var clazz = entityKey.getType();
        var id = entityKey.getId();

        var idColumnName = getIdColumnName(clazz);
        var selectQuery = String.format("SELECT * FROM %s WHERE %s = %s", getTableName(clazz), idColumnName, id);

        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                var rs = statement.executeQuery(selectQuery);

                if (rs.next()) {
                    return mapResultSetToEntity(clazz, rs);
                }
            }
        }

        return null;
    }

    @SneakyThrows
    @Override
    public <T> T save(T entity) {
        var clazz = entity.getClass();

        var requiredFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class) && !f.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());

        var insertQuery = buildInsertQuery(clazz, requiredFields);

        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.prepareStatement(insertQuery)) {
                initPrepareStatementParameters(statement, requiredFields, entity);
                statement.executeUpdate();
            }

            var newId = getLastInsertedId(connection);
            setEntityValue(newId, getIdField(clazz), entity);
        }

        return entity;
    }

    @SneakyThrows
    @Override
    public <T> boolean delete(T entity) {
        var clazz = entity.getClass();

        var idField = getIdField(clazz);
        var idColumn = getColumnName(idField);

        var sql = String.format("DELETE FROM %s WHERE %s = ?", getTableName(clazz), idColumn);

        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.prepareStatement(sql)) {
                statement.setObject(1, getFieldValue(idField, entity));
                return statement.executeUpdate() > 0;
            }
        }
    }

    private String buildInsertQuery(Class<?> clazz, List<Field> fields) {
        var columnsToInsert = fields.stream()
                .map(this::getColumnName)
                .collect(Collectors.joining(", "));

        var parametersString = fields.stream()
                .map(f -> "?")
                .collect(Collectors.joining(","));

        return "INSERT INTO " + getTableName(clazz) + "(" + columnsToInsert + ") VALUES (" + parametersString + ")";
    }

    @SneakyThrows
    private <T> void initPrepareStatementParameters(PreparedStatement statement, List<Field> fields, T entity) {
        for (var i = 0; i < fields.size(); i++) {
            var field = fields.get(i);

            var value = getFieldValue(field, entity);
            statement.setObject(i + 1, value);
        }
    }

    @SneakyThrows
    private long getLastInsertedId(Connection connection) {
        try (var statement = connection.createStatement()) {
            var rs = statement.executeQuery("SELECT LAST_INSERT_ID()");

            if (rs.next()) {
                return rs.getLong(1);
            }
        }

        return -1;
    }

    @SneakyThrows
    private <T> T mapResultSetToEntity(Class<T> clazz, ResultSet rs) {
        var entity = clazz.getConstructor().newInstance();

        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                var columnName = getColumnName(field);

                var data = rs.getObject(columnName, field.getType());
                setEntityValue(data, field, entity);
            }
        }

        return entity;
    }

    @SneakyThrows
    private <T> Object getFieldValue(Field field, T entity) {
        return entity.getClass()
                .getMethod(buildGetterName(field))
                .invoke(entity);
    }

    @SneakyThrows
    private <T> void setEntityValue(Object value, Field field, T entity) {
        var setterMethod = findSetter(field);
        setterMethod.invoke(entity, value);
    }

    private Method findSetter(Field field) {
        var methodName = buildSetterName(field);
        return findMethod(methodName, field.getType(), field.getDeclaringClass());
    }

    private String buildSetterName(Field field) {
        var fieldName = field.getName();
        var firstChar = String.valueOf(fieldName.charAt(0))
                .toUpperCase(Locale.ROOT);

        return "set" + firstChar + fieldName.substring(1);
    }

    private String buildGetterName(Field field) {
        var fieldName = field.getName();
        var firstChar = String.valueOf(fieldName.charAt(0))
                .toUpperCase(Locale.ROOT);

        return "get" + firstChar + fieldName.substring(1);
    }

    @SneakyThrows
    private Method findMethod(String name, Class<?> type, Class<?> clazz) {
        return clazz.getMethod(name, type);
    }

    private void checkIfTableAnnotationExists(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalStateException(MessageFormat.format("Table annotation is absent in {0} class!", clazz.getName()));
        }
    }

    private String getTableName(Class<?> clazz) {
        checkIfTableAnnotationExists(clazz);
        return clazz.getAnnotation(Table.class).value();
    }

    private String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).value();
        }

        throw new IllegalStateException(format("@Column annotation was not found in instance of type {0} for field {1}!", field.getDeclaringClass().getName(), field.getName()));
    }

    private String getIdColumnName(Class<?> clazz) {
        var idField = getIdField(clazz);
        return getColumnName(idField);
    }

    private Field getIdField(Class<?> clazz) {
        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                return field;
            }
        }

        throw new IllegalStateException(format("Id column was not found in instance of type {0}", clazz.getName()));
    }
}
