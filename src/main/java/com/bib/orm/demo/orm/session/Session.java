package com.bib.orm.demo.orm.session;

public interface Session {

    <T> T save(T entity);
    <T> T find(Class<T> clazz, Object id);
    <T> boolean delete(T entity);
}
