package com.apostle.controllers;

import com.apostle.data.models.Book;
import com.apostle.services.GoogleBooksService;
import com.apostle.services.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@PreAuthorize("hasRole('mUSER')")
public class UserController {

    private final UserService userService;
    private final GoogleBooksService googleBooksService;

    public UserController(UserService userService, GoogleBooksService googleBooksService) {
        this.userService = userService;
        this.googleBooksService = googleBooksService;
    }

    @GetMapping("/search-books")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam @NotBlank String query) {
        List<Book> localBooks = userService.searchBooks(query);
        List<Book> googleBooks = googleBooksService.searchBooks(query)
                .block(); // Use async in production
        if (googleBooks != null) {
            localBooks.addAll(googleBooks);
        }
        return ResponseEntity.ok(localBooks);
    }

    @GetMapping("/available-books")
    public ResponseEntity<List<Book>> viewAllAvailableBooks() {
        return ResponseEntity.ok(userService.viewAllAvailableBooks());
    }
}