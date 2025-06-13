package com.apostle.services;

import com.apostle.data.models.Book;

import java.util.List;

public interface UserService{
    List<Book> searchBooks(String query);
    List<Book> viewAllAvailableBooks();
}
