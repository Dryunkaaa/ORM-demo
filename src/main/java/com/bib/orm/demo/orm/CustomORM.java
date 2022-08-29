package com.bib.orm.demo.orm;

import com.bib.orm.demo.annotation.Column;
import com.bib.orm.demo.annotation.Table;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.persistence.Id;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.Locale;

import static java.text.MessageFormat.format;

@RequiredArgsConstructor
public class CustomORM {

    private final String SELECT_QUERY_TEMPLATE = "SELECT * FROM %s WHERE %s = %d";
    private final DataSource dataSource;

    @SneakyThrows
    public <T> T find(Class<T> clazz, Long id) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalStateException(format("Annotation @Table absent for class {0}", clazz.getName()));
        }

        var tableName = clazz.getAnnotation(Table.class).name();
        var idColumnName = getIdColumnName(clazz);

        var selectQuery = String.format(SELECT_QUERY_TEMPLATE, tableName, idColumnName, id);

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
    private <T> T mapResultSetToEntity(Class<T> clazz, ResultSet rs) {
        var entity = clazz.getConstructor().newInstance();

        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                var columnName = field.getAnnotation(Column.class).name();

                var data = rs.getObject(columnName, field.getType());
                setEntityValue(data, field, entity);
            }
        }

        return entity;
    }

    @SneakyThrows
    private <T> void setEntityValue(Object value, Field field, T entity) {
        var setterMethod = findSetter(field);
        setterMethod.invoke(entity, value);
    }

    private Method findSetter(Field field) {
        var fieldName = field.getName();
        var firstChar = String.valueOf(fieldName.charAt(0))
                .toUpperCase(Locale.ROOT);

        var methodName = "set" + firstChar + fieldName.substring(1);
        return findMethod(methodName, field.getType(), field.getDeclaringClass());
    }

    @SneakyThrows
    private <T> Method findMethod(String name, Class<?> type, Class<T> clazz) {
        return clazz.getMethod(name, type);
    }

    private <T> String getIdColumnName(Class<T> clazz) {
        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                var idColumn = field.getAnnotation(Column.class);
                return idColumn.name();
            }
        }

        throw new IllegalStateException(format("Id column was not found in instance of type {0}", clazz.getName()));
    }
}
