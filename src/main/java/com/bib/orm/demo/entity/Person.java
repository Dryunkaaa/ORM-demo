package com.bib.orm.demo.entity;

import com.bib.orm.demo.annotation.Column;
import com.bib.orm.demo.annotation.Table;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Table(name = "person")
@Data
public class Person {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

}
