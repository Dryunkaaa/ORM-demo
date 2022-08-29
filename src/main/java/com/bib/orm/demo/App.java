package com.bib.orm.demo;

import com.bib.orm.demo.entity.Person;
import com.bib.orm.demo.orm.ORM;
import com.bib.orm.demo.orm.impl.CustomORM;
import lombok.SneakyThrows;
import org.mariadb.jdbc.MariaDbDataSource;

public class App {

    @SuppressWarnings(value = "unused")
    //language=MariaDB
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE person (id INT PRIMARY KEY AUTO_INCREMENT, first_name VARCHAR( 50 ), last_name VARCHAR ( 50 ))";
    private static ORM orm;

    @SneakyThrows
    public static void main(String[] args) {
        initOrm();

        Person newPerson = new Person();

        newPerson.setFirstName("Inserted person first name");
        newPerson.setLastName("Inserted person last name");

        orm.save(newPerson);

        System.out.println("INSERTED PERSON - " + orm.find(Person.class, newPerson.getId()));

        if (orm.delete(newPerson)) {
            System.out.println("PERSON WAS DELETED! ID - " + newPerson.getId());
        }

        System.out.println("Person by id = " + newPerson.getId() + "  ->  " + orm.find(Person.class, newPerson.getId()));
    }

    @SneakyThrows
    private static void initOrm() {
        var dataSource = new MariaDbDataSource();

        dataSource.setUrl("jdbc:mariadb://127.0.0.2:3306/orm");
        dataSource.setUser("root");
        dataSource.setPassword("pass");

        orm = new CustomORM(dataSource);
    }
}
