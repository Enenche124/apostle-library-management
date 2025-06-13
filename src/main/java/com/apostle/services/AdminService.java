package com.apostle.services;

import com.apostle.data.models.Book;
import com.apostle.exceptions.BookNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


public interface AdminService {

    @PreAuthorize("hasRole('ADMIN')")
    Book addBook(Book book);
    @PreAuthorize("hasRole('ADMIN')")
    void deleteBook(String isbn) throws BookNotFoundException;
    @PreAuthorize("hasRole('ADMIN')")
    Book updateBook(String isbn, Book book)  throws BookNotFoundException;
    List<Book> searchBooks(String query);
    Book findBookByIsbn(String isbn) throws BookNotFoundException;
    long count();
    List<Book> viewAllBooks();
}
