package com.bib.orm.demo.entity;

import com.bib.orm.demo.annotation.Column;
import com.bib.orm.demo.annotation.Id;
import com.bib.orm.demo.annotation.Table;
import lombok.Data;

@Table(value = "person")
@Data
public class Person {

    @Id
    @Column(value = "id")
    private Long id;

    @Column(value = "first_name")
    private String firstName;

    @Column(value = "last_name")
    private String lastName;

}
