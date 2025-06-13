package com.apostle.controllers;


import com.apostle.data.models.Book;
import com.apostle.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search-books")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchBooks(query));
    }

    @GetMapping("/available-books")
    public ResponseEntity<List<Book>> viewAllAvailableBooks(){

        return ResponseEntity.ok(userService.viewAllAvailableBooks());
    }
}
