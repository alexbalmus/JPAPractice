package com.alexbalmus.rest;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.processing.CheckHQL;
import org.hibernate.query.Page;

import com.alexbalmus.jpapractice.Book;

@CheckHQL // validate named queries at compile time
@NamedQuery(name="findBooksByTitle",
    query="from Book where title like :title order by title")
class Queries
{
    static List<Book> findBooksByTitleWithPagination(Session session, String titlePattern, Page page)
    {
        return session.createNamedQuery("findBooksByTitle", Book.class)
            .setParameter("title", titlePattern)
            .setPage(page)
            .getResultList();
    }
}


