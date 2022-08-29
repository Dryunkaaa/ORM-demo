package com.bib.orm.demo;

import com.bib.orm.demo.entity.Person;
import com.bib.orm.demo.orm.session.factory.SessionFactory;
import lombok.SneakyThrows;
import org.mariadb.jdbc.MariaDbDataSource;

import javax.sql.DataSource;

public class App {

    @SuppressWarnings(value = "unused")
    //language=MariaDB
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE person (id INT PRIMARY KEY AUTO_INCREMENT, first_name VARCHAR( 50 ), last_name VARCHAR ( 50 ))";

    @SneakyThrows
    public static void main(String[] args) {
        var sessionFactory = new SessionFactory(createDataSource());
        var session = sessionFactory.createSession();

        var person = session.find(Person.class, 16L);
        System.out.println(person);

        var samePerson = session.find(Person.class, 16L);
        System.out.println(samePerson);

        System.out.println(person == samePerson);
    }


    @SneakyThrows
    private static DataSource createDataSource() {
        var dataSource = new MariaDbDataSource();

        dataSource.setUrl("jdbc:mariadb://127.0.0.2:3306/orm");
        dataSource.setUser("root");
        dataSource.setPassword("pass");

        return dataSource;
    }
}
