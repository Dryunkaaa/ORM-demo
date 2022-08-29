package com.bib.orm.demo.orm;

public interface ORM {

    <T> T save(T entity);
    <T> T find(Class<T> clazz, Long id);
    <T> boolean delete(T entity);
}
