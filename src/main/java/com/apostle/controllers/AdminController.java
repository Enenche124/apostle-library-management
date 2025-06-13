package com.apostle.controllers;

import com.apostle.data.models.Book;
import com.apostle.services.AdminService;
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

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/add-book")
    public ResponseEntity<?> addBook(@Valid @RequestBody Book book) {
        try {
            Book addedBook = adminService.addBook(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedBook);
        }catch (Exception e){
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-book/{isbn}")
    public ResponseEntity<?> deleteBook(@PathVariable String isbn){
       try{
           adminService.deleteBook(isbn);
           return ResponseEntity.ok("Book with ISBN " + isbn + " deleted successfully");
       }catch (Exception e){
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
       }
    }
    @GetMapping("/view-books")
    public ResponseEntity<List<Book>> viewAllBooks(){
        return ResponseEntity.ok(adminService.viewAllBooks());
    }

    @PutMapping("/update-book/{isbn}")
    public ResponseEntity<?> updateBook(@PathVariable String isbn, @Valid @RequestBody Book book){
        try {
            return ResponseEntity.ok(adminService.updateBook(isbn, book));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/search-books")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String query){
        return ResponseEntity.ok(adminService.searchBooks(query));
    }

    @GetMapping("/find-book/{isbn}")
    public ResponseEntity<?> findBookByIsbn(@PathVariable String isbn){
        try {
            return ResponseEntity.ok(adminService.findBookByIsbn(isbn));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
