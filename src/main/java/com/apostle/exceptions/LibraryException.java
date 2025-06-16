package com.apostle.exceptions;

import lombok.Getter;

@Getter
public class LibraryException extends RuntimeException{

    private final String borrowId;
    private final String isbn;
    private final String userEmail;
    private final Double fineAmount;


    public LibraryException(String messaged) {
        this(messaged, null, null, null, null);
    }
    public LibraryException(String message, String borrowId, String isbn, String userEmail, Double fineAmount) {
        super(message);
        this.borrowId = borrowId;
        this.isbn = isbn;
        this.userEmail = userEmail;
        this.fineAmount = fineAmount;
    }


}
