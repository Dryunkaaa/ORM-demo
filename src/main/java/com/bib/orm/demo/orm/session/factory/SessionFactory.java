package com.bib.orm.demo.orm.session.factory;

import com.bib.orm.demo.orm.session.Session;
import com.bib.orm.demo.orm.session.impl.SessionImpl;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class SessionFactory {

    private final DataSource dataSource;

    public Session createSession() {
        return new SessionImpl(dataSource);
    }
}
