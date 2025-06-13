package com.apostle.services;

import com.apostle.data.models.Book;
import com.apostle.data.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private BookSearchService bookSearchService;
    @Autowired
    public void setBookSearchService(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }
    private final BookRepository bookRepository;
    @Autowired
    public UserServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    @Override
    public List<Book> searchBooks(String query) {
        if (query == null || query.isEmpty()) {
            return List.of();
        }
        return bookSearchService.searchBooks(query);
    }

    @Override
    public List<Book> viewAllAvailableBooks() {
        if (bookRepository.count() == 0 || bookRepository.findAll().isEmpty()) {
            return List.of();
        }
        return bookRepository.findAll();
    }
}
