package com.bib.orm.demo;

import com.bib.orm.demo.entity.Person;
import com.bib.orm.demo.orm.CustomORM;
import lombok.SneakyThrows;
import org.mariadb.jdbc.MariaDbDataSource;

public class App {

    @SuppressWarnings(value = "unused")
    //language=MariaDB
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE person (id INT PRIMARY KEY AUTO_INCREMENT, first_name VARCHAR( 50 ), last_name VARCHAR ( 50 ))";
    private static CustomORM customORM;

    @SneakyThrows
    public static void main(String[] args) {
        initOrm();

        var person = customORM.find(Person.class, 1L);
        System.out.println(person);
    }

    @SneakyThrows
    private static void initOrm() {
        var dataSource = new MariaDbDataSource();

        dataSource.setUrl("jdbc:mariadb://127.0.0.2:3306/orm");
        dataSource.setUser("root");
        dataSource.setPassword("pass");

        customORM = new CustomORM(dataSource);
    }
}
