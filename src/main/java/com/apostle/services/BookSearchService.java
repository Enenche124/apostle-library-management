package com.apostle.services;

import com.apostle.data.models.Book;
import com.apostle.data.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookSearchService {
    private final BookRepository bookRepository;

    @Autowired
    public BookSearchService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> searchBooks(String query) {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(query, query, query);
        books.forEach(book -> {
            if (book.getImageUrl() == null && book.getIsbn() != null) {
                book.setImageUrl("https://covers.openlibrary.org/b/isbn/" + book.getIsbn() + "-M.jpg");
            }
        });
        return books;
    }
}
