package com.bib.orm.demo.orm.session.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class EntityKey<T> {

    private final Object id;
    private final Class<T> type;

}
