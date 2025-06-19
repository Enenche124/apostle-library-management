package com.apostle.controllers;

import com.apostle.data.models.Book;
import com.apostle.services.AdminService;
import com.apostle.services.GoogleBooksService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://127.0.0.1:5500", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final GoogleBooksService googleBooksService;

    @Autowired
    public AdminController(AdminService adminService, GoogleBooksService googleBooksService) {
        this.adminService = adminService;
        this.googleBooksService = googleBooksService;
    }

    @PostMapping("/books")
    public ResponseEntity<?> addBook(@Valid @RequestBody Book book) {
        try {
            boolean hasValidFields = book.getTitle() != null && !book.getTitle().isBlank() &&
                    book.getAuthor() != null && !book.getAuthor().isBlank() &&
                    book.getPublisher() != null && !book.getPublisher().isBlank() &&
                    book.getYearPublished() != 0;
            if (!hasValidFields && book.getIsbn() != null) {
                Book googleBook = googleBooksService.fetchBookByIsbn(book.getIsbn()).block();
                if (googleBook != null) {
                    book.setTitle(googleBook.getTitle() != null ? googleBook.getTitle() : book.getTitle());
                    book.setAuthor(googleBook.getAuthor() != null ? googleBook.getAuthor() : book.getAuthor());
                    book.setPublisher(googleBook.getPublisher() != null ? googleBook.getPublisher() : book.getPublisher());
                    book.setYearPublished(googleBook.getYearPublished() != 0 ? googleBook.getYearPublished() : book.getYearPublished());
                    book.setCategory(googleBook.getCategory() != null ? googleBook.getCategory() : book.getCategory());
                    book.setTags(googleBook.getTags() != null ? googleBook.getTags() : book.getTags());
                    book.setImageUrl(googleBook.getImageUrl() != null ? googleBook.getImageUrl() : book.getImageUrl());
                }
            }
            Book addedBook = adminService.addBook(book);
            return ResponseEntity.status(201).body(addedBook);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/books/{isbn}")
    public ResponseEntity<?> deleteBook(@PathVariable String isbn) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "ISBN cannot be null or empty"));
            }
            adminService.deleteBook(isbn);
            return ResponseEntity.ok(Map.of("message", "Book with ISBN " + isbn + " deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/books")
    public ResponseEntity<?> viewAllBooks() {
        try {
            return ResponseEntity.ok(Map.of("books", adminService.viewAllBooks()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/books/{isbn}")
    public ResponseEntity<?> updateBook(@PathVariable String isbn, @Valid @RequestBody Book book) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "ISBN cannot be null or empty"));
            }
            Book updatedBook = adminService.updateBook(isbn, book);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/books/search")
    public ResponseEntity<?> searchBooks(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Search query cannot be null or empty"));
            }
            return ResponseEntity.ok(Map.of("books", adminService.searchBooks(query)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/books/{isbn}")
    public ResponseEntity<?> findBookByIsbn(@PathVariable String isbn) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "ISBN cannot be null or empty"));
            }
            return ResponseEntity.ok(adminService.findBookByIsbn(isbn));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}