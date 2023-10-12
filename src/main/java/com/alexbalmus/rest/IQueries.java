package com.alexbalmus.rest;

import java.util.List;

import org.hibernate.annotations.processing.HQL;
import org.hibernate.query.Page;

import com.alexbalmus.jpapractice.Book;

public interface IQueries
{
    @HQL("where title like :title order by title")
    List<Book> findBooksByTitleWithPagination(String title, Page page);
}
