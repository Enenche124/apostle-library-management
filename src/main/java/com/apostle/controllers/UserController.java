package com.apostle.controllers;

import com.apostle.data.models.*;
import com.apostle.dtos.requests.PayFineRequest;
import com.apostle.dtos.responses.BorrowResponse;
import com.apostle.services.BorrowService;
import com.apostle.services.FineService;
import com.apostle.services.GoogleBooksService;
import com.apostle.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://127.0.0.1:5500", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@RestController
@RequestMapping("/api/v1/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final GoogleBooksService googleBooksService;
    private final BorrowService borrowService;
    private final FineService fineService;

    public UserController(UserService userService, GoogleBooksService googleBooksService,
                          BorrowService borrowService, FineService fineService) {
        this.userService = userService;
        this.googleBooksService = googleBooksService;
        this.borrowService = borrowService;
        this.fineService = fineService;
    }

    @GetMapping("/books/search")
    public ResponseEntity<?> searchBooks(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Search query cannot be null or empty"));
            }
            List<Book> localBooks = userService.searchBooks(query);
            List<Book> googleBooks = null;
            try {
                googleBooks = googleBooksService.searchBooks(query).blockOptional()
                        .orElse(List.of());
            } catch (Exception e) {
                logger.warn("Google Books API timeout or error: {}", e.getMessage());
            }
            if (googleBooks != null) {
                localBooks.addAll(googleBooks);
            }
            return ResponseEntity.ok(localBooks);
        } catch (Exception e) {
            logger.error("Search books failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Error searching books: " + e.getMessage()));
        }
    }

    @GetMapping("/books/available")
    public ResponseEntity<?> viewAllAvailableBooks() {
        try {
            return ResponseEntity.ok(userService.viewAllAvailableBooks());
        } catch (Exception e) {
            logger.error("View available books failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/borrow/{isbn}")
    public ResponseEntity<?> borrowBook(@PathVariable String isbn, Authentication authentication) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "ISBN cannot be null or empty"));
            }
            String userEmail = authentication.getName();
            BorrowResponse response = borrowService.borrowBook(isbn, userEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Borrow book failed for ISBN {}: {}", isbn, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/return/{bookIsbn}")
    public ResponseEntity<?> returnBook(@PathVariable String bookIsbn, Authentication authentication) {
        try {
            if (bookIsbn == null || bookIsbn.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "ISBN cannot be null or empty"));
            }
            String userEmail = authentication.getName();
            BorrowResponse response = borrowService.returnBook(bookIsbn, userEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Return book failed for ISBN {}: {}", bookIsbn, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/pay-fine/{borrowId}")
    public ResponseEntity<?> payFine(@PathVariable String borrowId,
                                     @Valid @RequestBody PayFineRequest request,
                                     Authentication authentication) {
        try {
            if (borrowId == null || borrowId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Borrow ID cannot be null or empty"));
            }
            logger.info("Pay fine request for borrowId: {}, amount: {}", borrowId, request.getAmount());
            String userEmail = authentication.getName();
            List<Fine> userFines = fineService.getUserFines(userEmail);
            Fine fine = userFines.stream()
                    .filter(f -> f.getBorrowId().equals(borrowId) && f.getStatus() == FineStatus.PENDING)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No pending fine found for borrow ID: " + borrowId));
            Fine updatedFine = fineService.processPayment(fine.getId(), request.getAmount(), PaymentMethod.CASH);
            logger.info("Fine paid successfully for borrowId: {}, fineId: {}", borrowId, fine.getId());
            return ResponseEntity.ok(Map.of(
                    "message", "Fine paid successfully",
                    "fineId", updatedFine.getId(),
                    "remainingAmount", updatedFine.getRemainingAmount(),
                    "status", updatedFine.getStatus()
            ));
        } catch (Exception e) {
            logger.error("Pay fine failed for borrowId: {}, user: {}, error: {}", borrowId, authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/fines")
    public ResponseEntity<?> getUserFines(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            logger.info("Fetching fines for user: {}", userEmail);
            List<Fine> fines = fineService.getUserFines(userEmail);
            return ResponseEntity.ok(fines);
        } catch (Exception e) {
            logger.error("Get fines failed for user: {}, error: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/borrowed")
    public ResponseEntity<?> getBorrowedBooks(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            logger.info("Fetching borrowed books for user: {}", userEmail);
            List<BorrowBookRecord> borrowedBooks = borrowService.getCurrentBorrowings(userEmail);
            return ResponseEntity.ok(Map.of("books", borrowedBooks));
        } catch (Exception e) {
            logger.error("Get borrowed books failed for user: {}, error: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}