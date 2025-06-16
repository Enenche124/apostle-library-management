package com.apostle.controllers;

import com.apostle.data.models.Book;
import com.apostle.services.AdminService;
import com.apostle.services.GoogleBooksService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/add-book")
    public ResponseEntity<?> addBook(@Valid @RequestBody Book book) {
        try {
            boolean hasValidFields = book.getTitle() != null && !book.getTitle().isBlank() &&
                    book.getAuthor() != null && !book.getAuthor().isBlank() &&
                    book.getPublisher() != null && !book.getPublisher().isBlank() &&
                    book.getYearPublished() != 0;
            if (!hasValidFields && book.getIsbn() != null) {
                Book googleBook = googleBooksService.fetchBookByIsbn(book.getIsbn()).block();
                if (googleBook != null) {
                    book.setTitle(googleBook.getTitle() != null && !googleBook.getTitle().isBlank() ? googleBook.getTitle() : book.getTitle());
                    book.setAuthor(googleBook.getAuthor() != null && !googleBook.getAuthor().isBlank() ? googleBook.getAuthor() : book.getAuthor());
                    book.setPublisher(googleBook.getPublisher() != null && !googleBook.getPublisher().isBlank() ? googleBook.getPublisher() : book.getPublisher());
                    book.setYearPublished(googleBook.getYearPublished() != 0 ? googleBook.getYearPublished() : book.getYearPublished());
                    book.setCategory(googleBook.getCategory() != null ? googleBook.getCategory() : book.getCategory());
                    book.setTags(googleBook.getTags() != null ? googleBook.getTags() : book.getTags());
                    book.setImageUrl(googleBook.getImageUrl() != null ? googleBook.getImageUrl() : book.getImageUrl());
                }
            }
            if (book.getTitle() == null || book.getTitle().isBlank() ||
                    book.getAuthor() == null || book.getAuthor().isBlank() ||
                    book.getPublisher() == null || book.getPublisher().isBlank() ||
                    book.getYearPublished() == 0) {
                throw new IllegalArgumentException("Title, author, publisher, and year published are required");
            }
            Book addedBook = adminService.addBook(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedBook);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @DeleteMapping("/delete-book/{isbn}")
    public ResponseEntity<?> deleteBook(@PathVariable String isbn) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ISBN cannot be null or empty");
            }
            adminService.deleteBook(isbn);
            return ResponseEntity.ok("Book with ISBN " + isbn + " deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/view-books")
    public ResponseEntity<List<Book>> viewAllBooks() {
        return ResponseEntity.ok(adminService.viewAllBooks());
    }

    @PutMapping("/update-book/{isbn}")
    public ResponseEntity<?> updateBook(@PathVariable String isbn, @Valid @RequestBody Book book) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ISBN cannot be null or empty");
            }
            return ResponseEntity.ok(adminService.updateBook(isbn, book));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/search-books")
    public ResponseEntity<?> searchBooks(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Search query cannot be null or empty");
            }
            return ResponseEntity.ok(adminService.searchBooks(query));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/find-book/{isbn}")
    public ResponseEntity<?> findBookByIsbn(@PathVariable String isbn) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ISBN cannot be null or empty");
            }
            return ResponseEntity.ok(adminService.findBookByIsbn(isbn));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}