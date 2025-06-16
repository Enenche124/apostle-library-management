package com.apostle.services;

import com.apostle.data.models.Book;
import com.apostle.data.repositories.BookRepository;
import com.apostle.exceptions.BookNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private BookSearchService bookSearchService;

    private final BookRepository bookRepository;


    @Autowired
    public AdminServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book addBook( Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("ISBN already exists");
        }
       return bookRepository.save(book);
    }



    @Override
    public void deleteBook(String isbn) throws BookNotFoundException {
        if (!bookRepository.existsByIsbn(isbn)) {
            throw new BookNotFoundException("Book with ISBN " + isbn + " not found");
        }
        bookRepository.deleteByIsbn(isbn);

    }

    @Override
    public Book updateBook(String isbn, Book book) throws BookNotFoundException {
        Book bookExist = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book with ISBN " + isbn + " not found"));
        bookExist.setAuthor(book.getAuthor());
        bookExist.setTitle(book.getTitle());
        bookExist.setPublisher(book.getPublisher());
        bookExist.setYearPublished(book.getYearPublished());
        return bookRepository.save(bookExist);
    }

    @Override
    public List<Book> searchBooks(String query) {
        if (query == null || query.isEmpty()) {
            return List.of();
        }

      return   bookSearchService.searchBooks(query);
    }

    @Override
    public Book findBookByIsbn(String isbn) throws BookNotFoundException {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book with ISBN " + isbn + " not found"));
    }

    public long count() {
        return bookRepository.count();
    }

    @Override
    public List<Book> viewAllBooks() {
        if (bookRepository.count() == 0 || bookRepository.findAll().isEmpty()) {
            return List.of();
        }
        return bookRepository.findAll();
    }
}
