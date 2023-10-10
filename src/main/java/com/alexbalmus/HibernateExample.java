package com.alexbalmus;

import static java.lang.Boolean.TRUE;
import static java.lang.System.out;
import static org.hibernate.cfg.AvailableSettings.FORMAT_SQL;
import static org.hibernate.cfg.AvailableSettings.HIGHLIGHT_SQL;
import static org.hibernate.cfg.AvailableSettings.PASS;
import static org.hibernate.cfg.AvailableSettings.SHOW_SQL;
import static org.hibernate.cfg.AvailableSettings.URL;
import static org.hibernate.cfg.AvailableSettings.USER;

import org.hibernate.cfg.Configuration;

import com.alexbalmus.jpapractice.Book;
import com.alexbalmus.jpapractice.Book_;

public class HibernateExample
{
    public static void main(String[] args)
    {
        var sessionFactory = new Configuration()
            .addAnnotatedClass(Book.class)
            // use H2 in-memory database
            .setProperty(URL, "jdbc:h2:mem:db1")
            .setProperty(USER, "sa")
            .setProperty(PASS, "")
            // use Agroal connection pool
            .setProperty("hibernate.agroal.maxSize", "20")
            // display SQL in console
            .setProperty(SHOW_SQL, TRUE.toString())
            .setProperty(FORMAT_SQL, TRUE.toString())
            .setProperty(HIGHLIGHT_SQL, TRUE.toString())
            .buildSessionFactory();

        // export the inferred database schema
        sessionFactory.getSchemaManager().exportMappedObjects(true);

        // persist an entity
        sessionFactory.inTransaction(session ->
        {
            session.persist(new Book("9781932394153", "Hibernate in Action"));
        });

        // query data using HQL
        sessionFactory.inSession(session ->
        {
            out.println(session.createSelectionQuery("select isbn||': '||title from Book").getSingleResult());
        });

        // query data using criteria API
        sessionFactory.inSession(session ->
        {
            var builder = sessionFactory.getCriteriaBuilder();
            var query = builder.createQuery(String.class);
            var book = query.from(Book.class);
            query.select(
                builder.concat(
                    builder.concat(book.get(Book_.isbn), builder.literal(": ")),
                    book.get(Book_.title)
                )
            );
            out.println(session.createSelectionQuery(query).getSingleResult());
        });
    }

}