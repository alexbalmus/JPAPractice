package com.alexbalmus;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;
import java.util.function.Consumer;

import static jakarta.persistence.Persistence.createEntityManagerFactory;
import static java.lang.System.out;
import static org.hibernate.cfg.AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION;
import static org.hibernate.tool.schema.Action.CREATE;

import com.alexbalmus.jpapractice.Book;
import com.alexbalmus.jpapractice.Book_;

public class JPAExample
{
    public static void main(String[] args)
    {
        var factory = createEntityManagerFactory("com.alexbalmus.jpapractice",
            // export the inferred database schema
            Map.of(JAKARTA_HBM2DDL_DATABASE_ACTION, CREATE));

        // persist an entity
        inSession(factory, entityManager ->
            entityManager.persist(new Book("9781932394153", "Hibernate in Action")));

        // query data using HQL
        inSession(factory, entityManager ->
            out.println(entityManager.createQuery("select isbn||': '||title from Book").getSingleResult()));

        // query data using criteria API
        inSession(factory, entityManager ->
        {
            var builder = factory.getCriteriaBuilder();
            var query = builder.createQuery(String.class);
            var book = query.from(Book.class);
            query.select(
                builder.concat(
                    builder.concat(book.get(Book_.isbn), builder.literal(": ")),
                    book.get(Book_.title)
                )
            );
            out.println(entityManager.createQuery(query).getSingleResult());
        });
    }

    // do some work in a session, performing correct transaction management
    static void inSession(EntityManagerFactory factory, Consumer<EntityManager> work)
    {
        var entityManager = factory.createEntityManager();
        var transaction = entityManager.getTransaction();

        try (entityManager)
        {
            transaction.begin();
            work.accept(entityManager);
            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction.isActive())
            {
                transaction.rollback();
            }
            throw e;
        }
    }
}
