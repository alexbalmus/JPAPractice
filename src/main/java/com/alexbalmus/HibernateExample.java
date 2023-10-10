package com.alexbalmus;

import static java.lang.Boolean.TRUE;
import static java.lang.System.out;
import static org.hibernate.cfg.AvailableSettings.*;

import java.util.Map;
import java.util.function.Consumer;

import org.hibernate.cfg.Configuration;

import com.alexbalmus.jpapractice.Book;
import com.alexbalmus.jpapractice.Book_;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class Main
{
    public static void main(String[] args)
    {
        performWithHibernateApi();
    }

    private static void performWithHibernateApi()
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

    private static void performWithJPAApi()
    {
        var factory = createEntityManagerFactory("example",
            // export the inferred database schema
            Map.of(JAKARTA_HBM2DDL_DATABASE_ACTION, CREATE));

        // persist an entity
        inSession(factory, entityManager -> {
            entityManager.persist(new Book("9781932394153", "Hibernate in Action"));
        });

        // query data using HQL
        inSession(factory, entityManager -> {
            out.println(entityManager.createQuery("select isbn||': '||title from Book").getSingleResult());
        });

        // query data using criteria API
        inSession(factory, entityManager -> {
            var builder = factory.getCriteriaBuilder();
            var query = builder.createQuery(String.class);
            var book = query.from(Book.class);
            query.select(builder.concat(builder.concat(book.get(Book_.isbn), builder.literal(": ")),
                book.get(Book_.title)));
            out.println(entityManager.createQuery(query).getSingleResult());
        });
    }

    // do some work in a session, performing correct transaction management
    static void inSession(EntityManagerFactory factory, Consumer<EntityManager> work) {
        var entityManager = factory.createEntityManager();
        var transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            work.accept(entityManager);
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
        finally {
            entityManager.close();
        }
    }
}