package com.bib.orm.demo.orm.session.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
public class EntityKey<T> {

    @Getter
    private final Object id;
}
