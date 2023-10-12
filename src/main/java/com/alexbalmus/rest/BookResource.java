package com.alexbalmus.rest;

import static java.lang.Boolean.TRUE;
import static java.lang.System.out;
import static org.hibernate.cfg.JdbcSettings.FORMAT_SQL;
import static org.hibernate.cfg.JdbcSettings.HIGHLIGHT_SQL;
import static org.hibernate.cfg.JdbcSettings.PASS;
import static org.hibernate.cfg.JdbcSettings.SHOW_SQL;
import static org.hibernate.cfg.JdbcSettings.URL;
import static org.hibernate.cfg.JdbcSettings.USER;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Page;

import com.alexbalmus.jpapractice.Book;

@Path("/")
@Produces("application/json")
public class BookResource
{
    private static final int RESULTS_PER_PAGE = 20;

    final SessionFactory sessionFactory;

    public BookResource()
    {
        sessionFactory = new Configuration()
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
            session.persist(new Book(UUID.randomUUID().toString(), "Hibernate in Action")));

        // query data using HQL
        sessionFactory.inSession(session ->
            out.println(session.createSelectionQuery("select isbn||': '||title from Book").getResultList()));
    }

    @GET
    @Path("book/{isbn}")
    public Response getBook(String isbn)
    {
        var book = sessionFactory.fromTransaction(session -> session.find(Book.class, isbn));
        return book == null ? Response.status(404).build() : Response.ok(book).build();
    }

    @GET
    @Path("books/{titlePattern}/{page:\\d+}")
    public Response findBooks(String titlePattern, int page)
    {
        var books = sessionFactory.fromTransaction(
            session -> findBooksByTitleWithPagination(titlePattern, page, session));
        return books.isEmpty() ? Response.status(404).build() : Response.ok(books).build();
    }

    private List<Book> findBooksByTitleWithPagination(String titlePattern, int page, Session session)
    {
        return session.createSelectionQuery(
                "from Book where title like ?1 order by title", Book.class)
            .setParameter(1, titlePattern)
            .setPage(Page.page(RESULTS_PER_PAGE, page))
            .getResultList();
    }

    @GET
    @Path("books/{titlePattern}")
    public Response findBooksWithNamedQuery(String titlePattern, int page)
    {
        var books = sessionFactory.fromTransaction(
            session -> Queries.findBooksByTitleWithPagination(session, titlePattern,
                Page.page(RESULTS_PER_PAGE, page)));
        return books.isEmpty() ? Response.status(404).build() : Response.ok(books).build();
    }

    @GET
    @Path("books/{titlePattern}")
    public Response findBooksWithMetamodelGeneratedQuery(String titlePattern, int page)
    {
        List<Book> books = sessionFactory.fromTransaction(session ->
            IQueries_.findBooksByTitleWithPagination(session, titlePattern,
                Page.page(RESULTS_PER_PAGE, page)));
        return books.isEmpty() ? Response.status(404).build() : Response.ok(books).build();
    }

}
