package com.alexbalmus.jpapractice;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class Book
{
    @Id
    String isbn;

    @NotNull
    String title;

    @ManyToOne(fetch = FetchType.LAZY)
    Author author;

    protected Book()
    {
    }

    public Book(String isbn, String title)
    {
        this.isbn = isbn;
        this.title = title;
    }
}
